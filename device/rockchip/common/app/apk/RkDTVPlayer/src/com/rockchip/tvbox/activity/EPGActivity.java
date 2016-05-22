/********************************************************************************************************************/
/**
 *  @skip   $Id:$
 *  @file   EPGActivity.java
 *  @brief  Electronic Program Guide activity.
 *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
 *
 *  ALL Rights Reserved, Copyright(C) ROCKCHIP LIMITED 2011
 */
/********************************************************************************************************************/
package com.rockchip.tvbox.activity;

import com.rockchip.tvbox.provider.TVProgram.Programs;
import com.rockchip.tvbox.utils.CommonStaticData;
import com.rockchip.tvbox.utils.EPGScan;
import com.rockchip.tvbox.utils.Logger;
import com.rockchip.tvbox.utils.PlayVideo;
import com.rockchip.tvbox.view.VideoView;
import com.rockchip.tvbox.view.VideoView.MySizeChangeLinstener;
import com.rockchip.tvbox.adapter.ServiceListSimpleAdapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.util.Log;

public class EPGActivity extends TabActivity {
    public volatile boolean threadSuspended;
    public int freq;
    public int serviceId;
    public int currWeekday;
    public Uri mUri;
    private VideoView vv;
    public Cursor mCursor;
    private SimpleAdapter EPGListViewSimpleAdapter;
    private ArrayList<HashMap<String, Object>> EPGListViewArrayList = new ArrayList<HashMap<String, Object>>();
    public static final String EPG_NAME_KEY = "epg_name";
    private Intent intent;
    private int listPos;
    TextView topBarText;
    ListView programList;
    ListView serviceList;
    private PlayVideo playVideo;
    private Thread playVideoThd;
    
    private Thread epgScanThd;
    private EPGScan epgScan;
    
    private TabHost mTabHost = null; 
    public LinearLayout mLoadLayout;
    /***************************************************************************************************************/
    /*!
     *  @brief  Called when the activity is first created.
     *  @note   This function treats following :\n
     *              - Set full screen.
     *              - case bolEPGScan is false:
     *                     Start epg scan thread.
     *              - case bolEPGScan is true:
     *                     Call init() function.
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
        setContentView(R.layout.epgview);
        init();
        
//        if(!CommonStaticData.bolEPGScan){
//            CommonStaticData.bolEPGScan = true;
//            displayWaiting();
////            myHandler.sendEmptyMessage(MSG_EPG_SCAN);
            epgScan = new EPGScan(this);
            epgScanThd = new Thread(epgScan);
            epgScanThd.start();
//        }
//        else{
//            init();
//        }
            serviceList.requestFocus();
    }
    private final static String TAB_TAG_SUN="sunday";
    private final static String TAB_TAG_MON="monday";
    private final static String TAB_TAG_TUE="tuesday";
    private final static String TAB_TAG_WED="wednesday";
    private final static String TAB_TAG_THU="thursday";
    private final static String TAB_TAG_FRI="friday";
    private final static String TAB_TAG_SAT="saturday";
    
    /***************************************************************************************************************/
    /*!
     *  @brief  Setup tab host.
     *  @note   This function treats following :\n
     *              - get the tab host that EPGActivity is using to host its tabs.
     *              - Host Specified tab to mTabHost.
     *  @param  none.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    private void setupTabHost() {
        mTabHost=getTabHost();
        if(mTabHost == null){
            Logger.e("mTabHost null!!!");
        }
        mTabHost.addTab(buildTabSpec(TAB_TAG_SUN, getResources().getStringArray(R.array.epgweekday)[0], R.drawable.sun));
        mTabHost.addTab(buildTabSpec(TAB_TAG_MON, getResources().getStringArray(R.array.epgweekday)[1], R.drawable.mon));
        mTabHost.addTab(buildTabSpec(TAB_TAG_TUE, getResources().getStringArray(R.array.epgweekday)[2], R.drawable.tue));
        mTabHost.addTab(buildTabSpec(TAB_TAG_WED, getResources().getStringArray(R.array.epgweekday)[3], R.drawable.wed));
        mTabHost.addTab(buildTabSpec(TAB_TAG_THU, getResources().getStringArray(R.array.epgweekday)[4], R.drawable.thu));
        mTabHost.addTab(buildTabSpec(TAB_TAG_FRI, getResources().getStringArray(R.array.epgweekday)[5], R.drawable.fri));
        mTabHost.addTab(buildTabSpec(TAB_TAG_SAT, getResources().getStringArray(R.array.epgweekday)[6], R.drawable.sat));
    }
    private TabHost.TabSpec buildTabSpec(String tag, String resLabel, int id) {
        return this.mTabHost.newTabSpec(tag).setIndicator("",
                getResources().getDrawable(id)).setContent(R.id.programList);
    } 
    
    /***************************************************************************************************************/
    /*!
     *  @brief  Initial EPGActivity.
     *  @note   This function treats following :\n
     *              - Initial video view(vv),register needed listeners.
     *              - Set serviceList adapter.
     *              - Set programList adapter.
     *              - Register serviceList listeners.
     *              - Setup TabHost and register TabChangedListener.
     *  @param  none.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    private void init(){

        vv = (VideoView)findViewById(R.id.epgvideoview);
        
        vv.setOnErrorListener(new OnErrorListener(){

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                
                /*new AlertDialog.Builder(EPGActivity.this)
                .setTitle("Warning")
                .setMessage("No or bad signal !")
                .setPositiveButton("OK",
                        new AlertDialog.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                vv.stopPlay();				
                            }
           
                        })
                .setCancelable(false)
                .show();*/
                
