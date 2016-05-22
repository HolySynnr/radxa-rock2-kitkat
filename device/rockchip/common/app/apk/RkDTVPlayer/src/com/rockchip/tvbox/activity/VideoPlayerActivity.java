/********************************************************************************************************************/
/**
 *  @skip   $Id:$
 *  @file   VideoPlayerActivity.java
 *  @brief  Video Player Activity.
 *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
 *
 *  ALL Rights Reserved, Copyright(C) ROCKCHIP LIMITED 2011
 */
/********************************************************************************************************************/
package com.rockchip.tvbox.activity;

import com.rockchip.tvbox.adapter.ServiceListSimpleAdapter;
import com.rockchip.tvbox.picker.NumberPickerDialog;
import com.rockchip.tvbox.provider.TVProgram.Programs;
import com.rockchip.tvbox.utils.CommonStaticData;
import com.rockchip.tvbox.utils.DisplaySubtitle;
import com.rockchip.tvbox.utils.Logger;
import com.rockchip.tvbox.utils.PlayVideo;
import com.rockchip.tvbox.view.SoundView;
import com.rockchip.tvbox.view.SoundView.OnVolumeChangedListener;
import com.rockchip.tvbox.view.VideoView;
import com.rockchip.tvbox.view.VideoView.MySizeChangeLinstener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.widget.LinearLayout;


import java.util.TimeZone;

public class VideoPlayerActivity extends Activity implements NumberPickerDialog.OnNumberSetListener{
	
	private final static String TAG = "VideoPlayerActivity";
	public volatile boolean threadSuspended;
	private VideoView vv;
	private GestureDetector mGestureDetector;
	private AudioManager mAudioManager;  
	
	private int maxVolume;
	private int currentVolume;  
	
	private TextView txt_btnPrompt;
	private ImageButton btn_prev;
	private ImageButton btn_next;
	private ImageButton btn_servicelist;
	private ImageButton btn_soundup;
	private ImageButton btn_sounddown;
	private ImageButton btn_epg;
	private ImageButton btn_fav;
	private ImageButton btn_help;
	private ImageButton btn_more;
	private ImageButton btn_back;
	
	private ImageButton btn_teletxtprev1;
    private ImageButton btn_teletxtnext1;
    private ImageButton btn_teletxtprev100;
    private ImageButton btn_teletxtnext100;
    private ImageButton btn_teletxtgoto;
    private ImageButton btn_teletxtback;
    
	private TextView promptProgramCurr;
	private TextView promptProgramNext;
	private View controlView;
	private PopupWindow controller;
	
	private SoundView mSoundView;
	private PopupWindow mSoundWindow;
	
	private View extralView;
	private PopupWindow extralWindow;
	
	public View contentView;
	public ImageView contentImage;
	public PopupWindow contentWindow;
	
//	public View teletxtView;
//	public ImageView teletxtImage;
//	public PopupWindow teletxtWindow;
	
	private View teletxtControlView;
    private PopupWindow teletxtController;
    
    Matrix matrix;
    
    private CharSequence[] subtitleStrArrayList;
    private CharSequence[] teletxtStrArrayList;
    private CharSequence[] audioStrArrayList;
	private int screenWidth;
	private int screenHeight;
	private int controlHeight;  
	private int extraHeight;
	private int teletxtControlHeight; 
	
	private final static int TIME = 10000;  
	public final static int SUB_TIME = 5000;
	
	private boolean isControllerShow = true;
	private boolean isSilent = false;
	
	public boolean isTeletxtShow;
	private boolean isTeletxtControllerShow;
	
	TextView serviceName;
	private Uri mUri;
	private Cursor mCursor;
	private Cursor tmpCursor;
    private int screenSizeMode; 
    
    private int favFlag;
    private int menuID;
    
    private PlayVideo playVideo;
    private Thread playVideoThd;
    
    private DisplaySubtitle dispSubtitle;
    private Thread dispSubtitleThd;

    ListView settingList;

    Dialog detailDialog;	
    
	  private int mLastSystemUiVis = 0;
	  View rootView ;    

//    public AlertDialog noSignalDia;
    /***************************************************************************************************************/
    /*!
     *  @brief  Called when the activity is first created.
     *  @note   This function treats following :\n
     *              - Set full screen.
     *              - Get the Cursor that was returned by query().
     *              - Initial controlView(Bottom PopupWindow),extralView(Top PopupWindow),and so on.
     *              - Register buttons listeners.
     *                      a)KeyListener is for remote control mode.
     *                      b)ClickListener is for touch mode.
     *  @param  savedInstanceState      [in] Bundle
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Logger.e("VideoPlayerActivity onCreate");
        super.onCreate(savedInstanceState);  
        /* set full screen */
        CommonStaticData.setFullScreen(this);
        
        setContentView(R.layout.videoview);
        
        getScreenSize();
        
//        noSignalDia = new AlertDialog.Builder(VideoPlayerActivity.this).create();
//        noSignalDia.setTitle(getString(R.string.alert));
//        noSignalDia.setIcon(R.drawable.nosignal);
//        noSignalDia.setMessage(getString(R.string.no_signal));

				LayoutInflater inflater = (LayoutInflater) VideoPlayerActivity.this
				                .getSystemService(LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.videoview_controller, null);

		    rootView = (LinearLayout)layout.findViewById(R.id.bottom_controller);
		    setOnSystemUiVisibilityChangeListener();
        //showSystemUi(false);
        
        mUri = getIntent().getData();

        int id = getIntent().getIntExtra("cursorid", 0);
        menuID = getIntent().getIntExtra("menu_id", 0);
        Logger.e("id:"+id);

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
		if((menuID == CommonStaticData.MENU_ID_SEARCH)||(menuID == CommonStaticData.MENU_ID_SETUP)){
			mCursor = managedQuery(mUri, CommonStaticData.PROJECTION, Programs.TYPE + "=?", 
			                       CommonStaticData.selectionArgsTV, orderSetStr);
			tmpCursor = managedQuery(mUri, CommonStaticData.PROJECTION, Programs.TYPE + "=?", 
			                       CommonStaticData.selectionArgsTV, orderSetStr);			
        }
        else if(menuID == CommonStaticData.MENU_ID_FAVORITE){
            mCursor = managedQuery(mUri, CommonStaticData.PROJECTION, Programs.FAV + "=?", 
                    CommonStaticData.selectionArgsFav,
                    orderSetStr);			
            tmpCursor = managedQuery(mUri, CommonStaticData.PROJECTION, Programs.FAV + "=?", 
                    CommonStaticData.selectionArgsFav,
                    orderSetStr);
        }
        else {
            mCursor = managedQuery(mUri, CommonStaticData.PROJECTION, Programs.TYPE + "=?", 
                    menuID == CommonStaticData.MENU_ID_TV ? CommonStaticData.selectionArgsTV : CommonStaticData.selectionArgsRadio,
                            orderSetStr);			
            tmpCursor = managedQuery(mUri, CommonStaticData.PROJECTION, Programs.TYPE + "=?", 
                    menuID == CommonStaticData.MENU_ID_TV ? CommonStaticData.selectionArgsTV : CommonStaticData.selectionArgsRadio,
                            orderSetStr);		
        }
		
        mCursor.moveToPosition(id);		

//        Logger.e("VideoPlayerActivity OnCreate cursor pos::::::::::"+mCursor.getPosition());
        
        screenSizeMode = CommonStaticData.settings.getInt(CommonStaticData.scrSizeModeKey, 
                                                                            SCREEN_FULL);
        Looper.myQueue().addIdleHandler(new IdleHandler(){

			@Override
			public boolean queueIdle() {
//				Logger.e("queue idle!!!!!!!!!!!!!!");
				// TODO Auto-generated method stub
			    if(vv.isShown()){
			        if(contentWindow != null){
			            contentWindow.showAtLocation(vv, Gravity.NO_GRAVITY, 0, 0);
			            contentWindow.update(0, 0, screenWidth, screenHeight);
			            
//			            subtitleWindow1.showAtLocation(vv, Gravity.NO_GRAVITY, 0, 0);
//                        subtitleWindow1.update(0, 0, screenWidth, 50);
                    }
			        
//			        if(teletxtWindow != null){
//			            teletxtWindow.showAtLocation(vv, Gravity.NO_GRAVITY, 0, 0);
//			            teletxtWindow.update(0, 0, screenWidth, screenHeight);
//                    }
			        
    				if(controller != null){
    					controller.showAtLocation(vv, Gravity.BOTTOM, 0, 0);
    					//controller.update(screenWidth, controlHeight);
    					controller.update(0, 0, screenWidth, controlHeight);
    				}
    				
    				if(extralWindow != null){
    					extralWindow.showAtLocation(vv,Gravity.TOP,0, 0);
    					extralWindow.update(0, 0, screenWidth, extraHeight);
    				}
    				
    				if(teletxtController != null){
    				    teletxtController.showAtLocation(vv, Gravity.BOTTOM, 0, 0);
//    				    teletxtController.update(0, 0, screenWidth, teletxtControlHeight);
                    }
			    }
				//myHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
				return false;  
			}
        });
        
        controlView = getLayoutInflater().inflate(R.layout.videoview_controller, null);
        controller = new PopupWindow(controlView);
        
        promptProgramCurr = (TextView)controlView.findViewById(R.id.prompt1);
        promptProgramNext = (TextView)controlView.findViewById(R.id.prompt2);
        
        mSoundView = new SoundView(this);
        mSoundView.setOnVolumeChangeListener(new OnVolumeChangedListener(){

			@Override
			public void setYourVolume(int index) {
				 
				cancelDelayHide();
				updateVolume(index);
				hideControllerDelay();
			}
        });
        
        mSoundWindow = new PopupWindow(mSoundView);
        
        extralView = getLayoutInflater().inflate(R.layout.extral, null);
        extralWindow = new PopupWindow(extralView);
        serviceName = (TextView)extralView.findViewById(R.id.servicename);
        
        serviceName.setText(String.format("%03d", (mCursor.getPosition()+1))+"  "+mCursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_NAME));
        contentView = getLayoutInflater().inflate(R.layout.subtitle_teletxt, null);
        contentImage = (ImageView)contentView.findViewById(R.id.contentImage);

        contentWindow = new PopupWindow(contentView);
        contentView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(!isTeletxtShow){
                    if(!isControllerShow){
                        showController();
                        hideControllerDelay();
                    }else {
                        cancelDelayHide();
                        hideController();
                    }
                }
                else{
                    if(!isTeletxtControllerShow){
                        showTeletxtController();
                        hideTeletxtControllerDelay();
                    }else {
                        cancelTeletxtDelayHide();
                        hideTeletxtController();
                    }
                }
            }
        });
        
        teletxtControlView = getLayoutInflater().inflate(R.layout.teletxtview_controller, null);
        teletxtController = new PopupWindow(teletxtControlView);
        
