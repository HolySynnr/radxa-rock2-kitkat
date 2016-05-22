/*
 * RK BLURAY Streaming demuxer
 * Copyright (c) 2010 Martin Storsjo
 *
 * This file is part of FFmpeg.
 *
 * FFmpeg is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * FFmpeg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with FFmpeg; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

/**
 * @file
 * RK BLURAY Streaming demuxer
 */

#include "libavutil/avstring.h"
#include "libavutil/intreadwrite.h"
#include "libavutil/mathematics.h"
#include "libavutil/opt.h"
#include "libavutil/dict.h"
#include "libavutil/time.h"
#include "avformat.h"
#include "internal.h"
#include "avio_internal.h"
#include "url.h"

#define INITIAL_BUFFER_SIZE 32768
#define RKBLURAY_DEBUG 0
/*
 * RK BLURAY stream consists of a playlist with media segment files,
 * played sequentially. There may be several playlists with the same
 * video content, in different bandwidth variants, that are played in
 * parallel (preferably only one bandwidth variant at a time). In this case,
 * the user supplied the url to a main playlist that only lists the variant
 * playlists.
 *
 * If the main playlist doesn't point at any variants, we still create
 * one anonymous toplevel variant for this, to maintain the structure.
 */

enum KeyType {
    KEY_NONE,
    KEY_AES_128,
};

struct RKBLURAYSegment {
    double duration;
    int discontinuity;
	int is_seek;                 //add by xhr, for Bluray
	double seek_time;	         //add by xhr, for Bluray
	double seek_time_end;        //add by xhr, for Bluray
	double seek_operation_time;  //add by xhr, for Bluray
    char url[MAX_URL_SIZE];
    char key[MAX_URL_SIZE];
    enum KeyType key_type;
    uint8_t iv[16];
};

/*
 * Each variant has its own demuxer. If it currently is active,
 * it has an open AVIOContext too, and potentially an AVPacket
 * containing the next packet from this stream.
 */
struct RKBLURAYVariant {
    int bandwidth;
    char url[MAX_URL_SIZE];
    AVIOContext pb;
    uint8_t* read_buffer;
    URLContext *input;
    AVFormatContext *parent;
    int index;
    AVFormatContext *ctx;
    AVPacket pkt;
    int stream_offset;
    int finished;
    int target_duration;
    int start_seq_no;
    int n_segments;
	int bluray_play_seq_no;               //add by xhr, 
	int64_t bluray_play_seq_position;     //add by xhr,
    struct RKBLURAYSegment **segments;
    int needed, cur_needed;
    int cur_seq_no;
    int64_t last_load_time;
    char key_url[MAX_URL_SIZE];
    uint8_t key[16];
};

typedef struct RKBLURAYContext {
    int n_variants;
    struct RKBLURAYVariant **variants;
    int cur_seq_no;
    int end_of_segment;
    int first_packet;
    int64_t first_timestamp;
    int64_t seek_timestamp;
    int seek_flags;
    int abort;
    AVIOInterruptCB *interrupt_callback;
	int rkb_seek_time; //add by xhr, for Bluray time position show
} RKBLURAYContext;


static int rkb_read_chomp_line(AVIOContext *s, char *buf, int maxlen)
{
    int len = ff_get_line(s, buf, maxlen);
    while (len > 0 && isspace(buf[len - 1]))
        buf[--len] = '\0';
    return len;
}

static void rkb_free_segment_list(struct RKBLURAYVariant *var)
{
    int i;
    for (i = 0; i < var->n_segments; i++)
        av_free(var->segments[i]);
    av_freep(&var->segments);
    var->n_segments = 0;
}

static void rkb_free_variant_list(RKBLURAYContext *c)
{
    int i;
    for (i = 0; i < c->n_variants; i++) {
        struct RKBLURAYVariant *var = c->variants[i];
        rkb_free_segment_list(var);
        av_free_packet(&var->pkt);
        av_free(var->pb.buffer);
        if (var->input)
            ffurl_close(var->input);
        if (var->ctx) {
            var->ctx->pb = NULL;
            avformat_close_input(&var->ctx);
        }
        av_free(var);
    }
    av_freep(&c->variants);
    c->n_variants = 0;
}

/*
 * Used to reset a statically allocated AVPacket to a clean slate,
 * containing no data.
 */
static void rkb_reset_packet(AVPacket *pkt)
{
    av_init_packet(pkt);
    pkt->data = NULL;
}

static struct RKBLURAYVariant *rkb_new_variant(RKBLURAYContext *c, int bandwidth,
                                   const char *url, const char *base)
{
    struct RKBLURAYVariant *var = av_mallocz(sizeof(struct RKBLURAYVariant));
    if (!var)
        return NULL;
    rkb_reset_packet(&var->pkt);
    var->bandwidth = bandwidth;
    ff_make_absolute_url(var->url, sizeof(var->url), base, url);
    dynarray_add(&c->variants, &c->n_variants, var);
    return var;
}