                return true;
            }
            
        });
        
        vv.setMySizeChangeLinstener(new MySizeChangeLinstener(){

            @Override
            public void doMyThings() {
                // TODO Auto-generated method stub
                vv.setVideoScale(375, 250);
            }
            
        });
        vv.setOnPreparedListener(new OnPreparedListener(){

            @Override
            public void onPrepared(MediaPlayer arg0) {
                // TODO Auto-generated method stub
                
                vv.setVideoScale(375, 250);
                vv.start();  
            }   
        });
    
        vv.setOnCompletionListener(new OnCompletionListener(){

            @Override
            public void onCompletion(MediaPlayer arg0) {
                // TODO Auto-generated method stub

            }
        });
        topBarText = (TextView) findViewById(R.id.epgtopbartext);

        serviceList = (ListView) findViewById(R.id.serviceList);
        programList = (ListView) findViewById(R.id.programList);
        addListFooterView();
        intent = getIntent();
        mUri = intent.getData();
        if (mUri == null) {
            mUri = Programs.CONTENT_URI;
            intent.setData(mUri);
        }
        int cursorPos = intent.getIntExtra("cursorpos", 0);
        Logger.e("cursorPos:"+cursorPos);
        String orderSetStr = CommonStaticData.settings.getString(CommonStaticData.orderSetKey, "");
        if(orderSetStr.equals(getResources().getStringArray(R.array.array_service_order)[0])){
            orderSetStr = Programs.SERVICEID;
        }
        else if(orderSetStr.equals(getResources().getStringArray(R.array.array_service_order)[1])){
            orderSetStr = Programs.SERVICENAME;
        }
        else{
            orderSetStr = Programs.LCN;
        }
        mCursor = managedQuery(mUri, CommonStaticData.PROJECTION, null, null,orderSetStr);
        if(cursorPos != 0){
            mCursor.moveToPosition(cursorPos);
//            String topBarStr1 = mCursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_NAME);
//            Logger.e("topbarstr11111:"+topBarStr1);
        }
        else{
            mCursor.moveToFirst();
        }
        freq = mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ);
        serviceId = mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID);
        topBarText.setText(mCursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_NAME));
/*        
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.epgservicelist_item, mCursor,
                new String[] { Programs.SERVICENAME, Programs.FREQ }, new int[] { R.id.ptitle, R.id.freq });
        serviceList.setAdapter(adapter);
*/
	ServiceListSimpleAdapter adapter = new ServiceListSimpleAdapter(this, R.layout.epg_servicelist_item, mCursor,
	        new String[] { Programs.SERVICENAME, Programs.FREQ }, 
	        new int[] { R.id.ptitle, R.id.freq });
	serviceList.setAdapter(adapter);
				
        playVideoProcess(CommonStaticData.jniIF.getDefalltAudioIndex_native( mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ),
			                                                         mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID) ));		
        Logger.e("mCurpos::::::::"+mCursor.getPosition());
        /* EPG listView */
        EPGListViewSimpleAdapter = new SimpleAdapter(EPGActivity.this, EPGListViewArrayList, 
                                                                      R.layout.epgprogramlist_item,
                                                                      new String[] { EPG_NAME_KEY }, 
                                                                      new int[] { R.id.programText }
                                                                     );
        programList.setAdapter(EPGListViewSimpleAdapter);
        
        UpdateEPGList();