//        teletxtView = getLayoutInflater().inflate(R.layout.teletxt, null);
//        teletxtImage = (ImageView)teletxtView.findViewById(R.id.teletxtImage);
//
//        teletxtWindow = new PopupWindow(teletxtView);
//        teletxtView.setOnClickListener(new OnClickListener() {
//            
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                if(!isTeletxtShow){
//                    if(!isControllerShow){
//                        showController();
//                        hideControllerDelay();
//                    }else {
//                        cancelDelayHide();
//                        hideController();
//                    }
//                }
//                else{
//                    if(!isTeletxtControllerShow){
//                        showTeletxtController();
//                        hideTeletxtControllerDelay();
//                    }else {
//                        cancelTeletxtDelayHide();
//                        hideTeletxtController();
//                    }
//                }
//            }
//        });
        
        float scaleWidth = (float)screenWidth/CommonStaticData.subW;
        float scaleHeight = (float)screenHeight/CommonStaticData.subH;
//        Logger.e("scaleW:"+scaleWidth+" scaleH:"+scaleHeight);
        matrix = new Matrix();
        matrix.postScale(scaleWidth,scaleHeight);
        
        favFlag = mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FAV);
        txt_btnPrompt = (TextView) controlView.findViewById(R.id.btnPrompt);
        btn_prev = (ImageButton) controlView.findViewById(R.id.prev);
        btn_next = (ImageButton) controlView.findViewById(R.id.next);
        btn_servicelist = (ImageButton) controlView.findViewById(R.id.servicelist);
        btn_soundup = (ImageButton) controlView.findViewById(R.id.soundup);
        btn_sounddown = (ImageButton) controlView.findViewById(R.id.sounddown);
        btn_epg = (ImageButton) controlView.findViewById(R.id.epgbutton);
        
        btn_fav = (ImageButton) controlView.findViewById(R.id.favbutton);
        if(favFlag == 1){
            btn_fav.setBackgroundResource(R.drawable.button_states7_1);
        }
        else{
            btn_fav.setBackgroundResource(R.drawable.button_states7);
        }
        
        btn_help = (ImageButton) controlView.findViewById(R.id.help);
        btn_more = (ImageButton) controlView.findViewById(R.id.more);
        btn_back = (ImageButton) controlView.findViewById(R.id.back);
        
        btn_teletxtprev1 = (ImageButton) teletxtControlView.findViewById(R.id.teletxtprev1);
        btn_teletxtnext1 = (ImageButton) teletxtControlView.findViewById(R.id.teletxtnext1);
        btn_teletxtprev100 = (ImageButton) teletxtControlView.findViewById(R.id.teletxtprev100);
        btn_teletxtnext100 = (ImageButton) teletxtControlView.findViewById(R.id.teletxtnext100);
        btn_teletxtgoto = (ImageButton) teletxtControlView.findViewById(R.id.teletxtgoto);
        btn_teletxtback = (ImageButton) teletxtControlView.findViewById(R.id.teletxtback);
        
        vv = (VideoView) findViewById(R.id.vv);
        
        vv.setOnErrorListener(new OnErrorListener(){

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				/*new AlertDialog.Builder(VideoPlayerActivity.this)
		                .setTitle("Warning")
		                .setMessage("No or bad signal !")
		                .setPositiveButton("OK",
		                        new AlertDialog.OnClickListener() {
	
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
						                vv.stopPlay();
						                VideoPlayerActivity.this.finish();
							}
	   
	                        })
		                .setCancelable(false)
		                .show();*/
		                
		                return true;
			}
        	
        });
        
        playVideoProcess(CommonStaticData.jniIF.getDefalltAudioIndex_native( mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ),
			                                                         mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID) ));
        
        vv.setMySizeChangeLinstener(new MySizeChangeLinstener(){

			@Override
			public void doMyThings() {
				// TODO Auto-generated method stub
				setVideoScale(screenSizeMode);
			}
        	
        });

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        
        btn_prev.setOnKeyListener(new OnKeyListener() {
            
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                bolStopSubtitle = true;
                CommonStaticData.jniIF.subtitleStop_native(0);
                
		  boolean isSuccess = mCursor.moveToPrevious();
		    if(!isSuccess){
		        mCursor.moveToLast();
		    }	
              serviceName.setText(String.format("%03d", (mCursor.getPosition()+1))+"  "+mCursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_NAME));
	        playVideoProcess(CommonStaticData.jniIF.getDefalltAudioIndex_native( mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ),
				                                                         mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID) ));
                
                favFlag = mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FAV);
                if(favFlag == 1){
                    btn_fav.setBackgroundResource(R.drawable.button_states7_1);
                }
                else{
                    btn_fav.setBackgroundResource(R.drawable.button_states7);
                }
                return false;
            }
        });
        btn_prev.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
                      bolStopSubtitle = true;
			 CommonStaticData.jniIF.subtitleStop_native(0);
			    
		  boolean isSuccess = mCursor.moveToPrevious();
		    if(!isSuccess){
		        mCursor.moveToLast();
		    }				
                serviceName.setText(String.format("%03d", (mCursor.getPosition()+1))+"  "+mCursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_NAME));
	        playVideoProcess(CommonStaticData.jniIF.getDefalltAudioIndex_native( mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ),
				                                                         mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID) ));
                
		        favFlag = mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FAV);
                if(favFlag == 1){
                    btn_fav.setBackgroundResource(R.drawable.button_states7_1);
                }
                else{
                    btn_fav.setBackgroundResource(R.drawable.button_states7);
                }
			}
        	
        });
//        btn_prev.setOnFocusChangeListener(new OnFocusChangeListener() {
//
//            public void onFocusChange(View v, boolean hasFocus) {
//                // TODO Auto-generated method stub
//
//                if (hasFocus == true) {
//
//                    btn_prev.setImageResource(R.drawable.fav_on);
//                } else {
//
//                    btn_prev.setImageResource(R.drawable.fav_off);
//                }
//            }
//
//        });

        btn_next.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                bolStopSubtitle = true;
                CommonStaticData.jniIF.subtitleStop_native(0);
                
                boolean isSuccess = mCursor.moveToNext();
                if(!isSuccess){
                    mCursor.moveToFirst();
                }				
                
                favFlag = mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FAV);
                serviceName.setText(String.format("%03d", (mCursor.getPosition()+1))+"  "+mCursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_NAME));
	        playVideoProcess(CommonStaticData.jniIF.getDefalltAudioIndex_native( mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ),
				                                                         mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID) ));
                
                if(favFlag == 1){
                    btn_fav.setBackgroundResource(R.drawable.button_states7_1);
                }
                else{
                    btn_fav.setBackgroundResource(R.drawable.button_states7);
                }
                
            
                return false;
            }
        });
        btn_next.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                bolStopSubtitle = true;
                CommonStaticData.jniIF.subtitleStop_native(0);
                
                // TODO Auto-generated method stub
                boolean isSuccess = mCursor.moveToNext();
                if(!isSuccess){
                    mCursor.moveToFirst();
                }	
                favFlag = mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FAV);
                serviceName.setText(String.format("%03d", (mCursor.getPosition()+1))+"  "+mCursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_NAME));
	        playVideoProcess(CommonStaticData.jniIF.getDefalltAudioIndex_native( mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ),
				                                                         mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID) ));
                
                if(favFlag == 1){
                    btn_fav.setBackgroundResource(R.drawable.button_states7_1);
                }
                else{
                    btn_fav.setBackgroundResource(R.drawable.button_states7);
                }
                
            }
            
        });
        
        btn_servicelist.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                
                detailDialog = new Dialog(VideoPlayerActivity.this, R.style.MyDialog);
                //设置它的ContentView
                detailDialog.setContentView(R.layout.playservicelist);
                ListView serviceList = (ListView)detailDialog.findViewById(R.id.playservicelist);
                
                ServiceListSimpleAdapter adapter = new ServiceListSimpleAdapter(VideoPlayerActivity.this, R.layout.servicelist_item, tmpCursor,
                        new String[] { Programs.SERVICENAME, Programs.FREQ }, 
                        new int[] { R.id.ptitle, R.id.freq });
                serviceList.setAdapter(adapter);
                serviceList.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // TODO Auto-generated method stub
                        Logger.e("selected id:"+id+" pos:"+position+"tmpCursor pos:"+tmpCursor.getPosition());
                        bolStopSubtitle = true;
                        CommonStaticData.jniIF.subtitleStop_native(0);
                        
                        
                        favFlag = tmpCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FAV);
                        
                        if(favFlag == 1){
                            btn_fav.setBackgroundResource(R.drawable.button_states7_1);
                        }
                        else{
                            btn_fav.setBackgroundResource(R.drawable.button_states7);
                        }
                        mCursor.moveToPosition((int)position);	
                       serviceName.setText(String.format("%03d", (mCursor.getPosition()+1))+"  "+mCursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_NAME));
		         playVideoProcess(CommonStaticData.jniIF.getDefalltAudioIndex_native( mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ),
					                                                         mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID) ));						
                    }
                });
                detailDialog.show();
        
                return false;
            }
        });
        btn_servicelist.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                detailDialog = new Dialog(VideoPlayerActivity.this, R.style.MyDialog);
                //设置它的ContentView
                detailDialog.setContentView(R.layout.playservicelist);
                ListView serviceList = (ListView)detailDialog.findViewById(R.id.playservicelist);
                
                ServiceListSimpleAdapter adapter = new ServiceListSimpleAdapter(VideoPlayerActivity.this, R.layout.servicelist_item, tmpCursor,
                        new String[] { Programs.SERVICENAME, Programs.FREQ }, 
                        new int[] { R.id.ptitle, R.id.freq });
                serviceList.setAdapter(adapter);
                serviceList.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // TODO Auto-generated method stub
                        Logger.e("selected id:"+id+" pos:"+position+"tmpCursor pos:"+tmpCursor.getPosition());
                        bolStopSubtitle = true;
                        CommonStaticData.jniIF.subtitleStop_native(0);
                        
                        
                        favFlag = tmpCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FAV);  
                        if(favFlag == 1){
                            btn_fav.setBackgroundResource(R.drawable.button_states7_1);
                        }
                        else{
                            btn_fav.setBackgroundResource(R.drawable.button_states7);
                        }
                        mCursor.moveToPosition((int)position);
                        serviceName.setText(String.format("%03d", (mCursor.getPosition()+1))+"  "+mCursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_NAME));
		          playVideoProcess(CommonStaticData.jniIF.getDefalltAudioIndex_native( mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ),
					                                                         mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID) ));                        
                    }
                });

                detailDialog.show();
            }
            
        });
        
        btn_soundup.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                cancelDelayHide();
                int index = SoundView.volIndex;
                mSoundView.setIndex(++index);
                
                mSoundWindow.showAtLocation(vv, Gravity.RIGHT|Gravity.CENTER_VERTICAL, 15, 0);
                mSoundWindow.update(15,0,SoundView.MY_WIDTH,SoundView.MY_HEIGHT);
                
//              if(isSoundShow){
//                  mSoundWindow.dismiss();
//              }else{
//                  if(mSoundWindow.isShowing()){
//                      mSoundWindow.update(15,0,SoundView.MY_WIDTH,SoundView.MY_HEIGHT);
//                  }else{
//                      mSoundWindow.showAtLocation(vv, Gravity.RIGHT|Gravity.CENTER_VERTICAL, 15, 0);
//                      mSoundWindow.update(15,0,SoundView.MY_WIDTH,SoundView.MY_HEIGHT);
//                  }
//              }
//              isSoundShow = !isSoundShow;
                hideControllerDelay();
            
                return false;
            }
        });
        
        btn_soundup.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			cancelDelayHide();
			int index = SoundView.volIndex;
            mSoundView.setIndex(++index);
			
			mSoundWindow.showAtLocation(vv, Gravity.RIGHT|Gravity.CENTER_VERTICAL, 15, 0);
            mSoundWindow.update(15,0,SoundView.MY_WIDTH,SoundView.MY_HEIGHT);
            
