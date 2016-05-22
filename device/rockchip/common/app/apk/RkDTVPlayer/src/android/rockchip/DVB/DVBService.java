/********************************************************************************************************************/
/**
 *  @skip   $Id:$
 *  @file   DVBService.java
 *  @brief  DVBService JNI interface.
 *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
 *
 *  ALL Rights Reserved, Copyright(C) ROCKCHIP LIMITED 2011
 */
/********************************************************************************************************************/
package android.rockchip.DVB;

import android.graphics.Bitmap;

public class DVBService {
	static {
		System.loadLibrary("rockchip_dvb_jni");
	}
	
	public native int DVBDeviceInit_native();

	public native int DVBDeviceDeInit_native();

	public native int getSignalStrength_native();

	public native int DVBScan_native(int mode, int freq, int bw, int freq_index);

	public native void DVBCancelScan_native();

	public native int DVBGetFreqCount_native();

	public native int DVBGetFreqPoint_native(int i);

	public native int DVBGetServiceCount_native(int freq);
	
    public native DVBServiceInfor DVBGetServiceInfor_native(int freq,int i);

    public native boolean setServiceCollect_native(int freq, int serviceId, int collect);

    public native String DVBGetEitInfo_native(int i);
	
    public native byte[] DVBGetEitInfoByteArray_native(int i);	

    public native int DVBGetEitCount_native();

    public native int getAudioPID_native();

    public native int getVideoPID_native();

    public native int DVBPlayService_native(int freq,int serviceId, int audio_index);
//    public int DVBPlayService_native1(int freq,int serviceId){
//        for(int i=0; i<10000; i++){
//            Logger.e("i:"+i);
//        }
//        return 0;
//    }
    
    public native int DVBStopService_native();

	public native int DVBRecordProgram_native(int freq, int serviceId);
	public native int DVBStopRecordProgram_native();
    public native void setDVBMode_native(int mode);

    public native int getDVBMode_native();
    
    public native int esgScan_native(int freq);

    public native void setZoneTime_native(int zoneTime_minute);	
    
    public native String getDailySchedule_native(int freq,int serviceId,int day ,int index);
	
    public native byte[] getDailyScheduleByteArray_native(int freq,int serviceId,int day ,int index);	

    public native int getDailyScheduleCount_native(int freq,int serviceId,int day);
    
    public String getDailySchedule_native1(int freq,int serviceId,int day ,int index){
        return "xxxxxxxxxx";
    }

    public int getDailyScheduleCount_native1(int freq,int serviceId,int day){
        return 10;
    }
    public native int getCurrentWeekDay_native(); // 获取当天是星期几。  返回值：  1-- 7；若为-1说明还没有解出来， 需等待。

    public native int getDefalltAudioIndex_native(int freq,int serviceId); 
    public native int getDefaultSubtitleIndex_native(int freq,int serviceId); 	
    
    public native int subtitlePlay_native(int subtitleIndex);  //播放指定subtitle索引的字幕，成功返回0，失败返回-1;
    public native int subtitleStop_native(int stopOption);    //stopOption 0: close normal,   stopOption -1: close and set subtitleIndex to -1
    public native int subtitleGetDispData_native(Bitmap bitmap);  //返回数据大小
    public native String subtitleGetInfo_native(int i);  //获取某个subtitle信息
    public native int subtitleGetTotalNum_native();   //获取当前播放节目的subtitle数目
    public native int subtitleIsNeedDisp_native();   //0不需要更新，1需要更新
    
    public native int teletextPlay_native(int teletextIndex);
    public native int teletextStop_native();
    public native String teletextGetInfo_native(int i);
    public native int teletextGetPageByDirection_native(int find_page_direction, Bitmap bitmap);
    public native int teletextGetPageByNum_native(int find_page_direction, Bitmap bitmap);
    public native int teletextGetTotalNum_native();
    
    public native String audioGetInfo_native(int i);  //获取当前节目某个audio信息
    public native int audioGetTotalNum_native();   //获取当前播放节目的audio数目
}