struct variant_info {
    char bandwidth[20];
};

static void rkb_handle_variant_args(struct variant_info *info, const char *key,
                                int key_len, char **dest, int *dest_len)
{
    if (!strncmp(key, "BANDWIDTH=", key_len)) {
        *dest     =        info->bandwidth;
        *dest_len = sizeof(info->bandwidth);
    }
}

struct key_info {
     char uri[MAX_URL_SIZE];
     char method[10];
     char iv[35];
};

static void rkb_handle_key_args(struct key_info *info, const char *key,
                            int key_len, char **dest, int *dest_len)
{
    if (!strncmp(key, "METHOD=", key_len)) {
        *dest     =        info->method;
        *dest_len = sizeof(info->method);
    } else if (!strncmp(key, "URI=", key_len)) {
        *dest     =        info->uri;
        *dest_len = sizeof(info->uri);
    } else if (!strncmp(key, "IV=", key_len)) {
        *dest     =        info->iv;
        *dest_len = sizeof(info->iv);
    }
}

static int rkb_parse_playlist(RKBLURAYContext *c, const char *url,
                          struct RKBLURAYVariant *var, AVIOContext *in)
{
#if RKBLURAY_DEBUG
    av_log(NULL, AV_LOG_ERROR,"rk bluray rkb_parse_playlist in, url = %s\n", url);
#endif
    int ret = 0,is_segment = 0,is_discontinuity = 0, is_variant = 0, bandwidth = 0;
    int is_seek = 0;				
	double seek_time = 0;			
	double seek_time_end = 0;		
    double duration = 0;
    enum KeyType key_type = KEY_NONE;
    uint8_t iv[16] = "";
    int has_iv = 0;
    char key[MAX_URL_SIZE] = "";
    char line[1024];
    const char *ptr;
    int close_in = 0;
    if (!in) {
        AVDictionary *opts = NULL;
        close_in = 1;
        /* Some RK BLURAY servers dont like being sent the range header */
        av_dict_set(&opts, "seekable", "0", 0);
        av_dict_set(&opts, "user-agent","Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10",0);
        ret = avio_open2(&in, url, AVIO_FLAG_READ,
                         c->interrupt_callback, &opts);

        av_dict_free(&opts);
        if (ret < 0)
            return ret;
    }

    rkb_read_chomp_line(in, line, sizeof(line));
    if (strcmp(line, "#RKBM3U")) {
        ret = AVERROR_INVALIDDATA;
        goto fail;
    }