//			if(isSoundShow){
//				mSoundWindow.dismiss();
//			}else{
//				if(mSoundWindow.isShowing()){
//					mSoundWindow.update(15,0,SoundView.MY_WIDTH,SoundView.MY_HEIGHT);
//				}else{
//					mSoundWindow.showAtLocation(vv, Gravity.RIGHT|Gravity.CENTER_VERTICAL, 15, 0);
//					mSoundWindow.update(15,0,SoundView.MY_WIDTH,SoundView.MY_HEIGHT);
//				}
//			}
//			isSoundShow = !isSoundShow;
			hideControllerDelay();
		}   
       });
        
//        btn_soundup.setOnLongClickListener(new OnLongClickListener(){
//
//			@Override
//			public boolean onLongClick(View arg0) {
//				// TODO Auto-generated method stub
//				if(isSilent){
//					btn_soundup.setImageResource(R.drawable.sounddown);
//				}else{
//					btn_soundup.setImageResource(R.drawable.sounddisable);
//				}
//				isSilent = !isSilent;
//				updateVolume(currentVolume);
//				cancelDelayHide();
//				hideControllerDelay();
//				return true;
//			}
//        	
//        });
        
        btn_sounddown.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                cancelDelayHide();
                
                int index = SoundView.volIndex;
                mSoundView.setIndex(--index);
                
                mSoundWindow.showAtLocation(vv, Gravity.RIGHT|Gravity.CENTER_VERTICAL, 15, 0);
                mSoundWindow.update(15,0,SoundView.MY_WIDTH,SoundView.MY_HEIGHT);
                hideControllerDelay();
            
                return false;
            }
        });
        
        btn_sounddown.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                cancelDelayHide();
                
                int index = SoundView.volIndex;
                mSoundView.setIndex(--index);
                
                mSoundWindow.showAtLocation(vv, Gravity.RIGHT|Gravity.CENTER_VERTICAL, 15, 0);
                mSoundWindow.update(15,0,SoundView.MY_WIDTH,SoundView.MY_HEIGHT);
                hideControllerDelay();
            }   
           });
        
        btn_epg.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                    vv.stopPlay();
                hideController();
                Intent intent = new Intent(null, mUri,
                                    VideoPlayerActivity.this, EPGActivity.class);
                intent.putExtra("cursorpos", mCursor.getPosition());
//                startActivityForResult(intent , 0);
                startActivity(intent);
            
                return false;
            }
        });
        
        btn_epg.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                    vv.stopPlay();
                hideController();
                Intent intent = new Intent(null, mUri,
                                    VideoPlayerActivity.this, EPGActivity.class);
                intent.putExtra("cursorpos", mCursor.getPosition());
//                startActivityForResult(intent , 0);
                startActivity(intent);
            }   
        });
        
        btn_fav.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                favFlag = mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FAV);
                if(favFlag == 0){
                    favFlag = 1;
                    btn_fav.setBackgroundResource(R.drawable.button_states7_1);
                }
                else{
                    favFlag = 0;
                    btn_fav.setBackgroundResource(R.drawable.button_states7);
                }
                ContentValues values = new ContentValues();
                String whereClause = Programs.SERVICEID + "=?";
                String[] selArgs = new String[] { String.valueOf(mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID)) };
                values.put(Programs.FAV, favFlag);
                getContentResolver().update(mUri, values, whereClause, selArgs);
                
                int curPos = mCursor.getPosition();
                mCursor.requery();
                mCursor.moveToPosition(curPos);
            
                return false;
            }
        });
        
        btn_fav.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                favFlag = mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FAV);
                if(favFlag == 0){
                    favFlag = 1;
                    btn_fav.setBackgroundResource(R.drawable.button_states7_1);
                }
                else{
                    favFlag = 0;
                    btn_fav.setBackgroundResource(R.drawable.button_states7);
                }
                ContentValues values = new ContentValues();
                String whereClause = Programs.SERVICEID + "=?";
                String[] selArgs = new String[] { String.valueOf(mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID)) };
                values.put(Programs.FAV, favFlag);
                getContentResolver().update(mUri, values, whereClause, selArgs);
                
                int curPos = mCursor.getPosition();
                mCursor.requery();
                mCursor.moveToPosition(curPos);
            }   
        });
        
        btn_help.setOnKeyListener(new OnKeyListener() {
            Dialog dialog;
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                /* TBD */
                dialog = new Dialog(VideoPlayerActivity.this, R.style.transDialog);
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                View view = VideoPlayerActivity.this.getLayoutInflater().inflate(R.layout.about, null);
                dialog.setContentView(view);
//                view.findViewById(R.id.cancel).setOnKeyListener(new OnKeyListener() {
//                    
//                    @Override
//                    public boolean onKey(View v, int keyCode, KeyEvent event) {
//                        // TODO Auto-generated method stub
//                        dialog.dismiss();
//                        return false;
//                    }
//                });
//                vv.pause();
                dialog.show();
                cancelDelayHide();
                return false;
            }
        });
        
        btn_help.setOnClickListener(new OnClickListener(){
            
            Dialog dialog;
            OnClickListener mClickListener = new OnClickListener(){
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
//                    vv.start();
                }
            };
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                /*Intent intent = new Intent();
                intent.setClass(VideoPlayerActivity.this, VideoChooseActivity.class);
                VideoPlayerActivity.this.startActivityForResult(intent, 0);*/
                
                dialog = new Dialog(VideoPlayerActivity.this, R.style.transDialog);
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                View view = VideoPlayerActivity.this.getLayoutInflater().inflate(R.layout.about, null);
                dialog.setContentView(view);
                view.findViewById(R.id.cancel).setOnClickListener(mClickListener);
//                vv.pause();
                dialog.show();
                cancelDelayHide();
            }
            
        });
        
        final OnClickListener moreDialogClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Logger.e("++++++++++++++++moreDialogClickListener click:"+
                        v.getId());
                detailDialog = new Dialog(VideoPlayerActivity.this, R.style.MyDialog);
                //设置它的ContentView
                detailDialog.setContentView(R.layout.moredetail);
                settingList = (ListView)detailDialog.findViewById(R.id.settingList);
                final RadioGroup radioGroup = (RadioGroup)detailDialog.findViewById(R.id.main_tab);

                RadioButton radioButton0 = (RadioButton) detailDialog.findViewById(R.id.radio_button0);
                radioButton0.setOnFocusChangeListener(new OnFocusChangeListener() {
                    
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        if(hasFocus){
                            radioGroup.check(R.id.radio_button0);
                            subtitleStrArrayList = null;
                            subtitleStrArrayList = new CharSequence[CommonStaticData.subtitleNum+1];
                            subtitleStrArrayList[0] = getResources().getString(R.string.disable_subtitle);
                            for(int Index = 1; Index <= CommonStaticData.subtitleNum; Index++)
                            {
                                subtitleStrArrayList[Index] = CommonStaticData.jniIF.subtitleGetInfo_native(Index-1);
                            }
                            subtitleListSet(subtitleStrArrayList);
                        }
                    }
                });
                radioButton0.setOnKeyListener(new OnKeyListener() {
                    
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                            settingList.setNextFocusLeftId(R.id.radio_button3);
                            settingList.setNextFocusRightId(R.id.radio_button1);
                            settingList.setSelection(0);
                            settingList.requestFocus();
                        }
                        return false;
                    }
                });
                radioButton0.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        radioGroup.check(R.id.radio_button0);
                        subtitleStrArrayList = null;
                        subtitleStrArrayList = new CharSequence[CommonStaticData.subtitleNum+1];
                        subtitleStrArrayList[0] = getResources().getString(R.string.disable_subtitle);
                        for(int Index = 1; Index <= CommonStaticData.subtitleNum; Index++)
                        {
                            subtitleStrArrayList[Index] = CommonStaticData.jniIF.subtitleGetInfo_native(Index-1);
                        }
                        subtitleListSet(subtitleStrArrayList);
                    }
                });
                RadioButton radioButton1 = (RadioButton) detailDialog.findViewById(R.id.radio_button1);
                radioButton1.setOnFocusChangeListener(new OnFocusChangeListener() {
                    
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        if(hasFocus){
                            radioGroup.check(R.id.radio_button1);
                            teletxtStrArrayList = null;
                            teletxtStrArrayList = new CharSequence[CommonStaticData.teletxtNum+1];
                            teletxtStrArrayList[0] = getResources().getString(R.string.disable_teletxt);
                            for(int Index = 1; Index <= CommonStaticData.teletxtNum; Index++){
                                teletxtStrArrayList[Index] = CommonStaticData.jniIF.teletextGetInfo_native(Index-1);
                            }
                            teletxtListSet(teletxtStrArrayList);
                        }
                    }
                });
                radioButton1.setOnKeyListener(new OnKeyListener() {
                    
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                            settingList.setNextFocusLeftId(R.id.radio_button0);
                            settingList.setNextFocusRightId(R.id.radio_button2);
                            settingList.setSelection(0);
                            settingList.requestFocus();
                        }
                        return false;
                    }
                });
                radioButton1.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        radioGroup.check(R.id.radio_button1);
                        teletxtStrArrayList = null;
                        teletxtStrArrayList = new CharSequence[CommonStaticData.teletxtNum+1];
                        teletxtStrArrayList[0] = getResources().getString(R.string.disable_teletxt);
                        for(int Index = 1; Index <= CommonStaticData.teletxtNum; Index++){
                            teletxtStrArrayList[Index] = CommonStaticData.jniIF.teletextGetInfo_native(Index-1);
                        }
                        teletxtListSet(teletxtStrArrayList);
                    }
                });
                RadioButton radioButton2 = (RadioButton) detailDialog.findViewById(R.id.radio_button2);
                radioButton2.setOnFocusChangeListener(new OnFocusChangeListener() {
                    
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        if(hasFocus){
                            radioGroup.check(R.id.radio_button2);
                            audioStrArrayList = null;
                            audioStrArrayList = new CharSequence[CommonStaticData.audioNum];
                            for(int Index = 0; Index <= CommonStaticData.audioNum-1; Index++){
                                audioStrArrayList[Index] = CommonStaticData.jniIF.audioGetInfo_native(Index);
                            }
                            audioListSet(audioStrArrayList);
                        }
                    }
                });
                radioButton2.setOnKeyListener(new OnKeyListener() {
                    
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                            settingList.setNextFocusLeftId(R.id.radio_button1);
                            settingList.setNextFocusRightId(R.id.radio_button3);
                            settingList.setSelection(0);
                            settingList.requestFocus();
                        }
                        return false;
                    }
                });
                radioButton2.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        radioGroup.check(R.id.radio_button2);
                        audioStrArrayList = null;
                        audioStrArrayList = new CharSequence[CommonStaticData.audioNum];
                        for(int Index = 0; Index <= CommonStaticData.audioNum-1; Index++){
                            audioStrArrayList[Index] = CommonStaticData.jniIF.audioGetInfo_native(Index);
                        }
                        audioListSet(audioStrArrayList);
                    }
                });
                
                RadioButton radioButton3 = (RadioButton) detailDialog.findViewById(R.id.radio_button3);
                radioButton3.setOnFocusChangeListener(new OnFocusChangeListener() {
                    
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        Logger.e("focus changed:"+v);
                        Logger.e("hsaFoucus :"+hasFocus);
                        // TODO Auto-generated method stub
                        if(hasFocus){
                            radioGroup.check(R.id.radio_button3);
                        }
                    }
                });
                radioButton3.setOnKeyListener(new OnKeyListener() {
                    
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                            settingList.setNextFocusLeftId(R.id.radio_button2);
                            settingList.setNextFocusRightId(R.id.radio_button0);
                            settingList.setSelection(0);
                            settingList.requestFocus();
                        }
                        return false;
                    }
                });
                radioButton3.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        radioGroup.check(R.id.radio_button3);
                    }
                });
                
                switch(v.getId()){
                    case R.id.more1:
                        radioButton0.requestFocus();
                        radioGroup.check(R.id.radio_button0);
                        radioButton0.performClick();
                        break;
                    case R.id.more2:
                        radioButton1.requestFocus();
                        radioGroup.check(R.id.radio_button1);
                        radioButton1.performClick();
                        break;
                    case R.id.more3:
                        radioButton2.requestFocus();
                        radioGroup.check(R.id.radio_button2);
                        break;
                    case R.id.more4:
                        radioButton3.requestFocus();
                        radioGroup.check(R.id.radio_button3);
                        break;
                    default:
                        radioButton0.requestFocus();
                        radioGroup.check(R.id.radio_button0);
                        break;
                }
                detailDialog.show();
            }
        };
        
        btn_more.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                Dialog dialog = new Dialog(VideoPlayerActivity.this, R.style.MyDialog);
