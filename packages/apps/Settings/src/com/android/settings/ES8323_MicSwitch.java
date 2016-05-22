
package com.android.settings;

import com.android.server.AudioCommon;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;
import java.io.RandomAccessFile;
public class ES8323_MicSwitch extends PreferenceActivity {

	private static final String TAG = ES8323_MicSwitch .class.getSimpleName();
	private static final boolean DEBUG = true;
	private PreferenceGroup mMicSwitchMode_List;
    private static final String ES8323_MIC_MODE_PATH = "/sys/class/es8323/mic_state/mic_state";
	private static final String AUTO_SWITCH_KEY = "0";
	private static final String ONLY_ONBOARD_MIC_KEY = "1";
	private static final String ONLY_HEADSET_MIC_KEY = "2";
	private Context mContext;
	private String mSelectedModeKey;

	private void logd(String msg) {
		if (DEBUG) {
			Log.d(TAG, msg);
		}
	}


	Preference.OnPreferenceClickListener mMicSwitchModeListClickListener = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			RadioPreference rp;
		    if (mSelectedModeKey.equals(preference.getKey())) {
				rp = (RadioPreference) preference;
				rp.setChecked(true);
				return true;
			}
			rp = (RadioPreference) mMicSwitchMode_List.findPreference(mSelectedModeKey);
			if (rp != null)
				rp.setChecked(false);
			rp = (RadioPreference) preference;
			rp.setChecked(true);
			mSelectedModeKey = preference.getKey();

			if(mSelectedModeKey.equals(AUTO_SWITCH_KEY)){
				writeMode(AUTO_SWITCH_KEY);
			}else if(mSelectedModeKey.equals(ONLY_ONBOARD_MIC_KEY)) {
				writeMode(ONLY_ONBOARD_MIC_KEY);
			}else if(mSelectedModeKey.equals(ONLY_HEADSET_MIC_KEY)){
				writeMode(ONLY_HEADSET_MIC_KEY);
			}
			return true;
		}
	};


	Preference.OnPreferenceChangeListener mMicSwitchModeChangeListener = new Preference.OnPreferenceChangeListener() {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			logd("onPreferenceChange(): Preference - " + preference
					+ ", key - " + preference.getKey() + ", newValue - "
					+ newValue + ", newValue type - " + newValue.getClass());
			return true;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    mContext = getApplicationContext();
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.es8323_mic_switch);
		getListView().setItemsCanFocus(true);
		mMicSwitchMode_List = (PreferenceGroup) findPreference("es8323_mic_switch_list");
	}

	@Override
	protected void onResume() {
		super.onResume();
		fillList();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	private String readMode() {
        String str = null;
        byte[] strbuf = null;
        long  lenfile = 0 ;
        RandomAccessFile rf;
        try {
            rf = new RandomAccessFile(ES8323_MIC_MODE_PATH, "r");
            try {
                strbuf = new byte[1]; //申请空间
                rf.read(strbuf, 0, 1);//读文件到strbuf中
                str = new String(strbuf, 0,1);
                rf.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch(FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return str;
	}
	
    public void writeMode(String mode)
    {
        RandomAccessFile rf;
        byte[] bt=mode.getBytes();
        logd("writeMode " + mode);
        try {
            rf = new RandomAccessFile(ES8323_MIC_MODE_PATH, "rw");
            try {
                rf.write(bt);
                rf.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }

	private void fillList() {
		RadioPreference rp;
        mSelectedModeKey = readMode();
		mMicSwitchMode_List.removeAll();
		
		rp = new RadioPreference(this, null);
		rp.setKey(AUTO_SWITCH_KEY);
		rp.setTitle(R.string.es8323_mic_auto_switch);
		rp.setPersistent(false);
		rp.setWidgetLayoutResource(R.layout.preference_radio);
		rp.setOnPreferenceClickListener(mMicSwitchModeListClickListener);
		rp.setOnPreferenceChangeListener(mMicSwitchModeChangeListener);
		mMicSwitchMode_List.addPreference(rp);
		if(mSelectedModeKey.equals(AUTO_SWITCH_KEY)) {
            rp.setChecked(true);
        }
        
		rp = new RadioPreference(this, null);
		rp.setKey(ONLY_ONBOARD_MIC_KEY);
		rp.setTitle(R.string.es8323_mic_onboard_only);
		rp.setPersistent(false);
		rp.setWidgetLayoutResource(R.layout.preference_radio);
		rp.setOnPreferenceClickListener(mMicSwitchModeListClickListener);
		rp.setOnPreferenceChangeListener(mMicSwitchModeChangeListener);
		mMicSwitchMode_List.addPreference(rp);
		if(mSelectedModeKey.equals(ONLY_ONBOARD_MIC_KEY)) {
            rp.setChecked(true);
        }
        
		rp = new RadioPreference(this, null);
		rp.setKey(ONLY_HEADSET_MIC_KEY);
		rp.setTitle(R.string.es8323_mic_headset_only);
		rp.setPersistent(false);
		rp.setWidgetLayoutResource(R.layout.preference_radio);
		rp.setOnPreferenceClickListener(mMicSwitchModeListClickListener);
		rp.setOnPreferenceChangeListener(mMicSwitchModeChangeListener);
		mMicSwitchMode_List.addPreference(rp);
		if(mSelectedModeKey.equals(ONLY_HEADSET_MIC_KEY)) {
            rp.setChecked(true);
        }
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		logd("onPreferenceTreeClick: " + preference.getTitle().toString());
		return true;
	}

}