    if (var) {
        rkb_free_segment_list(var);
        var->finished = 0;
		var->bluray_play_seq_no = 0;
    }
    while (!url_feof(in)) {
        rkb_read_chomp_line(in, line, sizeof(line));
        if (av_strstart(line, "#RKB-X-STREAM-INF:", &ptr)) {
            struct variant_info info = {{0}};
            is_variant = 1;
#if RKBLURAY_DEBUG
            av_log(NULL, AV_LOG_ERROR,"RKB-X-STREAM-INF\n");
#endif
            ff_parse_key_value(ptr, (ff_parse_key_val_cb) rkb_handle_variant_args,
                               &info);
            bandwidth = atoi(info.bandwidth);
        } else if (av_strstart(line, "#RKB-X-KEY:", &ptr)) {
            struct key_info info = {{0}};
            ff_parse_key_value(ptr, (ff_parse_key_val_cb) rkb_handle_key_args,
                               &info);
            key_type = KEY_NONE;
            has_iv = 0;
            if (!strcmp(info.method, "AES-128"))
                key_type = KEY_AES_128;
            if (!strncmp(info.iv, "0x", 2) || !strncmp(info.iv, "0X", 2)) {
                ff_hex_to_data(iv, info.iv + 2);
                has_iv = 1;
            }
            av_strlcpy(key, info.uri, sizeof(key));
        } else if (av_strstart(line, "#RKB-X-TARGETDURATION:", &ptr)) {
            if (!var) {
                var = rkb_new_variant(c, 0, in->url, NULL);
                if (!var) {
                    ret = AVERROR(ENOMEM);
                    goto fail;
                }
            }
            var->target_duration = atoi(ptr);
        } else if (av_strstart(line, "#RKB-X-MEDIA-SEQUENCE:", &ptr)) {
            if (!var) {
                var = rkb_new_variant(c, 0, in->url, NULL);
                if (!var) {
                    ret = AVERROR(ENOMEM);
                    goto fail;
                }
            }
            var->start_seq_no = atoi(ptr);
        } else if (av_strstart(line, "#RKB-X-ENDLIST", &ptr)) {
            if (var)
                var->finished = 1;
        } else if (av_strstart(line, "#RKBINF:", &ptr)) {
            is_segment = 1;
            duration   = strtod(ptr,NULL);
        }else if(av_strstart(line, "#RKB-X-DISCONTINUITY",NULL)){
           is_discontinuity = 1;
            av_log(NULL, AV_LOG_ERROR,"rk bluray parse DISCONTINUITY\n");
        }else if(av_strstart(line, "#RKB-X-SEEK:",&ptr)) {     //add by xhr, for Bluray
        	is_seek = 1;
			seek_time = strtod(ptr,NULL);
			av_log(NULL, AV_LOG_ERROR, "Hery, seek_position = %f", seek_time);
		}else if(av_strstart(line,"#RKB-X-SEEK-END:", &ptr)){
			seek_time_end = strtod(ptr,NULL);
			av_log(NULL, AV_LOG_ERROR, "Hery, seek_position_end = %f", seek_time_end);
		}else if(av_strstart(line,"#RKB-PLAY_SEG_NO:", &ptr)){ 
			if(var){
				var->bluray_play_seq_no= strtod(ptr,NULL);
				av_log(NULL, AV_LOG_ERROR, "Hery,  bluray_play_seq_no= %d", var->bluray_play_seq_no);
			}
		}else if(av_strstart(line,"#RKB-PLAY_SEG_POSITION:", &ptr)){ 
			if(var){
				var->bluray_play_seq_position= strtod(ptr,NULL);
				av_log(NULL, AV_LOG_ERROR, "Hery,  bluray_play_seq_position= %lld", var->bluray_play_seq_position);
			}
		}else if(av_strstart(line,"#RKB-PLAY_SEG_TIME:", &ptr)){
			if(c){
				c->rkb_seek_time = strtod(ptr,NULL);
				av_log(NULL, AV_LOG_ERROR, "Hery,  bluray_play_seq_time= %d", c->rkb_seek_time);
			}
		}
		else if (av_strstart(line, "#", NULL)) {
            continue;
        } else if (line[0]) {
            if (is_variant) {
#if RKBLURAY_DEBUG
                av_log(NULL, AV_LOG_ERROR,"url = %s\n",url);
#endif
                if (!rkb_new_variant(c, bandwidth, line, in->url)) {
                    ret = AVERROR(ENOMEM);
                    goto fail;
                }
                is_variant = 0;
                bandwidth  = 0;
            }
            if (is_segment) {
                struct RKBLURAYSegment *seg;
                if (!var) {
                    var = rkb_new_variant(c, 0, in->url, NULL);
                    if (!var) {
                        ret = AVERROR(ENOMEM);
                        goto fail;
                    }
                }
                seg = av_malloc(sizeof(struct RKBLURAYSegment));
                if (!seg) {
                    ret = AVERROR(ENOMEM);
                    goto fail;
                }
                seg->duration = duration;
                seg->key_type = key_type;
				seg->seek_operation_time = 0;  //add by xhr, for Bluray seek operation
                if(is_discontinuity){
                    seg->discontinuity = 1;
                    is_discontinuity = 0;
                }else{
                    seg->discontinuity = 0;
                }
				//add by xhr , for Bluray
				if(is_seek){
					seg->is_seek = 1;
					seg->seek_time = seek_time;
					seg->seek_time_end = seek_time_end;
					//seg->real_duration = real_duration;
					is_seek = 0;
					seek_time = 0;
					seek_time_end = 0;
					//real_duration = 0;
					duration= 0;
				}else{
					seg->is_seek = 0;
					seg->seek_time = 0;
					seg->seek_time_end = 0;
					//seg->real_duration = 0; 
					duration = 0;
				}
                if (has_iv) {
                    memcpy(seg->iv, iv, sizeof(iv));
                } else {
                    int seq = var->start_seq_no + var->n_segments;
                    memset(seg->iv, 0, sizeof(seg->iv));
                    AV_WB32(seg->iv + 12, seq);
                }
                ff_make_absolute_url(seg->key, sizeof(seg->key), in->url, key);
                ff_make_absolute_url(seg->url, sizeof(seg->url), in->url, line);
                dynarray_add(&var->segments, &var->n_segments, seg);
                is_segment = 0;
            }
        }
    }
    if (var)
        var->last_load_time = av_gettime();

fail:
    if (close_in)
        avio_close(in);
    return ret;
}

