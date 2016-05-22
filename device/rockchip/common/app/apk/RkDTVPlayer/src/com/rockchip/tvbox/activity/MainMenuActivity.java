/********************************************************************************************************************/
/**
 *  @skip   $Id:$
 *  @file   MainMenuActivity.java
 *  @brief  3D gallery main menu activity.
 *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
 *
 *  ALL Rights Reserved, Copyright(C) ROCKCHIP LIMITED 2011
 */
/********************************************************************************************************************/

/** @addtogroup MAIN_MENU_ACTIVITY
 * @{ */

package com.rockchip.tvbox.activity;

import com.rockchip.tvbox.adapter.GalleryFlowAdapter;
import com.rockchip.tvbox.provider.TVProgram.Programs;
import com.rockchip.tvbox.utils.CommonStaticData;
import com.rockchip.tvbox.utils.ExcuteScan;
import com.rockchip.tvbox.utils.Logger;
import com.rockchip.tvbox.view.GalleryFlow;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import android.os.Looper;
import java.util.Calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.util.Log;
import android.widget.Toast;

public class MainMenuActivity extends Activity {
    /** Called when the activity is first created. */
    TextView detailTxtView;

    /***************************************************************************************************************/
    /*!
     *  @brief  Called when the activity is first created.
     *  @note   This function treats following :\n
     *              - Set full screen.
     *              - Get configures from share preferences.
     *              - Call jniIF.DVBDeviceInit_native() to initial DVB device.
     *              - Set Gallery property and register OnItemClickListener.
     *              - Process gallery click event associated with the selected item.
     *  @param  savedInstanceState      [in] Bundle
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* set full screen */
        CommonStaticData.setFullScreen(this);
        
        CommonStaticData.settings = getSharedPreferences(CommonStaticData.mSharedPreferencesName, 0);

        CommonStaticData.bolHasChannel = CommonStaticData.settings.getBoolean(CommonStaticData.hasChannelKey, false);
        CommonStaticData.serviceTVNum = CommonStaticData.settings.getInt(CommonStaticData.serviceTVNumKey, 0);
        CommonStaticData.serviceRadioNum = CommonStaticData.settings.getInt(CommonStaticData.serviceRadioNumKey, 0);
        
        setContentView(R.layout.galleryflow);
        CommonStaticData.sysTm.setToNow();
        int dvbFrontEndMode = CommonStaticData.jniIF.DVBDeviceInit_native();	
		    CommonStaticData.jniIF.setZoneTime_native( getTimeZoneMinute() );  
	Runnable mRunnable = new Runnable()
	{
		public void run()
		{
			Looper.prepare();
			 
                   // int dvbFrontEndMode = CommonStaticData.jniIF.DVBDeviceInit_native();	
		      //CommonStaticData.jniIF.setZoneTime_native( getTimeZoneMinute() );  
		}	
	};
	Thread mThread = new Thread(mRunnable);
       mThread.start(); 
        /*TBD*/
        /*
        ((TextView) (this.findViewById(R.id.topbar))).setText(""
                + getResources().getStringArray(R.array.weekday)[CommonStaticData.sysTm.weekDay] + " "
                + getResources().getStringArray(R.array.month)[CommonStaticData.sysTm.month] + " " + CommonStaticData.sysTm.monthDay
                + " " + CommonStaticData.sysTm.hour + ":" + CommonStaticData.sysTm.minute);
         */
        Integer[] images = {
                R.drawable.tv, R.drawable.radio, R.drawable.favorite,  R.drawable.tools
        };
        /*R.drawable.aerial,
        R.drawable.epg,*/
        final GalleryFlowAdapter adapter = new GalleryFlowAdapter(this, images);
        adapter.createReflectedImages();

        GalleryFlow galleryFlow = (GalleryFlow) this.findViewById(R.id.gallery);
        galleryFlow.setFadingEdgeLength(0);
        galleryFlow.setSpacing(-50);
        galleryFlow.setAnimationDuration(1000);
        galleryFlow.setAdapter(adapter);

        galleryFlow.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Toast.makeText(getApplicationContext(),
                // String.valueOf(position), Toast.LENGTH_SHORT).show();
                switch (position) {
                    case CommonStaticData.MENU_ID_TV:
                    case CommonStaticData.MENU_ID_RADIO:
                    case CommonStaticData.MENU_ID_FAVORITE:
                        if(!CommonStaticData.bolHasChannel){
//                            buildChannelScanDialog(position);
                            ExcuteScan excuteScan = new ExcuteScan(MainMenuActivity.this, position);
                            excuteScan.buildChannelScanDialog();
                        }
                        else{
                            Intent intent = new Intent();
                            intent.putExtra("menu_id", position);
                            intent.setClass(MainMenuActivity.this, ServiceListActivity.class);
                            startActivity(intent);
                        }
                        break;
                        
                    case CommonStaticData.MENU_ID_SEARCH:
                        //buildChannelScanDialog(position);
                        break;
                        
                    case CommonStaticData.MENU_ID_EPG:
                        if(!CommonStaticData.bolHasChannel){
                            //buildChannelScanDialog(position);
                        }
                        else{
                            Intent intent = new Intent(null, Programs.CONTENT_URI,
                                    MainMenuActivity.this, EPGActivity.class);
                            startActivityForResult(intent , 0);
                        }
                        break;
                    case CommonStaticData.MENU_ID_SETUP:
                        Intent intent = new Intent();
                        intent.setClass(MainMenuActivity.this, SettingPreferences.class);
                        startActivity(intent);
                        break;
                }
            }

        });
        galleryFlow.setSelection(0);

        detailTxtView = (TextView) this.findViewById(R.id.txtViewDetail);

        galleryFlow.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                detailTxtView.setText(getResources().getStringArray(R.array.array_menu)[arg2]);

                for (int imageId = 0; imageId < adapter.getCount(); imageId++) {
                    if(imageId != arg2){
                        (adapter.mImages[imageId]).setAlpha(100);
                    }
                    else{
                        (adapter.mImages[imageId]).setAlpha(255);
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                // System.out.println("+++++++++++onNothingSelected+++++++++++++");

            }

        });
//        Logger.e("before set:"+System.currentTimeMillis());
//        
//        Calendar c = Calendar.getInstance();
//
//        c.set(Calendar.HOUR_OF_DAY, 10);
//        c.set(Calendar.MINUTE, 10);
//        long when = c.getTimeInMillis();
//        Logger.e("set:"+when);
//        
//        if (when / 1000 < Integer.MAX_VALUE) {
//            SystemClock.setCurrentTimeMillis(when);
//        }
//        
//        Logger.e("after set:"+System.currentTimeMillis());
        galleryFlow.performItemClick(galleryFlow, CommonStaticData.MENU_ID_TV, 0);
    }
	
    /*  Helper routines to format timezone */
    private int getTimeZoneMinute() {
        TimeZone    tz = java.util.Calendar.getInstance().getTimeZone();
        boolean daylight = tz.inDaylightTime(new Date());

        int offset_minute = (tz.getRawOffset() + (daylight ? tz.getDSTSavings() : 0))/(1000*60);

    	Log.e("EPG", "getTimeZoneMinute" + offset_minute);		
        
        return offset_minute; 
    }   
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonStaticData.jniIF.DVBDeviceDeInit_native();
    }
}
/* @} */
