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

public class BcshSatConPreference extends SeekBarDialogPreference implements
        SeekBar.OnSeekBarChangeListener, CheckBox.OnCheckedChangeListener, View.OnKeyListener{

    private static final String TAG = "BcshSatConPreference";
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
    public BcshSatConPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
		setPositiveButtonText(null);
        this.context = context;
        setDialogLayoutResource(R.layout.preference_dialog_brightness);
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
    String satcon = SystemProperties.get("persist.sys.bcsh.satcon");
	if(TextUtils.isEmpty(satcon))
		{
			mSeekBar.setProgress(256);
		}else{
			int oldvalue = Integer.parseInt(satcon);
			mSeekBar.setProgress(oldvalue);
			
		}
    mSeekBar.setOnSeekBarChangeListener(this);
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
		mDisplayManagement.setSat_con(mDisplayManagement.MAIN_DISPLAY,progress*2);
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