static int rkb_open_input(struct RKBLURAYVariant *var)
{
    AVDictionary *opts = NULL;
    int ret;
    struct RKBLURAYSegment *seg = var->segments[var->cur_seq_no - var->start_seq_no];
    av_dict_set(&opts, "seekable", "0", 0);
    av_dict_set(&opts, "timeout", "500000", 0);
    av_dict_set(&opts, "user-agent","Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10",0);

    if (seg->key_type == KEY_NONE) {
        ret = ffurl_open(&var->input, seg->url, AVIO_FLAG_READ,
                          &var->parent->interrupt_callback, &opts);
		//add by xhr, for Bluray
		if(seg->is_seek){
			if(seg->seek_operation_time){
				int64_t filesize = ffurl_size(var->input);

				int64_t totolsize = seg->seek_time + ((seg->seek_time_end - seg->seek_time) / seg->duration) * seg->seek_operation_time;
				int ret = ffurl_seek(var->input,totolsize,SEEK_SET);
				if (ret < 0){
					av_log(NULL, AV_LOG_ERROR, "Hery, seek err");
				}
			}else{
			    av_log(NULL, AV_LOG_ERROR, "Hery, seek var->bluray_play_seq_position = %lld", var->bluray_play_seq_position);
				int64_t filesize = ffurl_size(var->input);

				int ret = ffurl_seek(var->input,seg->seek_time + var->bluray_play_seq_position,SEEK_SET);

				if (ret < 0){
					av_log(NULL, AV_LOG_ERROR, "Hery, seek err");
				}
			}

		}else{
			av_log(NULL, AV_LOG_ERROR, "Hery, seek var->bluray_play_seq_position = %lld", var->bluray_play_seq_position);
			int ret = ffurl_seek(var->input,var->bluray_play_seq_position,SEEK_SET);
			if (ret < 0){
				av_log(NULL, AV_LOG_ERROR, "Hery, seek err");
			}


		}
        goto cleanup;
    } else if (seg->key_type == KEY_AES_128) {
        char iv[33], key[33], url[MAX_URL_SIZE];
        if (strcmp(seg->key, var->key_url)) {
            URLContext *uc;
            if (ffurl_open(&uc, seg->key, AVIO_FLAG_READ,
                           &var->parent->interrupt_callback, &opts) == 0) {
                if (ffurl_read_complete(uc, var->key, sizeof(var->key))
                    != sizeof(var->key)) {
                    av_log(NULL, AV_LOG_ERROR, "Unable to read key file %s\n",
                           seg->key);
                }
                ffurl_close(uc);
            } else {
                av_log(NULL, AV_LOG_ERROR, "Unable to open key file %s\n",
                       seg->key);
            }
            av_strlcpy(var->key_url, seg->key, sizeof(var->key_url));
        }
        ff_data_to_hex(iv, seg->iv, sizeof(seg->iv), 0);
        ff_data_to_hex(key, var->key, sizeof(var->key), 0);
        iv[32] = key[32] = '\0';
        if (strstr(seg->url, "://"))
            snprintf(url, sizeof(url), "crypto+%s", seg->url);
        else
            snprintf(url, sizeof(url), "crypto:%s", seg->url);
        if ((ret = ffurl_alloc(&var->input, url, AVIO_FLAG_READ,
                               &var->parent->interrupt_callback)) < 0)
            goto cleanup;
        av_opt_set(var->input->priv_data, "key", key, 0);
        av_opt_set(var->input->priv_data, "iv", iv, 0);
        /* Need to repopulate options */
        av_dict_free(&opts);
        av_dict_set(&opts, "seekable", "0", 0);
        av_dict_set(&opts, "user-agent","Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10",0);
        av_dict_set(&opts, "timeout", "500000", 0);
        if ((ret = ffurl_connect(var->input, &opts)) < 0) {
            ffurl_close(var->input);
            var->input = NULL;
            goto cleanup;
        }
        ret = 0;
    }
    else
      ret = AVERROR(ENOSYS);

cleanup:
    av_dict_free(&opts);
    return ret;
}

