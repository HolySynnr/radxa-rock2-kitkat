/********************************************************************************************************************/
/**
 *  @skip   $Id:$
 *  @file   CommonStaticData.java
 *  @brief  Common Static Data define.
 *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
 *
 *  ALL Rights Reserved, Copyright(C) ROCKCHIP LIMITED 2011
 */
/********************************************************************************************************************/
package com.rockchip.tvbox.utils;

import com.rockchip.tvbox.provider.TVProgram.Programs;

import android.app.Activity;
import android.content.SharedPreferences;
import android.rockchip.DVB.DVBService;
import android.text.format.Time;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import  java.io.UnsupportedEncodingException;



public class CommonStaticData {
    public static Time sysTm = new Time();
    
    public static ArrayList<Integer> freqPointArrayList = new ArrayList<Integer>();
    public static ArrayList<Integer> bandWidthArrayList = new ArrayList<Integer>();
    public static int totalFreqCount = 56;//56
    public static int scanMode = 0;
    public static int scanFreq = 0;
    public static int scanLNBFreq = 0;	
    public static int scanBandWidth = 0;
    
    public static int symRate = 0;
    public static int polMode = 0;
    public static int s22kMode = 0;	
//    public static boolean bolScanning = false;
//    public static boolean bolCancelScan = false; 
    
    public static SharedPreferences settings = null;
    
    public static boolean bolEPGScan = true;//false;
    public static int bufferId = 0;
    /**************************PREF KEY*********************************/
    public static final String mSharedPreferencesName = "GlobalSettings";
    public static final String hasChannelKey = "hasChannel";
    public static final String scrSizeModeKey = "scrSizeMode";
    public static final String serviceTVNumKey = "serviceTVNum";
    public static final String serviceRadioNumKey = "serviceRadioNum";
    
    public static final String areaSetKey = "areaSet";
    public static final String orderSetKey = "orderSet";
    public static final String timeZoneSetKey = "timeZone";
    public static final String scanChannelsKey = "scanChannels";
    /*******************************************************************/
    
//    public final static int BASE_FREQ = 473143;
    
    /* Menu ID */
    public final static byte MENU_ID_TV = 0;

    public final static byte MENU_ID_RADIO = 1;

    public final static byte MENU_ID_FAVORITE = 2;

    public final static byte MENU_ID_SEARCH = 4;

    public final static byte MENU_ID_EPG = 5;

    public final static byte MENU_ID_SETUP = 3;
    /*
     * Service Type
     */
    public final static String SERVICE_TYPE_ALL = "0"; /*  all services */
    public final static String SERVICE_TYPE_TV    = "1"; /*  TV services */
    public final static String SERVICE_TYPE_RADIO = "2"; /*  radio services */
    public final static String SERVICE_TYPE_OTHER = "4"; /*  data/other services */
    
    public final static String[] selectionArgsTV = new String[] {
        SERVICE_TYPE_TV
    };
    public final static String[] selectionArgsRadio = new String[] {
        SERVICE_TYPE_RADIO
    };
    public final static String[] selectionArgsFav = new String[] {
        "1"
    };
        
    /*
     * Service Number
     */
    public static int serviceTVNum = 0;
    public static int serviceRadioNum = 0;
    /**
     * The columns we are interested in from the database
     */
    public static final String[] PROJECTION = new String[] {
            Programs._ID, // 0
            Programs.SERVICEID, // 1
            Programs.SERVICENAME, // 2
            Programs.FREQ, // 3
            Programs.BW, // 4
            Programs.TYPE, //5
            Programs.FAV, //6
            Programs.ENCRYPT, //7
            Programs.LCN, //8
    };
    
    /**
     * The columns index in the database
     */
    public static final int COLUMN_INDEX_SERVICE_ID = 1;
    public static final int COLUMN_INDEX_SERVICE_NAME = 2;
    public static final int COLUMN_INDEX_SERVICE_FREQ = 3;
    public static final int COLUMN_INDEX_SERVICE_BW = 4;
    public static final int COLUMN_INDEX_SERVICE_TYPE = 5;
    public static final int COLUMN_INDEX_SERVICE_FAV = 6;
    public static final int COLUMN_INDEX_SERVICE_ENCRYPT = 7;
    public static final int COLUMN_INDEX_SERVICE_LCN = 8;