//        serviceList.setSelection(cursorPos);
        serviceList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                listPos = position;
                intent.putExtra("position", listPos);
                EPGActivity.this.setResult(0, intent);				
                mCursor.moveToPosition(position);
                lastEPGCount = 0;
                Logger.e("lastcnt:"+lastEPGCount);
                freq = mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ);
                serviceId = mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID);
                topBarText.setText(mCursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_NAME));
	        currWeekday = CommonStaticData.jniIF.getCurrentWeekDay_native();
	        if(currWeekday < 0)  
	        {
	            currWeekday = 0;	
	        }	
		 mTabHost.setCurrentTab(currWeekday%7);					
                UpdateEPGList();
                Logger.e("lastcnt1:"+lastEPGCount);
		  /*
	        playVideoProcess(CommonStaticData.jniIF.getDefalltAudioIndex_native( mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ),
				                                                         mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID) ));
		*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
                
            }
        });
        serviceList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick (AdapterView<?> parent, View view, int position, long id){   
                /*full screen play(TBD)*/
                listPos = position;
                intent.putExtra("position", listPos);
                EPGActivity.this.setResult(0, intent);
                mCursor.moveToPosition(position);
                lastEPGCount = 0;
                freq = mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ);
                serviceId = mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID);
                topBarText.setText(mCursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_NAME));
                Logger.e("lastcntxx:"+lastEPGCount);
	        currWeekday = CommonStaticData.jniIF.getCurrentWeekDay_native();
	        if(currWeekday < 0)  
	        {
	            currWeekday = 0;	
	        }	
		 mTabHost.setCurrentTab(currWeekday%7);	
                UpdateEPGList();
                Logger.e("lastcntxx1:"+lastEPGCount); 
	        playVideoProcess(CommonStaticData.jniIF.getDefalltAudioIndex_native( mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ),
				                                                         mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID) ));
            }
        });
        
//        final Calendar mCalendar = Calendar.getInstance();  
//        currWeekday = mCalendar.get(Calendar.DAY_OF_WEEK) - 1; 
        currWeekday = CommonStaticData.jniIF.getCurrentWeekDay_native();
        if(currWeekday < 0)  
        {
            currWeekday = 0;	
        }
        /* days spinner */
        /*
        weekdaySpinner = (Spinner)findViewById(R.id.weekday_spinner);    
        weekdaySpinerAdapter = new ArrayAdapter<CharSequence>(this, 
                                                                 android.R.layout.simple_spinner_item, 
                                                                 getResources().getStringArray(R.array.epgweekday)
                                                                );  
        weekdaySpinerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekdaySpinner.setAdapter(weekdaySpinerAdapter);
        weekdaySpinner.setSelection(currWeekday);
        
        weekdaySpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {           
                currWeekday = position;
                UpdateEPGList();
            }

            public void onNothingSelected(AdapterView<?> arg0){
                
            }       
        });
        weekdaySpinner.setFocusable(true);
        weekdaySpinner.requestFocus();
        */
        setupTabHost();
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            
            @Override
            public void onTabChanged(String tabId) {
                // TODO Auto-generated method stub
                currWeekday = mTabHost.getCurrentTab();
                Logger.e("currentTab:"+currWeekday);
                if(currWeekday == 0){
                    currWeekday = 7;
                }
                lastEPGCount = 0;
                UpdateEPGList();
            }
        });

//        mTabHost.findViewById(R.id.programList).setVisibility(View.VISIBLE);
        mTabHost.setCurrentTab(currWeekday%7);

        serviceList.setSelectionFromTop(cursorPos, 25);