static int rkb_read_data(void *opaque, uint8_t *buf, int buf_size)
{
    struct RKBLURAYVariant *v = opaque;
    RKBLURAYContext *c = v->parent->priv_data;
    int ret, i,retry = 0,parse_list_retry = 20,read_timeout_cnt = 0;
    int64_t last_load_timeUs = av_gettime();
    if(v->parent->exit_flag){
        return AVERROR_EOF;
    }
restart:
    if (!v->input) {
        /* If this is a live stream and the reload interval has elapsed since
         * the last playlist reload, reload the variant playlists now. */
        int64_t reload_interval = v->n_segments > 0 ?
                                  v->segments[v->n_segments - 1]->duration :
                                  v->target_duration;
#if RKBLURAY_DEBUG
  //      av_log(NULL, AV_LOG_ERROR,"reload_interval = %"PRId64"\n",reload_interval);
#endif

        reload_interval *= 1000000;

reload:
        if (!v->finished &&
            av_gettime() - v->last_load_time >= reload_interval) {
            if ((ret = rkb_parse_playlist(c, v->url, v, NULL)) < 0){
                parse_list_retry--;
                if(parse_list_retry < 0){
                    av_log(NULL, AV_LOG_ERROR,"rkb_parse_playlist return ret = %d",ret);
                return ret;
                }
                av_usleep(1000*1000);
            }
            /* If we need to reload the playlist again below (if
             * there's still no more segments), switch to a reload
             * interval of half the target duration. */
            reload_interval = v->target_duration * 500000LL;
        }
        if (v->cur_seq_no < v->start_seq_no) {
            av_log(NULL, AV_LOG_WARNING,
                   "skipping %d segments ahead, expired from playlists\n",
                   v->start_seq_no - v->cur_seq_no);
            v->cur_seq_no = v->start_seq_no;
        }
        if (v->cur_seq_no >= v->start_seq_no + v->n_segments) {
            if (v->finished){
                av_log(NULL, AV_LOG_ERROR, "finished RK BLURAY read data AVERROR_EOF\n");
                return AVERROR_EOF;
            }
            while (av_gettime() - v->last_load_time < reload_interval) {
                if (ff_check_interrupt(c->interrupt_callback))
                    return AVERROR_EXIT;
                av_usleep(100*1000);
            }
            /* Enough time has elapsed since the last reload */
            goto reload;
        }

        ret = rkb_open_input(v);
        if (ret < 0){
            if((av_gettime() - last_load_timeUs) >= 60000000){
                ffurl_close(v->input);
                v->input = NULL;
                v->cur_seq_no++;
                av_log(NULL, AV_LOG_ERROR, "RK bluray read data skip current url");
                c->end_of_segment = 1;
                c->cur_seq_no = v->cur_seq_no;
                last_load_timeUs = av_gettime();
                return ret;
            }else{
               if(c->abort || v->parent->exit_flag){
                     return AVERROR_EOF;
                }
               av_log(NULL, AV_LOG_ERROR,"open_input reload");
                av_usleep(200*1000);
                goto reload;
            }
        }

    }
    last_load_timeUs = av_gettime();
    retry = 15;
	struct RKBLURAYSegment *seg = v->segments[v->cur_seq_no - v->start_seq_no]; //add by xhr, for Bluray
    while(retry--){
    ret = ffurl_read(v->input, buf, buf_size);
    if (ret > 0){
				if(seg->is_seek){
				
				//int64_t pos = avio_tell(&v->pb);
				//int64_t total = avio_size(&v->pb);
				int64_t seek_position = ffurl_seek(v->input, 0, SEEK_CUR);
				//av_log(NULL, AV_LOG_ERROR, "Hery, seek_position = %lld", seek_position);
				//int64_t total = ffurl_size(v->input);
				//av_log(NULL, AV_LOG_ERROR, "Hery, pos = %lld, total = %lld", pos, total);
				if (seek_position  >= seg->seek_time_end){
					av_log(NULL, AV_LOG_ERROR, "Hery, read data break = %lld", seek_position);
					break;
				}
			}
        return ret;
        }
        if(ret == 0){
            break;
        }
        if(c->abort || v->parent->exit_flag){
            av_log(NULL, AV_LOG_ERROR,"ffurl_read ret = %d",ret);
             return AVERROR_EOF;
        }
    }
    ffurl_close(v->input);
    v->input = NULL;
    v->cur_seq_no++;

    c->end_of_segment = 1;
    c->cur_seq_no = v->cur_seq_no;
    read_timeout_cnt = 0;

    if (v->ctx && v->ctx->nb_streams && v->parent->nb_streams >= v->stream_offset + v->ctx->nb_streams) {
        v->needed = 0;
        for (i = v->stream_offset; i < v->stream_offset + v->ctx->nb_streams;
             i++) {
            if (v->parent->streams[i]->discard < AVDISCARD_ALL)
                v->needed = 1;
        }
    }
    if (!v->needed) {
        av_log(v->parent, AV_LOG_ERROR, "No longer receiving variant %d\n",
               v->index);
        return AVERROR_EOF;
    }
    goto restart;
}