//                //设置它的ContentView
//                dialog.setContentView(R.layout.moresummary);
//                
//                dialog.findViewById(R.id.more1).setOnClickListener(moreDialogClickListener);
//
//                dialog.findViewById(R.id.more2).setOnClickListener(moreDialogClickListener);
//
//                dialog.findViewById(R.id.more3).setOnClickListener(moreDialogClickListener);
//
//                dialog.findViewById(R.id.more4).setOnClickListener(moreDialogClickListener);
//
//                dialog.show();
                

                // TODO Auto-generated method stub
                detailDialog = new Dialog(VideoPlayerActivity.this, R.style.MyDialog);
                //设置它的ContentView
                detailDialog.setContentView(R.layout.moredetail);
                settingList = (ListView)detailDialog.findViewById(R.id.settingList);
                final RadioGroup radioGroup = (RadioGroup)detailDialog.findViewById(R.id.main_tab);

                RadioButton radioButton0 = (RadioButton) detailDialog.findViewById(R.id.radio_button0);
                radioButton0.setOnFocusChangeListener(new OnFocusChangeListener() {
                    
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        if(hasFocus){
                            radioGroup.check(R.id.radio_button0);
                            subtitleStrArrayList = null;
                            subtitleStrArrayList = new CharSequence[CommonStaticData.subtitleNum+1];
                            subtitleStrArrayList[0] = getResources().getString(R.string.disable_subtitle);
                            for(int Index = 1; Index <= CommonStaticData.subtitleNum; Index++)
                            {
                                subtitleStrArrayList[Index] = CommonStaticData.jniIF.subtitleGetInfo_native(Index-1);
                            }
                            subtitleListSet(subtitleStrArrayList);
                        }
                    }
                });
                radioButton0.setOnKeyListener(new OnKeyListener() {
                    
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                            settingList.setNextFocusLeftId(R.id.radio_button4);
                            settingList.setNextFocusRightId(R.id.radio_button1);
                            settingList.setSelection(0);
                            settingList.requestFocus();
                        }
                        return false;
                    }
                });
                radioButton0.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        radioGroup.check(R.id.radio_button0);
                        subtitleStrArrayList = null;
                        subtitleStrArrayList = new CharSequence[CommonStaticData.subtitleNum+1];
                        subtitleStrArrayList[0] = getResources().getString(R.string.disable_subtitle);
                        for(int Index = 1; Index <= CommonStaticData.subtitleNum; Index++)
                        {
                            subtitleStrArrayList[Index] = CommonStaticData.jniIF.subtitleGetInfo_native(Index-1);
                        }
                        subtitleListSet(subtitleStrArrayList);
                    }
                });
                RadioButton radioButton1 = (RadioButton) detailDialog.findViewById(R.id.radio_button1);
                radioButton1.setOnFocusChangeListener(new OnFocusChangeListener() {
                    
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        if(hasFocus){
                            radioGroup.check(R.id.radio_button1);
                            teletxtStrArrayList = null;
                            teletxtStrArrayList = new CharSequence[CommonStaticData.teletxtNum+1];
                            teletxtStrArrayList[0] = getResources().getString(R.string.disable_teletxt);
                            for(int Index = 1; Index <= CommonStaticData.teletxtNum; Index++){
                                teletxtStrArrayList[Index] = CommonStaticData.jniIF.teletextGetInfo_native(Index-1);
                            }
                            teletxtListSet(teletxtStrArrayList);
                        }
                    }
                });
                radioButton1.setOnKeyListener(new OnKeyListener() {
                    
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                            settingList.setNextFocusLeftId(R.id.radio_button0);
                            settingList.setNextFocusRightId(R.id.radio_button2);
                            settingList.setSelection(0);
                            settingList.requestFocus();
                        }
                        return false;
                    }
                });
                radioButton1.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        radioGroup.check(R.id.radio_button1);
                        teletxtStrArrayList = null;
                        teletxtStrArrayList = new CharSequence[CommonStaticData.teletxtNum+1];
                        teletxtStrArrayList[0] = getResources().getString(R.string.disable_teletxt);
                        for(int Index = 1; Index <= CommonStaticData.teletxtNum; Index++){
                            teletxtStrArrayList[Index] = CommonStaticData.jniIF.teletextGetInfo_native(Index-1);
                        }
                        teletxtListSet(teletxtStrArrayList);
                    }
                });
                RadioButton radioButton2 = (RadioButton) detailDialog.findViewById(R.id.radio_button2);
                radioButton2.setOnFocusChangeListener(new OnFocusChangeListener() {
                    
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        if(hasFocus){
                            radioGroup.check(R.id.radio_button2);
                            audioStrArrayList = null;
                            audioStrArrayList = new CharSequence[CommonStaticData.audioNum];
                            for(int Index = 0; Index <= CommonStaticData.audioNum-1; Index++){
                                audioStrArrayList[Index] = CommonStaticData.jniIF.audioGetInfo_native(Index);
                            }
                            audioListSet(audioStrArrayList);
                        }
                    }
                });
                radioButton2.setOnKeyListener(new OnKeyListener() {
                    
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                            settingList.setNextFocusLeftId(R.id.radio_button1);
                            settingList.setNextFocusRightId(R.id.radio_button3);
                            settingList.setSelection(0);
                            settingList.requestFocus();
                        }
                        return false;
                    }
                });
                radioButton2.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        radioGroup.check(R.id.radio_button2);
                        audioStrArrayList = null;
                        audioStrArrayList = new CharSequence[CommonStaticData.audioNum];
                        for(int Index = 0; Index <= CommonStaticData.audioNum-1; Index++){
                            audioStrArrayList[Index] = CommonStaticData.jniIF.audioGetInfo_native(Index);
                        }
                        audioListSet(audioStrArrayList);
                    }
                });
                
                RadioButton radioButton3 = (RadioButton) detailDialog.findViewById(R.id.radio_button3);
                radioButton3.setOnFocusChangeListener(new OnFocusChangeListener() {
                    
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        if(hasFocus){
                            radioGroup.check(R.id.radio_button3);
                            ScaleListSet(getResources().getStringArray(R.array.array_scale_set));
                        }
                    }
                });
                radioButton3.setOnKeyListener(new OnKeyListener() {
                    
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                            settingList.setNextFocusLeftId(R.id.radio_button2);
                            settingList.setNextFocusRightId(R.id.radio_button4);
                            settingList.setSelection(0);
                            settingList.requestFocus();
                        }
                        return false;
                    }
                });
                radioButton3.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        radioGroup.check(R.id.radio_button3);
                        ScaleListSet(getResources().getStringArray(R.array.array_scale_set));
                    }
                });
                
                RadioButton radioButton4 = (RadioButton) detailDialog.findViewById(R.id.radio_button4);
                radioButton4.setOnFocusChangeListener(new OnFocusChangeListener() {
                    
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        if(hasFocus){
                            radioGroup.check(R.id.radio_button4);
//                            ScaleListSet(getResources().getStringArray(R.array.array_scale_set));
                            String [] infoStr = new String[4];
                            infoStr[0] = "Video PID: 0x"+Integer.toHexString(CommonStaticData.jniIF.getVideoPID_native());
                            infoStr[1] = "Audio PID: 0x"+Integer.toHexString(CommonStaticData.jniIF.getAudioPID_native());
                            
                            infoStr[2] = "Frequency: "+(mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ)/1000)+"."+
                                                +(mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ)%1000)+"MHZ";
                            infoStr[3] = "Bandwidth: "+mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_BW)+"KHz";
                            settingList.setAdapter(new ArrayAdapter<Object>(VideoPlayerActivity.this,android.R.layout.simple_list_item_1, infoStr));
                        }
                    }
                });
                radioButton4.setOnKeyListener(new OnKeyListener() {
                    
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                            settingList.setNextFocusLeftId(R.id.radio_button3);
                            settingList.setNextFocusRightId(R.id.radio_button0);
                            settingList.setSelection(0);
                            settingList.requestFocus();
                        }
                        return false;
                    }
                });
                radioButton4.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        radioGroup.check(R.id.radio_button4);
//                        ScaleListSet(getResources().getStringArray(R.array.array_scale_set));
//                        settingList.setAdapter(null);
                        String [] infoStr = new String[4];
                        infoStr[0] = "Video PID: 0x"+Integer.toHexString(CommonStaticData.jniIF.getVideoPID_native());
                        infoStr[1] = "Audio PID: 0x"+Integer.toHexString(CommonStaticData.jniIF.getAudioPID_native());
                        
                        infoStr[2] = "Frequency: "+(mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ)/1000)+"."+
                                            +(mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ)%1000)+"MHZ";
                        infoStr[3] = "Bandwidth: "+mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_BW)+"KHz";
                        settingList.setAdapter(new ArrayAdapter<Object>(VideoPlayerActivity.this,android.R.layout.simple_list_item_1, infoStr));

                    }
                });
                
                radioButton0.requestFocus();
                radioGroup.check(R.id.radio_button0);
                radioButton0.performClick();
                
                detailDialog.show();
            
                
                return false;
            }
        });

        btn_more.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

