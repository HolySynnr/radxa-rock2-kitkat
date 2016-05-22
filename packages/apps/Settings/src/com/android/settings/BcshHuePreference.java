package com.android.settings;
import android.util.Log;
import android.content.Context;
import android.os.RemoteException;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.preference.SeekBarPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SeekBarDialogPreference;
import android.os.SystemProperties;
import java.util.Map;
import android.view.KeyEvent;

import android.text.*;
import java.io.*;
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.io.FileReader;
import java.io.FileWriter;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.DisplayOutputManager;

public class BcshHuePreference extends SeekBarDialogPreference implements
        SeekBar.OnSeekBarChangeListener, CheckBox.OnCheckedChangeListener, View.OnKeyListener{

    private static final String TAG = "BcshHuePreference";
    private static final int MINIMUN_SCREEN_SCALE = 0;
    private static final int MAXIMUN_SCREEN_SCALE = 20;
	
    private SeekBar mSeekBar;
    private int     mOldScale = 0;
    private int     mValue = 0;
    private int     mRestoreValue = 0;
    private boolean mFlag  = false;
	//for save hdmi config
    private	Context	context;
	public final int MAIN_DISPLAY = 0;
	private DisplayOutputManager mDisplayManagement = null;
    public BcshHuePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
		setPositiveButtonText(null);
        //setNegativeButtonText(null);
        this.context = context;
        setDialogLayoutResource(R.layout.preference_dialog_hue);
	    try {
        	mDisplayManagement = new DisplayOutputManager();
        }catch (RemoteException doe) {
            
        }
        
        int[] main_display = mDisplayManagement.getIfaceList(mDisplayManagement.MAIN_DISPLAY);
        if(main_display == null)	{
        	Log.e(TAG, "Can not get main display interface list");
        	return;
        }
       // setDialogIcon(R.drawable.ic_settings_screen_scale);

    }



    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
		
		mFlag = false;
	        mSeekBar = getSeekBar(view);
		//resotre value
		int oldvalue = 0;
	    String hue = SystemProperties.get("persist.sys.bcsh.hue");
		if(TextUtils.isEmpty(hue))
		{
			mSeekBar.setProgress(300);
		}else{
			oldvalue = Integer.parseInt(hue);
			mSeekBar.setProgress(oldvalue);
		}
		Log.d(TAG,"resotre value"+oldvalue);
	    mSeekBar.setOnSeekBarChangeListener(this);
    }
	/*
	+		a:[-30~0]:
	+			sin_hue = sin(a)*256 +0x100;
	+			cos_hue = cos(a)*256;
	+		a:[0~30]
	+			sin_hue = sin(a)*256;
	+			cos_hue = cos(a)*256;
	*/
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
        int sin_hue =0;
		int cos_hue = 0;
        if(progress >=0 && progress <= 300)
        {
        	sin_hue = (int)(Math.sin((float)progress/10-30)*256) + 0x100;
			cos_hue = (int)(Math.cos((float)progress/10-30)*256);
		}else
		{
			sin_hue = (int)(Math.sin((float)progress/10-30)*256);
			cos_hue = (int)(Math.cos((float)progress/10-30)*256);
		}
		SystemProperties.set("persist.sys.bcsh.hue",String.valueOf(progress));
		mDisplayManagement.setHue(mDisplayManagement.MAIN_DISPLAY,sin_hue,cos_hue);
		Log.d(TAG,"*******************sinhue = "+sin_hue+" coshue = "+cos_hue);
     }

    public void onStartTrackingTouch(SeekBar seekBar) {
	//If start tracking, record the initial position
	mFlag = true;
	mRestoreValue = seekBar.getProgress();
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){

    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
		//for save config
         }
   }