static int rkbluray_read_header(AVFormatContext *s)
{
    RKBLURAYContext *c = s->priv_data;
    int ret = 0, i, j, stream_offset = 0,retry = 0;
#if RKBLURAY_DEBUG
    av_log(NULL, AV_LOG_ERROR, "rkbluray_read_header in\n");
#endif
    c->interrupt_callback = &s->interrupt_callback;

loadplaylist:
    if ((ret = rkb_parse_playlist(c, s->filename, NULL, s->pb)) < 0){
        if(retry > 5){
        goto fail;
        }else{
            if(ret == AVERROR_EXIT || s->exit_flag){
                ret = AVERROR_EOF;
                goto fail;
            }
            retry++;
            av_usleep(100*1000);
            goto loadplaylist;
        }
    }

    if (c->n_variants == 0) {
        av_log(NULL, AV_LOG_WARNING, "Empty playlist\n");
        ret = AVERROR_EOF;
        goto fail;
    }
    /* If the playlist only contained variants, parse each individual
     * variant playlist. */
    retry = 0;
loadplaylist1:
    if (c->n_variants > 1 || c->variants[0]->n_segments == 0) {
       // for (i = 0; i < c->n_variants; i++) {
            struct RKBLURAYVariant *v = c->variants[0];
            if ((ret = rkb_parse_playlist(c, v->url, v, NULL)) < 0){
                if(retry > 5){
                goto fail;
                }else{
                    if(ret == AVERROR_EXIT || s->exit_flag){
                        ret = AVERROR_EOF;
                        goto fail;
                    }
                    retry++;
                    av_usleep(100*1000);
                    goto loadplaylist1;
        }
            }
    }

    if (c->variants[0]->n_segments == 0) {
        av_log(NULL, AV_LOG_WARNING, "Empty playlist\n");
        ret = AVERROR_EOF;
        goto fail;
    }

    /* If this isn't a live stream, calculate the total duration of the
     * stream. */
    if (c->variants[0]->finished) {
        double duration = 0;
        for (i = 0; i < c->variants[0]->n_segments; i++)
            duration += c->variants[0]->segments[i]->duration;
        s->duration = duration * AV_TIME_BASE;
    }

    /* Open the demuxer for each variant */
        i = 0;
        struct RKBLURAYVariant *v = c->variants[i];
        AVInputFormat *in_fmt = NULL;
        char bitrate_str[20];

        if (!(v->ctx = avformat_alloc_context())) {
            ret = AVERROR(ENOMEM);
            goto fail;
        }

        v->index  = i;
        v->needed = 1;
        v->parent = s;

        /* If this is a live stream with more than 3 segments, start at the
         * third last segment. */
        if(v->bluray_play_seq_no == 0)
        	v->cur_seq_no = v->start_seq_no;
		else
			v->cur_seq_no = v->bluray_play_seq_no;
        if (!v->finished && v->n_segments > 3)
            v->cur_seq_no = v->start_seq_no + v->n_segments - 3;

        v->read_buffer = av_malloc(INITIAL_BUFFER_SIZE);
		if (strstr(s->filename, "file/m3u8:fd")){
			v->ctx->probesize = 50000000;
		}
        ffio_init_context(&v->pb, v->read_buffer, INITIAL_BUFFER_SIZE, 0, v,
                          rkb_read_data, NULL, NULL);
        v->pb.seekable = 0;
#if RKBLURAY_DEBUG
		av_log(NULL, AV_LOG_ERROR, "rkbluray_read_header in, v->segments[0]->url = %s\n",v->segments[0]->url);
#endif
#if RKBLURAY_DEBUG
		av_log(NULL, AV_LOG_ERROR, "rkbluray_read_header in, v->cur_seq_no%d\n",v->cur_seq_no);
		av_log(NULL, AV_LOG_ERROR, "rkbluray_read_header in, v->segments[v->cur_seq_no]->url = %s\n",v->segments[v->cur_seq_no]->url);
#endif

		
        ret = av_probe_input_buffer(&v->pb, &in_fmt, v->segments[v->cur_seq_no]->url,
                                    NULL, 0, 0);
        if (ret < 0) {
            /* Free the ctx - it isn't initialized properly at this point,
             * so avformat_close_input shouldn't be called. If
             * avformat_open_input fails below, it frees and zeros the
             * context, so it doesn't need any special treatment like this. */
            av_log(s, AV_LOG_ERROR, "Error when loading first segment '%s'\n", v->segments[0]->url);
            avformat_free_context(v->ctx);
            v->ctx = NULL;
            goto fail;
        }
        v->ctx->pb       = &v->pb;

#if RKBLURAY_DEBUG
        av_log(NULL, AV_LOG_ERROR, "avformat_open_input in \n");
#endif
        ret = avformat_open_input(&v->ctx, v->segments[0]->url, in_fmt, NULL);
#if RKBLURAY_DEBUG
        av_log(NULL, AV_LOG_ERROR, "avformat_open_input out \n");
#endif
        if (ret < 0)
            goto fail;

        v->stream_offset = stream_offset;
        v->ctx->ctx_flags &= ~AVFMTCTX_NOHEADER;
        ret = avformat_find_stream_info(v->ctx, NULL);
        if (ret < 0)
            goto fail;
        snprintf(bitrate_str, sizeof(bitrate_str), "%d", v->bandwidth);
        /* Create new AVStreams for each stream in this variant */
        for (j = 0; j < v->ctx->nb_streams; j++) {
            AVStream *st = avformat_new_stream(s, NULL);
            if (!st) {
                ret = AVERROR(ENOMEM);
                goto fail;
            }
            st->id = i;
            avcodec_copy_context(st->codec, v->ctx->streams[j]->codec);
            if (v->bandwidth)
                av_dict_set(&st->metadata, "variant_bitrate", bitrate_str,
                                 0);
        }
        stream_offset += v->ctx->nb_streams;
  //  }

    c->first_packet = 1;
    c->first_timestamp = AV_NOPTS_VALUE;
    c->seek_timestamp  = AV_NOPTS_VALUE;

    return 0;
fail:
    rkb_free_variant_list(c);
    return ret;
}