//                Dialog dialog = new Dialog(VideoPlayerActivity.this, R.style.MyDialog);
//                //设置它的ContentView
//                dialog.setContentView(R.layout.moresummary);
//
//                dialog.findViewById(R.id.more1).setOnClickListener(moreDialogClickListener);
//
//                dialog.findViewById(R.id.more2).setOnClickListener(moreDialogClickListener);
//
//                dialog.findViewById(R.id.more3).setOnClickListener(moreDialogClickListener);
//
//                dialog.findViewById(R.id.more4).setOnClickListener(moreDialogClickListener);
//
//                dialog.show();
                // TODO Auto-generated method stub
                Logger.e("++++++++++++++++moreDialogClickListener click:"+
                        v.getId());
                detailDialog = new Dialog(VideoPlayerActivity.this, R.style.MyDialog);
                //设置它的ContentView
                detailDialog.setContentView(R.layout.moredetail);
                settingList = (ListView)detailDialog.findViewById(R.id.settingList);
                final RadioGroup radioGroup = (RadioGroup)detailDialog.findViewById(R.id.main_tab);

                RadioButton radioButton0 = (RadioButton) detailDialog.findViewById(R.id.radio_button0);
                radioButton0.setOnFocusChangeListener(new OnFocusChangeListener() {
                    
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        if(hasFocus){
                            radioGroup.check(R.id.radio_button0);
                            subtitleStrArrayList = null;
                            subtitleStrArrayList = new CharSequence[CommonStaticData.subtitleNum+1];
                            subtitleStrArrayList[0] = getResources().getString(R.string.disable_subtitle);
                            for(int Index = 1; Index <= CommonStaticData.subtitleNum; Index++)
                            {
                                subtitleStrArrayList[Index] = CommonStaticData.jniIF.subtitleGetInfo_native(Index-1);
                            }
                            subtitleListSet(subtitleStrArrayList);
                        }
                    }
                });
                radioButton0.setOnKeyListener(new OnKeyListener() {
                    
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                            settingList.setNextFocusLeftId(R.id.radio_button4);
                            settingList.setNextFocusRightId(R.id.radio_button1);
                            settingList.setSelection(0);
                            settingList.requestFocus();
                        }
                        return false;
                    }
                });
                radioButton0.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        radioGroup.check(R.id.radio_button0);
                        subtitleStrArrayList = null;
                        subtitleStrArrayList = new CharSequence[CommonStaticData.subtitleNum+1];
                        subtitleStrArrayList[0] = getResources().getString(R.string.disable_subtitle);
                        for(int Index = 1; Index <= CommonStaticData.subtitleNum; Index++)
                        {
                            subtitleStrArrayList[Index] = CommonStaticData.jniIF.subtitleGetInfo_native(Index-1);
                        }
                        subtitleListSet(subtitleStrArrayList);
                    }
                });
                RadioButton radioButton1 = (RadioButton) detailDialog.findViewById(R.id.radio_button1);
                radioButton1.setOnFocusChangeListener(new OnFocusChangeListener() {
                    
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        if(hasFocus){
                            radioGroup.check(R.id.radio_button1);
                            teletxtStrArrayList = null;
                            teletxtStrArrayList = new CharSequence[CommonStaticData.teletxtNum+1];
                            teletxtStrArrayList[0] = getResources().getString(R.string.disable_teletxt);
                            for(int Index = 1; Index <= CommonStaticData.teletxtNum; Index++){
                                teletxtStrArrayList[Index] = CommonStaticData.jniIF.teletextGetInfo_native(Index-1);
                            }
                            teletxtListSet(teletxtStrArrayList);
                        }
                    }
                });
                radioButton1.setOnKeyListener(new OnKeyListener() {
                    
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                            settingList.setNextFocusLeftId(R.id.radio_button0);
                            settingList.setNextFocusRightId(R.id.radio_button2);
                            settingList.setSelection(0);
                            settingList.requestFocus();
                        }
                        return false;
                    }
                });
                radioButton1.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        radioGroup.check(R.id.radio_button1);
                        teletxtStrArrayList = null;
                        teletxtStrArrayList = new CharSequence[CommonStaticData.teletxtNum+1];
                        teletxtStrArrayList[0] = getResources().getString(R.string.disable_teletxt);
                        for(int Index = 1; Index <= CommonStaticData.teletxtNum; Index++){
                            teletxtStrArrayList[Index] = CommonStaticData.jniIF.teletextGetInfo_native(Index-1);
                        }
                        teletxtListSet(teletxtStrArrayList);
                    }
                });
                RadioButton radioButton2 = (RadioButton) detailDialog.findViewById(R.id.radio_button2);
                radioButton2.setOnFocusChangeListener(new OnFocusChangeListener() {
                    
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        if(hasFocus){
                            radioGroup.check(R.id.radio_button2);
                            audioStrArrayList = null;
                            audioStrArrayList = new CharSequence[CommonStaticData.audioNum];
                            for(int Index = 0; Index <= CommonStaticData.audioNum-1; Index++){
                                audioStrArrayList[Index] = CommonStaticData.jniIF.audioGetInfo_native(Index);
                            }
                            audioListSet(audioStrArrayList);
                        }
                    }
                });
                radioButton2.setOnKeyListener(new OnKeyListener() {
                    
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                            settingList.setNextFocusLeftId(R.id.radio_button1);
                            settingList.setNextFocusRightId(R.id.radio_button3);
                            settingList.setSelection(0);
                            settingList.requestFocus();
                        }
                        return false;
                    }
                });
                radioButton2.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        radioGroup.check(R.id.radio_button2);
                        audioStrArrayList = null;
                        audioStrArrayList = new CharSequence[CommonStaticData.audioNum];
                        for(int Index = 0; Index <= CommonStaticData.audioNum-1; Index++){
                            audioStrArrayList[Index] = CommonStaticData.jniIF.audioGetInfo_native(Index);
                        }
                        audioListSet(audioStrArrayList);
                    }
                });
                
                RadioButton radioButton3 = (RadioButton) detailDialog.findViewById(R.id.radio_button3);
                radioButton3.setOnFocusChangeListener(new OnFocusChangeListener() {
                    
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        if(hasFocus){
                            radioGroup.check(R.id.radio_button3);
                            ScaleListSet(getResources().getStringArray(R.array.array_scale_set));
                        }
                    }
                });
                radioButton3.setOnKeyListener(new OnKeyListener() {
                    
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                            settingList.setNextFocusLeftId(R.id.radio_button2);
                            settingList.setNextFocusRightId(R.id.radio_button4);
                            settingList.setSelection(0);
                            settingList.requestFocus();
                        }
                        return false;
                    }
                });
                radioButton3.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        radioGroup.check(R.id.radio_button3);
                        ScaleListSet(getResources().getStringArray(R.array.array_scale_set));
                    }
                });
                
                RadioButton radioButton4 = (RadioButton) detailDialog.findViewById(R.id.radio_button4);
                radioButton4.setOnFocusChangeListener(new OnFocusChangeListener() {
                    
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        if(hasFocus){
                            radioGroup.check(R.id.radio_button4);
//                            ScaleListSet(getResources().getStringArray(R.array.array_scale_set));
                            String [] infoStr = new String[4];
                            infoStr[0] = "Video PID: 0x"+Integer.toHexString(CommonStaticData.jniIF.getVideoPID_native());
                            infoStr[1] = "Audio PID: 0x"+Integer.toHexString(CommonStaticData.jniIF.getAudioPID_native());
                            
                            infoStr[2] = "Frequency: "+(mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ)/1000)+"."+
                                                +(mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ)%1000)+"MHZ";
                            infoStr[3] = "Bandwidth: "+mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_BW)+"KHz";
                            settingList.setAdapter(new ArrayAdapter<Object>(VideoPlayerActivity.this,android.R.layout.simple_list_item_1, infoStr));
                        }
                    }
                });
                radioButton4.setOnKeyListener(new OnKeyListener() {
                    
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                            settingList.setNextFocusLeftId(R.id.radio_button3);
                            settingList.setNextFocusRightId(R.id.radio_button0);
                            settingList.setSelection(0);
                            settingList.requestFocus();
                        }
                        return false;
                    }
                });
                radioButton4.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        radioGroup.check(R.id.radio_button4);
