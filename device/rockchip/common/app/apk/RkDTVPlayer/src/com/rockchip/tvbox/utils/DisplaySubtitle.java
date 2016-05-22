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

import com.rockchip.tvbox.activity.VideoPlayerActivity;


public class DisplaySubtitle extends Thread//implements Runnable
{
    VideoPlayerActivity vpa;
    int time_cnt = 0;
	
    public DisplaySubtitle(VideoPlayerActivity vpa)
    {
        this.vpa = vpa;
    }
    
    public void run() 
    {
        while(!vpa.threadSuspended){
            synchronized(this) {
//                Logger.e("subtitleIsNeedDisp_native:"+CommonStaticData.jniIF.subtitleIsNeedDisp_native());
                if(vpa.encrypt != 1){
                    if(!vpa.bolStopSubtitle && CommonStaticData.subtitleCheckedItem != 0 && vpa.bolDispCmp
    //                        ){
                            && CommonStaticData.jniIF.subtitleIsNeedDisp_native() == 1 && !vpa.isTeletxtShow){
    //                    Logger.e("UPDATE_SUBTITLE");
                        vpa.bolDispCmp = false;
                        vpa.myHandler.sendEmptyMessage(vpa.UPDATE_SUBTITLE);
                        vpa.myHandler.removeMessages(vpa.CLEAR_SUBTITLE);
                        vpa.myHandler.sendEmptyMessageDelayed(vpa.CLEAR_SUBTITLE, vpa.SUB_TIME);
                    }
	                if((time_cnt++ % 20) == 0)
	                {
	                    if(!vpa.bolNoSignal && CommonStaticData.jniIF.getSignalStrength_native() < 15){
	//                    if(!vpa.bolNoSignal && CommonStaticData.jniIF.getSignalStrength_native1()%500 < 250){
	                        vpa.myHandler.sendEmptyMessage(vpa.NO_SIGNAL_DISP);
	                    }
	                    else if(vpa.bolNoSignal && CommonStaticData.jniIF.getSignalStrength_native() >= 15){
	//                    else if(vpa.bolNoSignal && CommonStaticData.jniIF.getSignalStrength_native1()%500 > 250){
	                        vpa.myHandler.sendEmptyMessage(vpa.NO_SIGNAL_DISMISS);
	                    }
	                }
                }
                try
                {
                    Thread.sleep(50);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
//                vpa.refurbishSubtitle();
            }
        }
    }
}
    