static int rkb_recheck_discard_flags(AVFormatContext *s, int first)
{
    RKBLURAYContext *c = s->priv_data;
    int i, changed = 0;

    /* Check if any new streams are needed */
    for (i = 0; i < c->n_variants; i++)
        c->variants[i]->cur_needed = 0;

    for (i = 0; i < s->nb_streams; i++) {
        AVStream *st = s->streams[i];
        struct RKBLURAYVariant *var = c->variants[s->streams[i]->id];
        if (st->discard < AVDISCARD_ALL)
            var->cur_needed = 1;
    }
    for (i = 0; i < c->n_variants; i++) {
        struct RKBLURAYVariant *v = c->variants[i];
        if (v->cur_needed && !v->needed) {
            v->needed = 1;
            changed = 1;
            v->cur_seq_no = c->cur_seq_no;
            v->pb.eof_reached = 0;
            av_log(s, AV_LOG_INFO, "Now receiving variant %d\n", i);
        } else if (first && !v->cur_needed && v->needed) {
            if (v->input)
                ffurl_close(v->input);
            v->input = NULL;
            v->needed = 0;
            changed = 1;
            av_log(s, AV_LOG_INFO, "No longer receiving variant %d\n", i);
        }
    }
    return changed;
}

static int rkbluray_read_packet(AVFormatContext *s, AVPacket *pkt)
{
    RKBLURAYContext *c = s->priv_data;
    int ret, i, minvariant = -1;

    if (c->first_packet) {
        rkb_recheck_discard_flags(s, 1);
        c->first_packet = 0;
    }

start:
    c->end_of_segment = 0;
   // for (i = 0; i < c->n_variants; i++) {
        i = 0;
        struct RKBLURAYVariant *var = c->variants[i];
        /* Make sure we've got one buffered packet from each open variant
         * stream */
        if (var->needed && !var->pkt.data) {
            while (1) {
                int64_t ts_diff;
                AVStream *st;
                ret = av_read_frame(var->ctx, &var->pkt);
                if (ret < 0) {
#if RKBLURAY_DEBUG
                    av_log(NULL, AV_LOG_ERROR, "rkbluray_read_packet = %d\n",ret);
#endif
                    if (!url_feof(&var->pb) && ret != AVERROR_EOF){
                        return ret;
                    }
#if RKBLURAY_DEBUG
                    av_log(NULL, AV_LOG_ERROR, "rkbluray_read_packet AVERROR_EOF ret = %d\n",ret);
#endif
                    rkb_reset_packet(&var->pkt);
                    break;
                } else {
                    if (c->first_timestamp == AV_NOPTS_VALUE)
                        c->first_timestamp = var->pkt.dts;
                }

                if (c->seek_timestamp == AV_NOPTS_VALUE)
                    break;

                if (var->pkt.dts == AV_NOPTS_VALUE) {
                    c->seek_timestamp = AV_NOPTS_VALUE;
                    break;
                }

                st = var->ctx->streams[var->pkt.stream_index];
                ts_diff = av_rescale_rnd(var->pkt.dts, AV_TIME_BASE,
                                         st->time_base.den, AV_ROUND_DOWN) -
                          c->seek_timestamp;
                if (ts_diff >= 0 && (c->seek_flags  & AVSEEK_FLAG_ANY ||
                                     var->pkt.flags & AV_PKT_FLAG_KEY)) {
                    c->seek_timestamp = AV_NOPTS_VALUE;
                    break;
                }
            }
        }
        /* Check if this stream has the packet with the lowest dts */
        if (var->pkt.data) {
            if(minvariant < 0) {
                minvariant = i;
            } else {
                struct RKBLURAYVariant *minvar = c->variants[minvariant];
                int64_t dts    =    var->pkt.dts;
                int64_t mindts = minvar->pkt.dts;
                AVStream *st   =    var->ctx->streams[   var->pkt.stream_index];
                AVStream *minst= minvar->ctx->streams[minvar->pkt.stream_index];

                if(   st->start_time != AV_NOPTS_VALUE)    dts -=    st->start_time;
                if(minst->start_time != AV_NOPTS_VALUE) mindts -= minst->start_time;

                if (av_compare_ts(dts, st->time_base, mindts, minst->time_base) < 0)
                    minvariant = i;
            }
        }
   // }
    if (c->end_of_segment) {
        if (rkb_recheck_discard_flags(s, 0)){
            goto start;
         }
    }
    /* If we got a packet, return it */
    if (minvariant >= 0) {
        *pkt = c->variants[minvariant]->pkt;
        pkt->stream_index += c->variants[minvariant]->stream_offset;
        rkb_reset_packet(&c->variants[minvariant]->pkt);
        return 0;
    }

    av_log(NULL, AV_LOG_ERROR, "rk bluray read AVERROR_EOF\n");
    return AVERROR_EOF;
}

