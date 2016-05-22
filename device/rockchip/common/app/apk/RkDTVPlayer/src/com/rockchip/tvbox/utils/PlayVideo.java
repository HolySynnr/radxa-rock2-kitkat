/********************************************************************************************************************/
/**
 *  @skip   $Id:$
 *  @file   PlayVideo.java
 *  @brief  Video play thread.
 *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
 *
 *  ALL Rights Reserved, Copyright(C) ROCKCHIP LIMITED 2011
 */
/********************************************************************************************************************/
package com.rockchip.tvbox.utils;

import com.rockchip.tvbox.view.VideoView;

import android.net.Uri;

public class PlayVideo extends Thread//implements Runnable
{
    VideoView vv;
    int freq;
    int serviceId;
    int audioId;
    Uri uri;
    public PlayVideo(VideoView vv,int freq,int serviceId,int audioId)
    {
        Logger.e("==============playVideo================");
        uri = Uri.parse(CommonStaticData.DVB_VIDEO_PATH); 
        this.vv = vv;
        this.freq = freq;
        this.serviceId = serviceId;
        this.audioId = audioId;
    }
    
    public void run() 
    {
        synchronized(this) {
            CommonStaticData.bufferId++;
            if(uri!=null){
                while(vv.bolProcessing){
                    if(CommonStaticData.bufferId > 2){
                        CommonStaticData.bufferId--;
                        return;
                    }
                }
                vv.bolProcessing = true;
               vv.stopPlay();
                try
                {
                    Thread.sleep(1);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }                
                Logger.e("serviceId:"+serviceId);
               vv.setVideoURI(uri, freq, serviceId, audioId);
                vv.bolProcessing = false;
                
            }
        }
    }
}
    
