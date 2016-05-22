/********************************************************************************************************************/
/**
 *  @skip   $Id:$
 *  @file   ScanPrograms.java
 *  @brief  Programs Scan thread.
 *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
 *
 *  ALL Rights Reserved, Copyright(C) ROCKCHIP LIMITED 2011
 */
/********************************************************************************************************************/
package com.rockchip.tvbox.utils;

import com.rockchip.tvbox.activity.ServiceListActivity;
import com.rockchip.tvbox.provider.TVProgram.Programs;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.Message;


public class ScanPrograms extends Thread//implements Runnable
{
	ServiceListActivity tvp;
	String msgStr[];
	public ScanPrograms(ServiceListActivity tvp)
	{
		this.tvp = tvp;
		CommonStaticData.serviceTVNum = 0;
		CommonStaticData.serviceRadioNum = 0;
		msgStr = new String[2];
	}
	
    public void run() 
    {
//        while(CommonStaticData.bolScanning){
//            ;
//        }
//        CommonStaticData.bolScanning = true;
        ContentValues values = new ContentValues();
        int totalFreqCnt = 1;
        int freqPointValue = 0;
	 int freqLNB = 0;
        int bandWidthValue = 0;
        int totalServiceCnt = 0;
		int freqIndex = 0;
        if(CommonStaticData.scanMode == 1){
            totalFreqCnt = CommonStaticData.totalFreqCount;
        }
        tvp.scanProgressBar.setMax(totalFreqCnt);
        for (freqIndex = 0; freqIndex < totalFreqCnt; freqIndex++){
            if(tvp.threadSuspended){
                this.tvp = null;
                return;
            }
            if(CommonStaticData.scanMode == 1){
                freqPointValue = CommonStaticData.freqPointArrayList.get(freqIndex);
                bandWidthValue = CommonStaticData.bandWidthArrayList.get(freqIndex);
            }
            else{
                freqPointValue = CommonStaticData.scanFreq;
		  freqLNB = CommonStaticData.scanLNBFreq;
                bandWidthValue = CommonStaticData.scanBandWidth;
	     }
		int mode = CommonStaticData.jniIF.getDVBMode_native();
		if(mode == CommonStaticData.DVB_FE_TYPE_QPSK){
		        int pol_mode = CommonStaticData.polMode; //0:VER  1:HOR   
		        if(pol_mode == 0) {
		            bandWidthValue = (bandWidthValue & 0xFFFFFFFE);    	
		        }
		        else {
		            bandWidthValue = (bandWidthValue & 0xFFFFFFFE)| 0x01;  
		        }
		        int s22k_mode = CommonStaticData.s22kMode; //0:on 1:off  
		        if(pol_mode == 0) {
		            bandWidthValue = (bandWidthValue & 0xFFFFFFFD);    	
		        }
		        else {
		            bandWidthValue = (bandWidthValue & 0xFFFFFFFD)| 0x02;  
		        }				

		  msgStr[0] = "Download Frequency: "+freqPointValue+"MHz" + "LNB Frequency: "+freqLNB+"MHz" ;
		  msgStr[1] = "  "+"Symbol Rate: "+bandWidthValue+"KHz";	
		  //freqPointValue = (freqPointValue - freqLNB)*1000;//the freq KHz which input to demodulator IC
		  if(freqPointValue > freqLNB)
		  {
		  	freqPointValue = (freqPointValue - freqLNB)*1000;//the freq KHz which input to demodulator IC
		  }
		  else
		  {
		  	freqPointValue = (freqLNB - freqPointValue)*1000;//the freq KHz which input to demodulator IC
		  }
		}
		else {
		    msgStr[0] = "Scanning Frequency: "+(freqPointValue/1000)+"."+(freqPointValue%1000)+"MHz";
		    msgStr[1] = "  "+" BW: "+bandWidthValue+"KHz";
		}

            Message msg = tvp.myHandler.obtainMessage(tvp.MSG_UPDATE_SCAN_FREQ, msgStr);
            tvp.myHandler.sendMessage(msg);
//            tvp.scanFreqText.setText("Scanning Frequency:"+freqPointValue+"MHz");
            if(CommonStaticData.bolScanLCN){
                CommonStaticData.scanMode += 100;
            }
            int serviceCount = CommonStaticData.jniIF.DVBScan_native(CommonStaticData.scanMode,
                    freqPointValue, bandWidthValue, freqIndex);

            if(serviceCount > 0)
            {
                totalServiceCnt += serviceCount;
            }
            for (int serviceIndex = 0; serviceIndex < serviceCount; serviceIndex++) {
                if(tvp.threadSuspended){
                    this.tvp = null;
                    return;
                }
                int Service_ID = (CommonStaticData.jniIF
                        .DVBGetServiceInfor_native(freqPointValue, serviceIndex))
                        .getServiceId();
                values.put(Programs.SERVICEID, Service_ID);
                
                //String Service_Name = (CommonStaticData.jniIF
                //        .DVBGetServiceInfor_native(freqPointValue, serviceIndex))
                //        .getName();
                String Service_Name = CommonStaticData.FormatToGbkByte((CommonStaticData.jniIF.DVBGetServiceInfor_native(freqPointValue, serviceIndex)).getNameArray(), 0);                        
                Logger.d("Service_Name:" + Service_Name);
                values.put(Programs.SERVICENAME, Service_Name);

                int Service_Freq = freqPointValue;
                values.put(Programs.FREQ, Service_Freq);

                int Service_BW = bandWidthValue;
                values.put(Programs.BW, Service_BW);
                
                int Service_Type = (CommonStaticData.jniIF
                        .DVBGetServiceInfor_native(freqPointValue, serviceIndex))
                        .getType();
                values.put(Programs.TYPE, Service_Type);
                if(Service_Type == Integer.parseInt(CommonStaticData.SERVICE_TYPE_TV)){
                    CommonStaticData.serviceTVNum ++;
                }
                else if(Service_Type == Integer.parseInt(CommonStaticData.SERVICE_TYPE_RADIO)){
                    CommonStaticData.serviceRadioNum ++;
                }
                values.put(Programs.FAV, 0);
                
                int Service_Encrypt = (CommonStaticData.jniIF
                        .DVBGetServiceInfor_native(freqPointValue, serviceIndex))
                        .getEncrypt();
                //Service_Encrypt = serviceIndex%2;/*TBD*/
                values.put(Programs.ENCRYPT, Service_Encrypt);
                
                int Service_LCN = (CommonStaticData.jniIF
                        .DVBGetServiceInfor_native(freqPointValue, serviceIndex))
                        .getLCN();
                values.put(Programs.LCN, Service_LCN);
//                if(CommonStaticData.bolCancelScan){
//                    CommonStaticData.bolCancelScan = false;
//                    CommonStaticData.bolScanning = false;
//                    tvp.getContentResolver().delete(tvp.mUri, null, null);
//                    return;
//                }else{
                    tvp.getContentResolver().insert(tvp.mUri, values);
                    CommonStaticData.bolHasChannel = true;
//                }
                msgStr[0] = "DTV:"+CommonStaticData.serviceTVNum;
                msgStr[1] = "Radio:"+CommonStaticData.serviceRadioNum;
                msg = tvp.myHandler.obtainMessage(tvp.MSG_UPDATE_SCAN_RESULT, msgStr);
                tvp.myHandler.sendMessage(msg);
                try{
                    sleep(100);
                }catch(Exception e){
                    
                }
            }
//            tvp.myHandler.sendEmptyMessage(tvp.MSG_REMOVE_SCAN_VIEW);
            tvp.scanProgressBar.setProgress(freqIndex);
        }
        
        if (totalServiceCnt > 0) {
//            CommonStaticData.bolHasChannel = true;
            SharedPreferences.Editor editor = CommonStaticData.settings.edit();
            editor.putBoolean(CommonStaticData.hasChannelKey, true);
            editor.putInt(CommonStaticData.serviceTVNumKey, CommonStaticData.serviceTVNum);
            editor.putInt(CommonStaticData.serviceRadioNumKey, CommonStaticData.serviceRadioNum);
            editor.commit();
        }
        else{
            CommonStaticData.bolHasChannel = false;
            tvp.myHandler.sendEmptyMessage(tvp.MSG_NO_SERVICE);
        }
        
        tvp.myHandler.sendEmptyMessage(tvp.MSG_REMOVE_SCAN_VIEW);
//        CommonStaticData.bolScanning = false;
    }
}
    