//                        ScaleListSet(getResources().getStringArray(R.array.array_scale_set));
//                        settingList.setAdapter(null);
                        String [] infoStr = new String[4];
                        infoStr[0] = "Video PID: 0x"+Integer.toHexString(CommonStaticData.jniIF.getVideoPID_native());
                        infoStr[1] = "Audio PID: 0x"+Integer.toHexString(CommonStaticData.jniIF.getAudioPID_native());
                        
                        infoStr[2] = "Frequency: "+(mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ)/1000)+"."+
                                            +(mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ)%1000)+"MHZ";
                        infoStr[3] = "Bandwidth: "+mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_BW)+"KHz";
                        settingList.setAdapter(new ArrayAdapter<Object>(VideoPlayerActivity.this,android.R.layout.simple_list_item_1, infoStr));

                    }
                });
                radioButton0.requestFocus();
                radioGroup.check(R.id.radio_button0);
                radioButton0.performClick();
                
                detailDialog.show();
            }
        });
        
        btn_teletxtprev1.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if(refurbishTeletxt(DVB_TELETEXT_FIND_PAGE_DIRECT_PREV_001_ACCURATE) == 0){
                    refurbishTeletxt(DVB_TELETEXT_FIND_PAGE_DIRECT_PREV_001_FUZZY);
                }
                return false;
            }
        });
        
        btn_teletxtprev1.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(refurbishTeletxt(DVB_TELETEXT_FIND_PAGE_DIRECT_PREV_001_ACCURATE) == 0){
                    refurbishTeletxt(DVB_TELETEXT_FIND_PAGE_DIRECT_PREV_001_FUZZY);
                }
            }
        });
        
        btn_teletxtnext1.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if(refurbishTeletxt(DVB_TELETEXT_FIND_PAGE_DIRECT_NEXT_001_ACCURATE) == 0){
                    refurbishTeletxt(DVB_TELETEXT_FIND_PAGE_DIRECT_NEXT_001_FUZZY);
                }
                return false;
            }
        });
        
        btn_teletxtnext1.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(refurbishTeletxt(DVB_TELETEXT_FIND_PAGE_DIRECT_NEXT_001_ACCURATE) == 0){
                    refurbishTeletxt(DVB_TELETEXT_FIND_PAGE_DIRECT_NEXT_001_FUZZY);
                }
            }
        });
        
        btn_teletxtprev100.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if(refurbishTeletxt(DVB_TELETEXT_FIND_PAGE_DIRECT_PREV_100_ACCURATE) == 0){
                    refurbishTeletxt(DVB_TELETEXT_FIND_PAGE_DIRECT_PREV_100_FUZZY);
                }
                return false;
            }
        });
        
        btn_teletxtprev100.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(refurbishTeletxt(DVB_TELETEXT_FIND_PAGE_DIRECT_PREV_100_ACCURATE) == 0){
                    refurbishTeletxt(DVB_TELETEXT_FIND_PAGE_DIRECT_PREV_100_FUZZY);
                }
            }
        });
        
        btn_teletxtnext100.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if(refurbishTeletxt(DVB_TELETEXT_FIND_PAGE_DIRECT_NEXT_100_ACCURATE) == 0){
                    refurbishTeletxt(DVB_TELETEXT_FIND_PAGE_DIRECT_NEXT_100_FUZZY);
                }
                return false;
            }
        });
        
        btn_teletxtnext100.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(refurbishTeletxt(DVB_TELETEXT_FIND_PAGE_DIRECT_NEXT_100_ACCURATE) == 0){
                    refurbishTeletxt(DVB_TELETEXT_FIND_PAGE_DIRECT_NEXT_100_FUZZY);
                }
            }
        });
        
        btn_teletxtgoto.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                NumberPickerDialog dialog = new NumberPickerDialog(VideoPlayerActivity.this, R.style.transDialog, 0);

                dialog.setTitle(getString(R.string.dialog_picker_title));
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                dialog.setOnNumberSetListener(VideoPlayerActivity.this);
                dialog.show();
                return false;
            }
        });
        
        btn_teletxtgoto.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                NumberPickerDialog dialog = new NumberPickerDialog(VideoPlayerActivity.this, R.style.transDialog, 0);

                dialog.setTitle(getString(R.string.dialog_picker_title));
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                dialog.setOnNumberSetListener(VideoPlayerActivity.this);
                dialog.show();

            }
        });
        
        btn_teletxtback.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                CommonStaticData.teletxtCheckedItem = 0;
                CommonStaticData.jniIF.teletextStop_native();
                clearTeletxt();
                cancelTeletxtDelayHide();
                hideTeletxtController();
                isTeletxtShow = false;
                isTeletxtControllerShow = false;
                
                showController();
                hideControllerDelay();
                return false;
            }
        });
        
        btn_teletxtback.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                CommonStaticData.teletxtCheckedItem = 0;
                CommonStaticData.jniIF.teletextStop_native();
                clearTeletxt();
                cancelTeletxtDelayHide();
                hideTeletxtController();
                isTeletxtShow = false;
                isTeletxtControllerShow = false;
                
                showController();
                hideControllerDelay();
            }
        });
        
        btn_back.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                bolStopSubtitle = true;
                CommonStaticData.jniIF.subtitleStop_native(0);
                VideoPlayerActivity.this.finish();
            
                return false;
            }
        });
        
        btn_back.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                bolStopSubtitle = true;
                CommonStaticData.jniIF.subtitleStop_native(0);
                VideoPlayerActivity.this.finish();
            }
            
        });
       
        mGestureDetector = new GestureDetector(new SimpleOnGestureListener(){
/*
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				// TODO Auto-generated method stub
				if(isFullScreen){
					setVideoScale(SCREEN_DEFAULT);
				}else{
					setVideoScale(SCREEN_FULL);
				}
				isFullScreen = !isFullScreen;
				Log.d(TAG, "onDoubleTap");
				
				if(isControllerShow){
					showController();
				}
				//return super.onDoubleTap(e);
				return true;
			}
*/
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				// TODO Auto-generated method stub
				if(!isControllerShow){
					showController();
					hideControllerDelay();
				}else {
					cancelDelayHide();
					hideController();
				}
				//return super.onSingleTapConfirmed(e);
				return true;
			}
/*
			@Override
			public void onLongPress(MotionEvent e) {
				// TODO Auto-generated method stub
				if(isPaused){
					vv.start();
					bn3.setImageResource(R.drawable.pause);
					cancelDelayHide();
					hideControllerDelay();
				}else{
					vv.pause();
					bn3.setImageResource(R.drawable.play);
					cancelDelayHide();
					showController();
				}
				isPaused = !isPaused;
				//super.onLongPress(e);
			}	*/
        });
                
        // vv.setVideoPath("http://202.108.16.171/cctv/video/A7/E8/69/27/A7E86927D2BF4D2FA63471D1C5F97D36/gphone/480_320/200/0.mp4");
        
        vv.setOnPreparedListener(new OnPreparedListener(){

				@Override
				public void onPrepared(MediaPlayer arg0) {
					// TODO Auto-generated method stub
					setVideoScale(screenSizeMode);
					if(isControllerShow){
						showController();  
					}

					/*controller.showAtLocation(vv, Gravity.BOTTOM, 0, 0);
					controller.update(screenWidth, controlHeight);
					myHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);*/
					
					vv.start();  
					hideControllerDelay();
				}	
	        });
        
        vv.setOnCompletionListener(new OnCompletionListener(){

				@Override
				public void onCompletion(MediaPlayer arg0) {
					// TODO Auto-generated method stub
				}
        	});

        dispSubtitle = new DisplaySubtitle(this);
        dispSubtitleThd = new Thread(dispSubtitle);
        dispSubtitleThd.start();
    }
    public void subtitleListSet(CharSequence[] arrayList){
        Logger.e("subtitle list set!!!!!!!!"+CommonStaticData.subtitleCheckedItem);
        settingList.setAdapter(new ArrayAdapter<Object>(VideoPlayerActivity.this,android.R.layout.simple_list_item_single_choice, arrayList));
        settingList.setItemsCanFocus(true); 
        settingList.setChoiceMode(settingList.CHOICE_MODE_SINGLE);
        settingList.setItemChecked(CommonStaticData.subtitleCheckedItem, true);
        settingList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                Logger.e("selected id:"+position);
                if(position != 0 && CommonStaticData.subtitleCheckedItem != position){
                    CommonStaticData.jniIF.subtitleStop_native(0);
                    bolStopSubtitle = false;
                    int ret = CommonStaticData.jniIF.subtitlePlay_native((int)position-1);
                    Logger.e("subtitlePlay_native:"+ret);
                }
                else if(position == 0){
                    Logger.e("before stop!!!!!!!!");
                    bolStopSubtitle = true;
                    CommonStaticData.jniIF.subtitleStop_native(-1);
                    myHandler.sendEmptyMessage(CLEAR_SUBTITLE);
                    Logger.e("after stop!!!!!!!!");
                }
                CommonStaticData.subtitleCheckedItem = (int)position;
		  detailDialog.dismiss();
            }
        });
    }
    public void teletxtListSet(CharSequence[] arrayList){
        settingList.setAdapter(new ArrayAdapter<Object>(VideoPlayerActivity.this,android.R.layout.simple_list_item_single_choice, arrayList));
        settingList.setItemsCanFocus(true); 
        settingList.setChoiceMode(settingList.CHOICE_MODE_SINGLE);
        settingList.setItemChecked(CommonStaticData.teletxtCheckedItem, true);
        settingList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                Logger.e("selected id:"+position);
              if(position != 0 && CommonStaticData.teletxtCheckedItem != position){
                myHandler.removeMessages(CLEAR_SUBTITLE);
                CommonStaticData.jniIF.teletextStop_native();
                int ret = CommonStaticData.jniIF.teletextPlay_native((int)position-1);
                refurbishTeletxt(find_page_direction);
                Logger.e("teletextPlay_native:"+ret);
                
                cancelDelayHide();
                hideController();
                showTeletxtController();
                hideTeletxtControllerDelay();
              }
              else if(position == 0){
                CommonStaticData.jniIF.teletextStop_native();
                clearTeletxt();
    //            myHandler.sendEmptyMessage(CLEAR_TELETEXT);
              }
              CommonStaticData.teletxtCheckedItem = (int)position;
		detailDialog.dismiss();
            }
        });
    }
    
    public void audioListSet(CharSequence[] arrayList){
        settingList.setAdapter(new ArrayAdapter<Object>(VideoPlayerActivity.this,android.R.layout.simple_list_item_single_choice, arrayList));
        settingList.setItemsCanFocus(true); 
        settingList.setChoiceMode(settingList.CHOICE_MODE_SINGLE);
        settingList.setItemChecked(CommonStaticData.audioCheckedItem, true);
        settingList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
              if(CommonStaticData.audioCheckedItem != position){
                  playVideoProcess((int)position);
              }

              CommonStaticData.audioCheckedItem = (int)position;
		detailDialog.dismiss();
            }
        });
    }
    
    public void ScaleListSet(CharSequence[] arrayList){
        settingList.setAdapter(new ArrayAdapter<Object>(VideoPlayerActivity.this,android.R.layout.simple_list_item_single_choice, arrayList));
        settingList.setItemsCanFocus(true); 
        settingList.setChoiceMode(settingList.CHOICE_MODE_SINGLE);
        settingList.setItemChecked(screenSizeMode, true);
        settingList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
              if(screenSizeMode != position){
                  setVideoScale((int)position);
              }
              screenSizeMode = (int)position;
		detailDialog.dismiss();
            }
        });
    }
    
    private final int HIDE_CONTROLER = 1;
    public final int UPDATE_SUBTITLE = 2;
    public final int CLEAR_SUBTITLE = 3;
    
    public final int UPDATE_TELETEXT = 4;
    public final int CLEAR_TELETEXT = 5;
    
    public final int HIDE_TELETXT_CONTROLER = 6;
    
    public final int NO_SIGNAL_DISP = 7;
    public final int NO_SIGNAL_DISMISS = 8;
    
    public boolean bolDispCmp = true;
    public static boolean bolStopSubtitle = true;
    public boolean bolNoSignal = false;
    public Handler myHandler = new Handler(){
    
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			switch(msg.what){
				case HIDE_CONTROLER:
					hideController();
					break;
				case UPDATE_SUBTITLE:
				    refurbishSubtitle();
				    bolDispCmp = true;
				    break;
				case CLEAR_SUBTITLE:
                    clearSubtitle();
                    break;
				case HIDE_TELETXT_CONTROLER:
                    hideTeletxtController();
                    break;
                    
				case NO_SIGNAL_DISP:	    
				    vv.setBackgroundResource(R.drawable.nosignal);
			            bolNoSignal = true;
				    break;
				    
				case NO_SIGNAL_DISMISS:
				    vv.setBackgroundResource(0);
                                    bolNoSignal = false;
				    break;
			}
			
			super.handleMessage(msg);
		}	
    };
    /* subtitle bitmap */
    public void refurbishSubtitle(){
        Bitmap subBitmap = Bitmap.createBitmap(CommonStaticData.subW, CommonStaticData.subH,Bitmap.Config.ARGB_8888);
        if(subBitmap == null) {
            Logger.e("create subtitle bitmap failed!");
            return;
        }
        int ret = CommonStaticData.jniIF.subtitleGetDispData_native(subBitmap);
        if(ret == 0) {
            subBitmap = null;
            return;
        }
        Logger.e(" subtitleDataRet:"+ret);
//        subBitmap.eraseColor(Color.BLACK);
//        subBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.epg);
        Bitmap resizeBmp = Bitmap.createBitmap(subBitmap, 0, 0, CommonStaticData.subW, CommonStaticData.subH, matrix, true);
//        Logger.e("resizeW:"+resizeBmp.getWidth()+" resizeH:"+resizeBmp.getHeight());

        contentImage.setImageBitmap(resizeBmp);
        contentView.invalidate();
        
        subBitmap = null;
        resizeBmp = null;
        System.gc();
    }
    
    public void clearSubtitle(){
        Logger.e("clear subtitle");
        if(contentImage != null){
            contentImage.setImageBitmap(null);
            contentView.invalidate();
        }
    }
    
    public final int DVB_TELETEXT_FIND_PAGE_DIRECT_FIRST     = 0x00;  /* 转到第1 页TELETEXT*/
    public final int DVB_TELETEXT_FIND_PAGE_DIRECT_PREV_001_FUZZY  = 0x01;  /*  模糊转到前1 页TELETEXT*/
    public final int DVB_TELETEXT_FIND_PAGE_DIRECT_PREV_001_ACCURATE = 0x02;  /*  精确转到前1 页TELETEXT*/
    public final int DVB_TELETEXT_FIND_PAGE_DIRECT_NEXT_001_FUZZY  = 0x03;  /*  模糊转到后1 页TELETEXT*/
    public final int DVB_TELETEXT_FIND_PAGE_DIRECT_NEXT_001_ACCURATE  = 0x04;  /*精确 转到后1 页TELETEXT*/
    public final int DVB_TELETEXT_FIND_PAGE_DIRECT_PREV_100_FUZZY  = 0x05;  /* 模糊转到前100 页TELETEXT*/
    public final int DVB_TELETEXT_FIND_PAGE_DIRECT_PREV_100_ACCURATE  = 0x06;  /*精确 转到前100 页TELETEXT*/
    public final int DVB_TELETEXT_FIND_PAGE_DIRECT_NEXT_100_FUZZY  = 0x07;  /* 模糊转到后1 00 页TELETEXT*/
    public final int DVB_TELETEXT_FIND_PAGE_DIRECT_NEXT_100_ACCURATE  = 0x08;  /*精确 转到后1 00 页TELETEXT*/
    public final int DVB_TELETEXT_FIND_PAGE_DIRECT_Cur       = 0x09;
    public int find_page_direction = DVB_TELETEXT_FIND_PAGE_DIRECT_FIRST;
    /* subtitle bitmap */
    public int refurbishTeletxt(int page_dir){
        Logger.e("refurbishTeletxt!!!!!!");
        Bitmap teletxtBitmap = Bitmap.createBitmap(CommonStaticData.teletxtW, CommonStaticData.teletxtH,Bitmap.Config.ARGB_8888);
        int ret = 0;
	 int 	time_out = 100;
        do{
             ret = CommonStaticData.jniIF.teletextGetPageByDirection_native(page_dir,teletxtBitmap);
             
        }while(page_dir == DVB_TELETEXT_FIND_PAGE_DIRECT_FIRST && ret == 0 && time_out-- >  0);
        Logger.e(" teletxtDataRet:"+ret);
//        if(ret == 0) {
//            return 0;
//        }
//        Bitmap teletxtBitmap = Bitmap.createBitmap(300, 290,Bitmap.Config.ARGB_8888);
//        Bitmap teletxtBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.epg);
//        Bitmap resizeBmp = teletxtBitmap;/*TBD*/
        Bitmap resizeBmp = Bitmap.createBitmap(teletxtBitmap, 0, 0, CommonStaticData.teletxtW, CommonStaticData.teletxtH, matrix, true);/*TBD*/
//        Logger.e("resizeW:"+resizeBmp.getWidth()+" resizeH:"+resizeBmp.getHeight());
        contentImage.setImageBitmap(resizeBmp);
        contentView.invalidate();
//        teletxtBitmap = null;
//        resizeBmp = null;
//        System.gc();
        return ret;
    }
    
    public int gotoSpecifyTeletxt(int pageNum){
        Bitmap teletxtBitmap = Bitmap.createBitmap(CommonStaticData.teletxtW, CommonStaticData.teletxtH,Bitmap.Config.ARGB_8888);
        int ret = CommonStaticData.jniIF.teletextGetPageByNum_native(pageNum,teletxtBitmap);
        if(ret == 0){
            return 0;
        }
        Bitmap resizeBmp = Bitmap.createBitmap(teletxtBitmap, 0, 0, CommonStaticData.teletxtW, CommonStaticData.teletxtH, matrix, true);
//      Logger.e("resizeW:"+resizeBmp.getWidth()+" resizeH:"+resizeBmp.getHeight());

        contentImage.setImageBitmap(resizeBmp);
        contentView.invalidate();
        return ret;
    }
    public void clearTeletxt(){
        Logger.e("clear teletxt");
        if(contentImage != null){
            contentImage.setImageBitmap(null);
            contentView.invalidate();
        }
    }
    @Override
    protected void onActivityResult(int requestcode, int resultCode, Intent data){
//        int position = data.getIntExtra("position", 0);
//        Logger.e("position::::::::::::::"+position+"########:"+mCursor.getPosition());
//        mCursor.moveToPosition(position);
//        Logger.e("position::::::::::::::"+position+"########111:"+mCursor.getPosition());
//        serviceName.setText(mCursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_NAME));
    }
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		
		boolean result = mGestureDetector.onTouchEvent(event);
		
		if(!result){
			if(event.getAction()==MotionEvent.ACTION_UP){
				
				/*if(!isControllerShow){
					showController();
					hideControllerDelay();
				}else {
					cancelDelayHide();
					hideController();
				}*/
			}
			result = super.onTouchEvent(event);
		}
		
		return result;
	}
	
    private void setOnSystemUiVisibilityChangeListener() {
        // When the user touches the screen or uses some hard key, the framework
        // will change system ui visibility from invisible to visible. We show
        // the media control and enable system UI (e.g. ActionBar) to be visible at this point
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                int diff = mLastSystemUiVis ^ visibility;
                mLastSystemUiVis = visibility;
                if ((diff & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0
                        && (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                    //mVideoController.show();
                    controller.update(0,0,screenWidth, controlHeight);
                }
            }
        });
    }

    public void showSystemUi(boolean visible) {
       int flag = visible? 0:View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().getDecorView().setSystemUiVisibility(flag);
		    //rootView.setSystemUiVisibility(flag);
    }	

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
	    Logger.e("VideoPlayerActivity onConfigurationChanged");
		getScreenSize();
		if(isControllerShow){
			cancelDelayHide();
			hideController();
			showController();
			hideControllerDelay();
		}
		
		super.onConfigurationChanged(newConfig);
	}
	
    /***************************************************************************************************************/
    /*!
     *  @brief  Called when the activity is paused.
     *  @note   This function treats following :\n
     *              - Pause the activity.
     *              - If vv is playing, stop play.
     *              - Save mCursor's current position.
     *  @param  none.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
	int curPosSav = -1;
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
//		vv.pause();
	    Logger.e("VideoPlayerActivity onPause:"+mCursor.getPosition());
		super.onPause(); 
		    bolStopSubtitle = true;
            CommonStaticData.jniIF.subtitleStop_native(0);
            vv.stopPlay();
		curPosSav = mCursor.getPosition();
	}

	/***************************************************************************************************************/
    /*!
     *  @brief  Called when the activity is resumed.
     *  @note   This function treats following :\n
     *              - Resume the activity.
     *              - Move mCursor to the position saved.
     *  @param  none.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		Logger.d("cursor pos::::::::::"+mCursor.getPosition());
		if(curPosSav != -1){
		    Logger.e("EPG return play video,curPos:"+curPosSav);
		    mCursor.moveToPosition(curPosSav);
		
	            favFlag = mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FAV);
	                serviceName.setText(String.format("%03d", (mCursor.getPosition()+1))+"  "+mCursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_NAME));
		        playVideoProcess(CommonStaticData.jniIF.getDefalltAudioIndex_native( mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ),
					                                                         mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID) ));
	            
	            if(favFlag == 1){
	                btn_fav.setBackgroundResource(R.drawable.button_states7_1);
	            }
	            else{
	                btn_fav.setBackgroundResource(R.drawable.button_states7);
	            }
		}
		
		if(!isControllerShow) {
                  showController();
		}
	}

    /***************************************************************************************************************/
    /*!
     *  @brief  Called when the activity is destroyed.
     *  @note   This function treats following :\n
     *              - Save screen size mode.
     *              - Dismiss controller, extralWindow and mSoundWindow.
     *              - remove HIDE_CONTROLER messages from myHandler.
     *              - If vv is playing, stop play.
     *              - Close cursor.
     *  @param  none.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
        Logger.e("onDestroy");
        threadSuspended = true;
//        CommonStaticData.jniIF.subtitleStop_native(0);
        myHandler.removeMessages(UPDATE_SUBTITLE);
        myHandler.removeMessages(CLEAR_SUBTITLE);
	    SharedPreferences.Editor editor = CommonStaticData.settings.edit();
        editor.putInt(CommonStaticData.scrSizeModeKey, screenSizeMode);
        editor.commit();
        if(contentWindow.isShowing()){
            contentWindow.dismiss();
        }
//        if(teletxtWindow.isShowing()){
//            teletxtWindow.dismiss();
//        }
		if(controller.isShowing()){
			controller.dismiss();
			extralWindow.dismiss();
		}
		if(mSoundWindow.isShowing()){
			mSoundWindow.dismiss();
		}
		if(teletxtController.isShowing()){
		    teletxtController.dismiss();
		}
		
		myHandler.removeMessages(HIDE_CONTROLER);
		myHandler.removeMessages(HIDE_TELETXT_CONTROLER);
		vv.stopPlay();
		
		mCursor.close();
		super.onDestroy();
	}     

    /***************************************************************************************************************/
    /*!
     *  @brief  Get the device screen size.
     *  @note   This function treats following :\n
     *              - Get the device screen size.
     *              - Set top and bottom PopupWindow height.
     *  @param  none.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    private void getScreenSize()
	{
		Display display = getWindowManager().getDefaultDisplay();
        screenHeight = display.getHeight();
        screenWidth = display.getWidth();
        controlHeight = 150;//screenHeight/4;
        teletxtControlHeight = 78;
        extraHeight = 70;
	}
	
    /***************************************************************************************************************/
    /*!
     *  @brief  Hide PopupWindow.
     *  @note   This function treats following :\n
     *              - If PopupWindow is showing, hide it.
     *  @param  none.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    private void hideController(){
		if(controller.isShowing()){
			controller.update(0,0,0, 0);
			extralWindow.update(1080,0,0,0);
			isControllerShow = false;
		}
		if(mSoundWindow.isShowing()){
			mSoundWindow.dismiss();
		}
	}
	
	private void hideControllerDelay(){
		myHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
	}
	
    private void hideTeletxtController(){
        if(teletxtController.isShowing()){
            teletxtController.update(0,0,0, 0);
            isTeletxtControllerShow = false;
        }
    }
    
    private void hideTeletxtControllerDelay(){
        myHandler.sendEmptyMessageDelayed(HIDE_TELETXT_CONTROLER, TIME);
    }
    
    /***************************************************************************************************************/
    /*!
     *  @brief  Show PopupWindow.
     *  @note   This function treats following :\n
     *              - Show PopupWindow.
     *  @param  none.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    private void showController(){
	    showPromptProgram();
		controller.update(0,0,screenWidth, controlHeight);
		extralWindow.update(0,0,screenWidth, extraHeight);
		
		isControllerShow = true;
		
	}
	
	private void cancelDelayHide(){
		myHandler.removeMessages(HIDE_CONTROLER);
	}
	
    private void showTeletxtController(){
        teletxtController.update(0,0,screenWidth, teletxtControlHeight);
        isTeletxtShow = true;
        isTeletxtControllerShow = true;
        
    }
    
    private void cancelTeletxtDelayHide(){
        myHandler.removeMessages(HIDE_TELETXT_CONTROLER);
    }
    public int encrypt;
    private void playVideoProcess(int audioId){
        encrypt = mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ENCRYPT);
        if(encrypt == 1){
            vv.stopPlay();
//            Bitmap encryptBmp = BitmapFactory.decodeResource(VideoPlayerActivity.this.getResources(), R.drawable.nosignalbig);
//            
////          Bitmap resizeBmp = Bitmap.createBitmap(noSignalBmp, 0, 0, CommonStaticData.teletxtW, CommonStaticData.teletxtH, matrix, true);/*TBD*/
////          contentImage.setImageBitmap(resizeBmp);
//            contentImage.setImageBitmap(encryptBmp);
//            contentView.invalidate();
            vv.setBackgroundResource(R.drawable.scramblechannel);
        }
        else{
//            contentImage.setImageBitmap(null);
//            contentView.invalidate();
            vv.setBackgroundResource(0);
            vv.displayWaiting();
            playVideo = new PlayVideo(vv,
                    mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ),
                    mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID),
                    audioId);
            playVideoThd = new Thread(playVideo);
            playVideoThd.start();
            showPromptProgram();
        }

       ImageView iconImageview = (ImageView)extralView.findViewById(R.id.serviceicon);
        if(mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_TYPE)
                == Integer.parseInt(CommonStaticData.SERVICE_TYPE_RADIO)){
		iconImageview.setImageResource(R.drawable.radio_icon);
	}
	else
	{
           iconImageview.setImageResource(R.drawable.tv_icon_small);
	}		
		
        cancelDelayHide();
        hideControllerDelay();
    }
    /***************************************************************************************************************/
    /*!
     *  @brief  Show current program and next broadcast program.
     *  @note   This function treats following :\n
     *              - Call jniIF.DVBGetEitCount_native().
     *              - Set promptProgram textview if exists.
     *  @param  none.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    private void showPromptProgram(){
	    int PromptInfoCount = CommonStaticData.jniIF.DVBGetEitCount_native();
	    
        if(PromptInfoCount > 0){
            //promptProgramCurr.setText(CommonStaticData.jniIF.DVBGetEitInfo_native(0));
            promptProgramCurr.setText(CommonStaticData.FormatToGbkByte(CommonStaticData.jniIF.DVBGetEitInfoByteArray_native(0), 21));
        }
        
        if(PromptInfoCount > 1){
            //promptProgramNext.setText(CommonStaticData.jniIF.DVBGetEitInfo_native(1));
            promptProgramCurr.setText(CommonStaticData.FormatToGbkByte(CommonStaticData.jniIF.DVBGetEitInfoByteArray_native(1), 21));            
        }
	}
    
    /***************************************************************************************************************/
    /*!
     *  @brief  When a key was pressed down , set focus.
     *  @note   This function treats following :\n
     *              - If isControllerShow is false, showController.
     *              - Else cancelDelayHide.
     *              - Call hideControllerDelay to start hide delay timer.
     *              - According to different keyCode, set next focus object.
     *  @param  keyCode     [in] The value in event.getKeyCode().
     *          event       [in] Description of the key event.

     *  @return true: prevent this event from being propagated further.
     *          false: it should continue to be propagated.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    private int focusID;
    private int teletxtFocusID;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
    {
    	int  keycode1 =  keyCode;
	    Logger.e("onKeyDown!!!!!!!!!!!!!!!!!!!!!!!!!!!!   keycode = " + keycode1 + "   !!!!!");
        if(!isTeletxtShow){
            if(!isControllerShow){
                showController();
                return super.onKeyDown(keyCode, event);	//pop up controller bar		
            }else {
                cancelDelayHide();
            }
            hideControllerDelay();
        }
        else{
            if(!isTeletxtControllerShow){
                showTeletxtController();
                return super.onKeyDown(keyCode, event);	//pop up controller bar							
            }else {
                cancelTeletxtDelayHide();
            }
            hideTeletxtControllerDelay();
        }
        
	    if((keyCode == KeyEvent.KEYCODE_DPAD_CENTER) || (keyCode == 23) || (keyCode == 66)){
	        if(!isTeletxtShow){
	    	        if(btn_prev.hasFocus()){
	    	            btn_prev.dispatchKeyEvent(event);
	                }
	    	        else if(btn_next.hasFocus()){
	    	            btn_next.dispatchKeyEvent(event);
	    	        }
	    	        else if(btn_servicelist.hasFocus()){
	    	            btn_servicelist.dispatchKeyEvent(event);
	                }
	                else if(btn_soundup.hasFocus()){
	                    btn_soundup.dispatchKeyEvent(event);
	                }
	                else if(btn_sounddown.hasFocus()){
	                    btn_sounddown.dispatchKeyEvent(event);
	                }
	                else if(btn_epg.hasFocus()){
	                    btn_epg.dispatchKeyEvent(event);
	                }
	                else if(btn_fav.hasFocus()){
	                    btn_fav.dispatchKeyEvent(event);
	                }
	                else if(btn_help.hasFocus()){
	                    btn_help.dispatchKeyEvent(event);
	                }
	                else if(btn_more.hasFocus()){
	                    Logger.e("btn_more focus!!!!!!!!!!");
	                    btn_more.dispatchKeyEvent(event);
	                }
	                else if(btn_back.hasFocus()){
	                    btn_back.dispatchKeyEvent(event);
	                }
			  else {
	                    focusID = 5;
	                    btn_more.requestFocus();
                       }			  	
	        }
	        else{
	                if(btn_teletxtprev1.hasFocus()){
	                    btn_teletxtprev1.dispatchKeyEvent(event);
	                }
	                else if(btn_teletxtnext1.hasFocus()){
	                    btn_teletxtnext1.dispatchKeyEvent(event);
	                }
	                else if(btn_teletxtprev100.hasFocus()){
	                    btn_teletxtprev100.dispatchKeyEvent(event);
	                }
	                else if(btn_teletxtnext100.hasFocus()){
	                    btn_teletxtnext100.dispatchKeyEvent(event);
	                }
	                else if(btn_teletxtgoto.hasFocus()){
	                    btn_teletxtgoto.dispatchKeyEvent(event);
	                }
	                else if(btn_teletxtback.hasFocus()){
	                    btn_teletxtback.dispatchKeyEvent(event);
	                }
			  else {
				focusID = 0;
    	                    btn_prev.requestFocus();
			}
	        }
	    }
	    else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
	        if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
	            if(!isTeletxtShow){
	                focusID++;
	            }
	            else{
	                teletxtFocusID++;
	            }
	        }
	        else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
	            if(!isTeletxtShow){
                    focusID--;
                    if(focusID < 0){
                        focusID = 6;//8;
                    }
	            }
	            else{
	                teletxtFocusID--;
	                if(teletxtFocusID < 0){
	                    teletxtFocusID = 5;
                    }
	            }
            }
	        if(!isTeletxtShow){
    	        focusID %= 7;//9;
    	        if(focusID == 0){
    	            btn_prev.requestFocus();
                    txt_btnPrompt.setText("Prev");
    	        }
    	        else if(focusID == 1){
    	            btn_next.requestFocus();
                    txt_btnPrompt.setText("Next");
    	        }
                else if(focusID == 2){
                    btn_servicelist.requestFocus();
                    txt_btnPrompt.setText("List");
                }
                else if(focusID == 3){
                    btn_epg.requestFocus();
                    txt_btnPrompt.setText("EPG");
                }
                else if(focusID == 4){
                    btn_fav.requestFocus();
                    txt_btnPrompt.setText("Fav");
                }
                else if(focusID == 5){
                    btn_more.requestFocus();
                    txt_btnPrompt.setText("More");
                }
                else if(focusID == 6){
                    btn_back.requestFocus();
                    txt_btnPrompt.setText("Back");
                }
	        }
	        else{
	            Logger.e("teletxt focus");
	            teletxtFocusID %= 6;
                if(teletxtFocusID == 0){
                    btn_teletxtprev1.requestFocus();
                }
                else if(teletxtFocusID == 1){
                    btn_teletxtnext1.requestFocus();
                }
                else if(teletxtFocusID == 2){
                    btn_teletxtprev100.requestFocus();
                }
                else if(teletxtFocusID == 3){
                    btn_teletxtnext100.requestFocus();
                }
                else if(teletxtFocusID == 4){
                    btn_teletxtgoto.requestFocus();
                }
                else if(teletxtFocusID == 5){
                    btn_teletxtback.requestFocus();
                }
	        }
	    }
        return super.onKeyDown(keyCode, event);
    }
	
    /***************************************************************************************************************/
    /*!
     *  @brief  Set video view scale.
     *  @note   This function treats following :\n
     *              - Call setVideoScale to set different video view scale
     *  @param  flag.   [in] Video scale mode.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    private final static byte SCREEN_FULL = 0;
    private final static byte SCREEN_FIT = 1;
    private final static byte SCREEN_16_9 = 2;
    private final static byte SCREEN_4_3 = 3;
    private final static byte SCREEN_HALF = 4;
    
    private void setVideoScale(int flag){
    	switch(flag){
    		case SCREEN_FULL:
    			Log.d(TAG, "screenWidth: "+screenWidth+" screenHeight: "+screenHeight);
    			vv.setVideoScale(screenWidth, screenHeight);
    			break;
    			
    		case SCREEN_FIT:
    			int videoWidth = vv.getVideoWidth();
    			int videoHeight = vv.getVideoHeight();
    		    vv.setVideoScale(videoWidth, videoHeight);
    			break;
    			
    		case SCREEN_16_9:
    		    vv.setVideoScale(screenWidth, screenWidth/16*9);
    		    break;
    		case SCREEN_4_3:
    		    vv.setVideoScale(screenWidth, screenWidth/4*3);
    		    break;
    		case SCREEN_HALF:
    		    vv.setVideoScale(screenWidth/2, screenHeight/2);
    		    break;
    	}
    }

    /***************************************************************************************************************/
    /*!
     *  @brief  TBD(Removable)
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    private int findAlphaFromSound(){
    	if(mAudioManager!=null){
    		//int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    		int alpha = currentVolume * (0xCC-0x55) / maxVolume + 0x55;
    		return alpha;
    	}else{
    		return 0xCC;
    	}
    }

    private void updateVolume(int index){
    	if(mAudioManager!=null){
    		if(isSilent){
    			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
    		}else{
    			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
    		}
    		currentVolume = index;
//    		btn_soundup.setAlpha(findAlphaFromSound());
    	}
    }
    public void onNumberSet(int number) {
        Log.d("NumberPicker", "Number selected: " + number);
        gotoSpecifyTeletxt(number);
    }
    /***************************************************************************************************************/
    /*!
     *  @brief  SubtitleBitmapView class.
     *  @note   This function treats following :\n
     *              - draw view if its visible.
     *  @date   2011/12/01 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    public class SubtitleBitmapView extends View {
        public Bitmap mBitmap;
        public SubtitleBitmapView(Context context, Bitmap bitmap) {
            super(context);
            
            mBitmap = bitmap;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            setMeasuredDimension(
                      mBitmap.getWidth(),
                      mBitmap.getHeight());
        }
        @Override
        protected void onDraw(Canvas canvas) {
            Logger.e("SubtitleBitmap ondraw!!!!!");
            canvas.drawBitmap(mBitmap, 50.0f, 50.0f, null);
        }
    }
}

