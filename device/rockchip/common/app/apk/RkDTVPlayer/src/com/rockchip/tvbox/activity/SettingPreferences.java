package com.rockchip.tvbox.activity;

import com.rockchip.tvbox.utils.CommonStaticData;
import com.rockchip.tvbox.utils.ExcuteScan;
import com.rockchip.tvbox.utils.Logger;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;

public class SettingPreferences extends PreferenceActivity implements
							Preference.OnPreferenceChangeListener{

	private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        doIntentMessageHook();
        super.onCreate(savedInstanceState);
        setPreferenceScreen(createPreferenceHierarchy());
        this.getWindow().setBackgroundDrawableResource(R.drawable.epgbackground);
    }

    private PreferenceScreen createPreferenceHierarchy() {
    	String summaryStr = "";
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
        
        // Area preferences 
        PreferenceCategory areaSetPrefCat = new PreferenceCategory(this);
        areaSetPrefCat.setTitle(R.string.general_setting);
        root.addPreference(areaSetPrefCat);
        
        // Scan preferences 
        PreferenceCategory scanPrefCat = new PreferenceCategory(this);
        scanPrefCat.setTitle(R.string.scan);
        root.addPreference(scanPrefCat);
        
        // Area set list preference
        ListPreference areaSetListPref = new ListPreference(this);
        int mode = CommonStaticData.jniIF.getDVBMode_native();
        int arrayId = R.array.array_area_set_dvb;
        if(mode == CommonStaticData.DVB_FE_TYPE_DVBT){
            arrayId = R.array.array_area_set_dvb;
        }
        else if(mode == CommonStaticData.DVB_FE_TYPE_ATSC){
            arrayId = R.array.array_area_set_atsc;
        }
        else if(mode == CommonStaticData.DVB_FE_TYPE_ISDB_ONESEG){
            arrayId = R.array.array_area_set_isdb_oneseg;
        }
        else if(mode == CommonStaticData.DVB_FE_TYPE_ISDB_FULLSEG){
            arrayId = R.array.array_area_set_isdb_fullseg;
        }
        areaSetListPref.setEntries(arrayId);
        areaSetListPref.setEntryValues(arrayId);
        areaSetListPref.setKey(CommonStaticData.areaSetKey);
        areaSetListPref.setTitle(R.string.area_set);
        summaryStr = CommonStaticData.settings.getString(CommonStaticData.areaSetKey, 
        		getResources().getStringArray(arrayId)[0]);
        areaSetListPref.setSummary(summaryStr);
        areaSetListPref.setDefaultValue(summaryStr);
        
        
     // Service order select list preference
        ListPreference orderSetListPref = new ListPreference(this);
        orderSetListPref.setEntries(R.array.array_service_order);
        orderSetListPref.setEntryValues(R.array.array_service_order);
        orderSetListPref.setKey(CommonStaticData.orderSetKey);
        orderSetListPref.setTitle(R.string.order_set);
        summaryStr = CommonStaticData.settings.getString(CommonStaticData.orderSetKey, 
                getResources().getStringArray(R.array.array_service_order)[0]);
        orderSetListPref.setSummary(summaryStr);
        orderSetListPref.setDefaultValue(summaryStr);
        
        
        
        Preference timeZonePref = new Preference(this);
        timeZonePref.setTitle(R.string.date_time);
        timeZonePref.setKey(CommonStaticData.timeZoneSetKey);
        
//        areaSetPrefCat.addPreference(areaSetListPref);
        areaSetPrefCat.addPreference(orderSetListPref);
//        areaSetPrefCat.addPreference(timeZonePref);
        
        areaSetListPref.setOnPreferenceChangeListener(this);
        orderSetListPref.setOnPreferenceChangeListener(this);
        timeZonePref.setOnPreferenceChangeListener(this);
        timeZonePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                ComponentName componentName = 
                    new ComponentName("com.android.settings", 
                                      "com.android.settings.DateTimeSettings");  
                intent.setComponent(componentName);  
                  
                startActivity(intent); 
                return false;
            }
        });
        
        Preference scanPref = new Preference(this);
        scanPref.setTitle(R.string.scan_channels);
        scanPref.setKey(CommonStaticData.scanChannelsKey);

        scanPrefCat.addPreference(scanPref);
        
        scanPref.setOnPreferenceChangeListener(this);
        scanPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO Auto-generated method stub
                ExcuteScan excuteScan = new ExcuteScan(SettingPreferences.this,CommonStaticData.MENU_ID_SEARCH);
                excuteScan.buildChannelScanDialog();
                return false;
            }
        });
        
        
        return root;
    }
//    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
    	if(preference.getKey().equals(CommonStaticData.areaSetKey)) {
    		preference.setSummary((String)newValue);
            SharedPreferences.Editor editor = CommonStaticData.settings.edit();
            editor.putString(CommonStaticData.areaSetKey, (String)newValue);
            editor.commit();
    	}
        if(preference.getKey().equals(CommonStaticData.orderSetKey)) {
            preference.setSummary((String)newValue);
            SharedPreferences.Editor editor = CommonStaticData.settings.edit();
            editor.putString(CommonStaticData.orderSetKey, (String)newValue);
            editor.commit();
    	}
    	else if(preference.getKey().equals(CommonStaticData.timeZoneSetKey)) {

        }
		return true;
	}
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	Logger.d("keyCode:"+keyCode);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}
    /*
     * USB ÏûÏ¢¹´×Ó
     */
    private void doIntentMessageHook() {
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                    System.exit(0);
                    SettingPreferences.this.finish();

                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addDataScheme("file");
        registerReceiver(mReceiver, intentFilter);
    }
    
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Logger.d("SettingPreferences-----------onPause");
        this.unregisterReceiver(mReceiver);
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        Logger.d("SettingPreferences-----------onResume");
//        doIntentMessageHook();
        super.onResume();
    }
}