static int rkbluray_close(AVFormatContext *s)
{
    RKBLURAYContext *c = s->priv_data;

    rkb_free_variant_list(c);
    return 0;
}

static int rkbluray_read_seek(AVFormatContext *s, int stream_index,
                               int64_t timestamp, int flags)
{
    RKBLURAYContext *c = s->priv_data;
    int i, j, ret;

    if ((flags & AVSEEK_FLAG_BYTE) || !c->variants[0]->finished)
        return AVERROR(ENOSYS);

    c->seek_flags     = flags;
    c->seek_timestamp = stream_index < 0 ? timestamp :
                        av_rescale_rnd(timestamp, AV_TIME_BASE,
                                       s->streams[stream_index]->time_base.den,
                                       flags & AVSEEK_FLAG_BACKWARD ?
                                       AV_ROUND_DOWN : AV_ROUND_UP);
    timestamp = av_rescale_rnd(timestamp, 1, stream_index >= 0 ?
                               s->streams[stream_index]->time_base.den :
                               AV_TIME_BASE, flags & AVSEEK_FLAG_BACKWARD ?
                               AV_ROUND_DOWN : AV_ROUND_UP);
    if (s->duration < c->seek_timestamp) {
        c->seek_timestamp = s->duration;
    }

    ret = AVERROR(EIO);
        /* Reset reading */
        i = 0;
        struct RKBLURAYVariant *var = c->variants[i];
        double pos = c->first_timestamp == AV_NOPTS_VALUE ? 0 :
                      av_rescale_rnd(c->first_timestamp, 1, stream_index >= 0 ?
                               s->streams[stream_index]->time_base.den :
                               AV_TIME_BASE, flags & AVSEEK_FLAG_BACKWARD ?
                               AV_ROUND_DOWN : AV_ROUND_UP);
        double dispos = -1;
        if (var->input) {
            ffurl_close(var->input);
            var->input = NULL;
        }
        av_free_packet(&var->pkt);
        rkb_reset_packet(&var->pkt);
        var->pb.eof_reached = 0;
        /* Clear any buffered data */
        var->pb.buf_end = var->pb.buf_ptr = var->pb.buffer;
        /* Reset the pos, to let the mpegts demuxer know we've seeked. */
        var->pb.pos = 0;

        /* Locate the segment that contains the target timestamp */
        for (j = 0; j < var->n_segments; j++) {
            if(var->segments[j]->discontinuity){
                dispos = pos;
            }
            if (timestamp >= pos &&
                timestamp < pos + var->segments[j]->duration) {
                var->cur_seq_no = var->start_seq_no + j;
				var->segments[j]->seek_operation_time = timestamp - pos;      //add by xhr, for Bluray seek operation
                ret = 0;
                break;
            }
            pos += var->segments[j]->duration;
        }
        if(j == var->n_segments){
              var->cur_seq_no = var->start_seq_no + j;
              ret = 0;
        }
        if (ret)
            c->seek_timestamp = AV_NOPTS_VALUE;
    if(dispos > 0){
#if RKBLURAY_DEBUG
        av_log(NULL, AV_LOG_ERROR, "c->seek_timestamp= %lld,dispos = %lld\n",c->seek_timestamp,dispos*1000000);
#endif
		c->seek_timestamp -= ((int64_t)dispos)*1000000;
#if RKBLURAY_DEBUG
        av_log(NULL, AV_LOG_ERROR, "seek_timestamp after = %lld \n",c->seek_timestamp);
#endif
	}else{
        c->seek_timestamp = AV_NOPTS_VALUE;
    }
    return ret;
}

static int rkbluray_probe(AVProbeData *p)
{
    /* Require #RKBM3U at the start, and either one of the ones below
     * somewhere for a proper match. */
#if RKBLURAY_DEBUG
    av_log(NULL,AV_LOG_ERROR,"rkbluray_probe in\n");
#endif
    if (strncmp(p->buf, "#RKBM3U", 7))
        return 0;
    if (strstr(p->buf, "#RKB-X-STREAM-INF:")     ||
        strstr(p->buf, "#RKB-X-TARGETDURATION:") ||
        strstr(p->buf, "#RKB-X-MEDIA-SEQUENCE:"))
        return AVPROBE_SCORE_MAX;
    return 0;
}

AVInputFormat ff_rkbluray_demuxer = {
    .name           = "rk, bluray",
    .long_name      = NULL_IF_CONFIG_SMALL("Bluray Streaming"),
    .priv_data_size = sizeof(RKBLURAYContext),
    .read_probe     = rkbluray_probe,
    .read_header    = rkbluray_read_header,
    .read_packet    = rkbluray_read_packet,
    .read_close     = rkbluray_close,
    .read_seek      = rkbluray_read_seek,
};