	public static final int DVB_FE_TYPE_QPSK = 0;
	public static final int DVB_FE_TYPE_QAM = 1;
	public static final int DVB_FE_TYPE_DVBT =2;
	public static final int DVB_FE_TYPE_ATSC = 3;
	public static final int DVB_FE_TYPE_ISDB_ONESEG = 4;
	public static final int DVB_FE_TYPE_ISDB_FULLSEG = 5; 	
    
    public static boolean bolHasChannel = false;
    public static DVBService jniIF = new DVBService();
    public static boolean bolScanLCN = false;
    /* set full screen */
    public static void setFullScreen(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);

        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // activity.getWindow().setFlags(WindowManager.LayoutParams.TYPE_STATUS_BAR,
        // WindowManager.LayoutParams.TYPE_STATUS_BAR);
    }

    public static String FormatToISO8859Str(byte data){
		String IsoStant = null;
		
		switch(data){
			case 0x01:
				IsoStant =  "ISO-8859-1";
				break;
			case 0x02:
				IsoStant =  "ISO-8859-2";
				break;
			case 0x03:
				IsoStant =  "ISO-8859-3";
				break;
			case 0x04:
				IsoStant =  "ISO-8859-4";
				break;
			case 0x05:
				IsoStant =  "ISO-8859-5";
				break;
			case 0x06:
				IsoStant =  "ISO-8859-6";
				break;				
			case 0x07:
				IsoStant =  "ISO-8859-7";
				break;
			case 0x08:
				IsoStant =  "ISO-8859-8";
				break;
			case 0x09:
				IsoStant =  "ISO-8859-9";
				break;
			case 0x0a:
				IsoStant =  "ISO-8859-10";
				break;
			case 0x0b:
				IsoStant =  "ISO-8859-11";
				break;
			case 0x0c:
				IsoStant =  "ISO-8859-1";
				break;
			case 0x0d:
				IsoStant =  "ISO-8859-13";
				break;
			case 0x0e:
				IsoStant =  "ISO-8859-14";
				break;
			case 0x0f:
				IsoStant =  "ISO-8859-15";
				break;
			
			default:
				IsoStant =  "ISO-8859-1";
				break;
			}
		return IsoStant;
	}
	public static String FormatToGbkByte(byte[] bytes, int startByte){
		// ISO/IEC 8859-15(Brazil)
		// JIS 8bit character code(Japan)
		try{
			byte[] data = bytes;
			String isoS;

		    int mode = CommonStaticData.jniIF.getDVBMode_native();
            if(mode == CommonStaticData.DVB_FE_TYPE_DVBT){
				isoS = FormatToISO8859Str(data[startByte]);
            }else {
                isoS = "Shift_JIS";			
                //isoS = "BIG5";
                //isoS = "ISO-8859-1";
				//isoS = FormatToISO8859Str(data[startByte]);
            }

			return new String(data,isoS);
		}catch(UnsupportedEncodingException e){
		
			return null;
		}
	}	
    
//  public static final String DVB_VIDEO_PATH = "/sdcard/demo_dtv.mp4";
//    public static final String DVB_VIDEO_PATH = "/system/etc/dtv/demo_dtv.mp4";
    public static final String DVB_VIDEO_PATH = "/system/etc/dtv/ROCKCHIP.TV";
   //public static final String DVB_VIDEO_PATH = "DVBTV://";
    public static int subtitleNum = 0;
    public static int subtitleCheckedItem = 0;
    
    public static int teletxtNum = 0;
    public static int teletxtCheckedItem = 0;
    
    public static int audioNum = 0;
    public static int audioCheckedItem = 0;
    
    public static final int subW = 720;
    public static final int subH = 576;
    
    public static final int teletxtW = 720;
    public static final int teletxtH = 576;
}