//        serviceList.requestFocus();
//      mTabHost.getTabContentView().requestFocus();
//        programList.setSelection(0);
//        programList.requestFocus();
    }
    public void addListFooterView(){
      mLoadLayout = new LinearLayout(this);
      mLoadLayout.setMinimumHeight(60);  
      mLoadLayout.setGravity(Gravity.CENTER);  
      mLoadLayout.setOrientation(LinearLayout.HORIZONTAL); 
      mLoadLayout.setBackgroundColor(Color.RED);
      ProgressBar mProgressBar = new ProgressBar(this);  
      mProgressBar.setPadding(0, 0, 15, 0);  
      mLoadLayout.addView(mProgressBar, new LinearLayout.LayoutParams(  
              LinearLayout.LayoutParams.WRAP_CONTENT,  
              LinearLayout.LayoutParams.WRAP_CONTENT)); 
      
      TextView mTipContent = new TextView(this);  
      mTipContent.setText("Loading...");  
      mLoadLayout.addView(mTipContent, new LinearLayout.LayoutParams(  
              LinearLayout.LayoutParams.FILL_PARENT,  
              LinearLayout.LayoutParams.WRAP_CONTENT)); 
      mLoadLayout.setFocusable(false);
      mLoadLayout.setClickable(false);
      programList.addFooterView(mLoadLayout);
  }
    /***************************************************************************************************************/
    /*!
     *  @brief  Called when the activity is paused.
     *  @note   This function treats following :\n
     *              - Pause the activity.
     *              - If vv is playing, stop play.
     *  @param  none.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
//      vv.pause();
        super.onPause();   
        Logger.d("EPGActivity onPause!!!!!");

        threadSuspended = true;
        myHandler.removeMessages(MSG_EPG_SCAN_REFRESH);
        vv.stopPlay();
    }
    
    /***************************************************************************************************************/
    /*!
     *  @brief  Called when the activity is destroyed.
     *  @note   This function treats following :\n
     *              - If vv is playing, stop play.
     *              - Destroy the activity.
     *  @param  none.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Logger.d("EPGActivity onDestroy!!!!!");
        
//        mCursor.close(); /*TBD*/
        super.onDestroy();
    }
    private void playVideoProcess(int audioIndex){
        if(mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ENCRYPT) ==1){
            vv.stopPlay();
            vv.setBackgroundResource(R.drawable.nosignal);
        }
        else{
            vv.setBackgroundResource(0);
            playVideo = new PlayVideo(vv,
                    mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ),
                    mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID),
                    audioIndex);
            playVideoThd = new Thread(playVideo);
            playVideoThd.start();
        }
    }
    /***************************************************************************************************************/
    /*!
     *  @brief  When TabChangedListener catch TabChanged event, update EPGListViewArrayList.
     *  @note   This function treats following :\n
     *              - Clear EPGListViewArrayList.
     *              - case EPGCount>0
     *                     a)update EPGListViewArrayList.
     *                     b)notifyDataSetChanged().
     *  @param  none.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    public int lastEPGCount = 0;
    public void UpdateEPGList()
    {
//        topBarText.setText(mCursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_NAME));
//        Logger.e("freq:"+freq+" serviceId:"+serviceId+" currWeekday:"+currWeekday);
        int EPGCount = CommonStaticData.jniIF.getDailyScheduleCount_native(freq, serviceId, currWeekday);
//        Logger.d("EPGCount is: " + EPGCount +" Last EPGCount is:"+ lastEPGCount + " for FreqValue: " + freq + 
//             " , and for ServiceID: " + serviceId + ", and for CurrentSelectedDayIndex: " + 
//             currWeekday);
        
        if((EPGCount > 0 && EPGCount != lastEPGCount) || EPGCount == 0)
        {
            EPGListViewArrayList.clear();
            for(int EPGIndex = 0; EPGIndex < EPGCount; EPGIndex++)
            {
                HashMap<String, Object> MapItem = null;
                MapItem = new HashMap<String, Object>();
                //String EPGName = CommonStaticData.jniIF.getDailySchedule_native(freq, serviceId, currWeekday, EPGIndex);
                String EPGName = CommonStaticData.FormatToGbkByte(CommonStaticData.jniIF.getDailyScheduleByteArray_native(freq, serviceId, currWeekday, EPGIndex), 28);
                MapItem.put(EPG_NAME_KEY, EPGName);//+"   weekday:"+currWeekday
                    
                EPGListViewArrayList.add(MapItem);
            }
            EPGListViewSimpleAdapter.notifyDataSetChanged();
            lastEPGCount = EPGCount;
        }   
        else if(EPGCount == -1){
            programList.removeFooterView(mLoadLayout);
        }
    }
    
    /***************************************************************************************************************/
    /*!
     *  @brief  TBD(Removable)
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    class EPGSimpleAdapter extends SimpleAdapter
    {
        public EPGSimpleAdapter(Context context,
                List<? extends Map<String, ?>> data, int resource, String[] from,
                int[] to) 
        {
            super(context, data, resource, from, to);
        }
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent)
//        {
//            View mView = super.getView(position, convertView, parent);
////            mView.setFocusable(true);
//            return mView;       
//        }
    }
    
    /***************************************************************************************************************/
    /*!
     *  @brief  When a key was pressed down , set focus.
     *  @note   This function treats following :\n
     *              - According to different keyCode, set next focus object.
     *  @param  keyCode     [in] The value in event.getKeyCode().
     *          event       [in] Description of the key event.

     *  @return true: prevent this event from being propagated further.
     *          false: it should continue to be propagated.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){

//            Logger.e("tabhost:"+mTabHost.hasFocus()+" widget:"+mTabHost.getTabWidget().hasFocus()+" content:"+mTabHost.getTabContentView().hasFocus()+" servlist:"+serviceList.hasFocus());
//            if(serviceList.hasFocus()){
//                focusID++;
//                if(focusID > (CommonStaticData.serviceTVNum + CommonStaticData.serviceRadioNum)){
////                    serviceList.requestFocus();
//                    focusID = CommonStaticData.serviceTVNum + CommonStaticData.serviceRadioNum;
//                }
//            }
//            else 
//                if(mTabHost.getTabWidget().hasFocus()){
                if(mTabHost.hasFocus()){
//                    mTabHost.getTabContentView().requestFocus();
                    programList.requestFocus();
                    programList.setSelection(0);
//                    Logger.e("===tabhost:"+mTabHost.hasFocus()+" widget:"+mTabHost.getTabWidget().hasFocus()+" content:"+mTabHost.getTabContentView().hasFocus()+" servlist:"+serviceList.hasFocus());
                    return true;
                }


                
        }
        else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
            
            if(serviceList.hasFocus()&& serviceList.getSelectedItemPosition()<=0){
                Logger.e("pos :"+serviceList.getSelectedItemPosition());
                return true;
            }
//            if(!serviceList.hasFocus() && !programList.hasFocus()){
//                Logger.e("weekdaySpinner fucus XXXXXXXXX");
//                weekdaySpinner.requestFocus();
//                return true;
//            }
//            else if(serviceList.hasFocus()){
//                focusID--;
//                if (focusID <= 0) {
//                    focusID = 0;
//                    weekdaySpinner.requestFocus();
//                    return true;
//                }
//            }
//            else if(programList.hasFocus()){
//                return true;
//            }
        }
        else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
            if(serviceList.hasFocus()){
                mTabHost.getTabWidget().requestFocus();
                return true;
            }
            else if(mTabHost.getTabContentView().hasFocus()){
                mTabHost.getTabWidget().requestFocus();
//                return true;
            }
        }
        else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
//            serviceList.requestFocus();
            if(mTabHost.getTabContentView().hasFocus()){
                mTabHost.getTabWidget().requestFocus();
//                return true;
            }
            if(mTabHost.getTabWidget().hasFocus() && mTabHost.getCurrentTab()<=0){
                return true;
            }
            
        }
        else if(keyCode == KeyEvent.KEYCODE_BACK){
            if(mTabHost.hasFocus()){
                serviceList.requestFocus();
                return true;
            }
            Logger.e("mTabHost hasFocus:"+mTabHost.hasFocus());
        }
        return super.onKeyDown(keyCode, event);
    }
    public final byte MSG_EPG_SCAN_REFRESH = 0;
    public Handler myHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what) 
            {
                case MSG_EPG_SCAN_REFRESH:
                    UpdateEPGList();
                    break;
                default:
                    break;
            }
        }
    };
    
    public ProgressDialog myDialog;
    public void displayWaiting()
    {
        if (myDialog != null && myDialog.isShowing())
        {
            myDialog.dismiss();
        }
        myDialog = ProgressDialog.show(EPGActivity.this, getString(R.string.waitTitle), getString(R.string.waitBody),true);
    }
    public void cancelDispWaiting(){
        if (myDialog != null && myDialog.isShowing())
        {
            myDialog.dismiss();
        }
    }
    
    
}
