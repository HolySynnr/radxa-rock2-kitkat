/********************************************************************************************************************/
/**
 *  @skip   $Id:$
 *  @file   EPGScan.java
 *  @brief  EPGScan thread.
 *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
 *
 *  ALL Rights Reserved, Copyright(C) ROCKCHIP LIMITED 2011
 */
/********************************************************************************************************************/
package com.rockchip.tvbox.utils;

import com.rockchip.tvbox.activity.EPGActivity;


public class EPGScan extends Thread//implements Runnable
{
    EPGActivity ea;
    int cnt = 0;	
    public EPGScan(EPGActivity ea)
    {
        this.ea = ea;
	cnt = 0;	
    }
    
    public void run() 
    {
        while(!ea.threadSuspended){
            synchronized(this) {
			if(cnt++ % 20 == 1)
			{
                        ea.myHandler.sendEmptyMessage(ea.MSG_EPG_SCAN_REFRESH);
			 	
	                try{
	                    Thread.sleep(100);
	                }catch(Exception e){
                    
                      }
		     }
            }
            
        }
    }
}
    
