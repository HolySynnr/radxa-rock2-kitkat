/********************************************************************************************************************/
/**
 *  @skip   $Id:$
 *  @file   RecordActivity.java
 *  @brief  Record Programs.
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

import android.app.AlertDialog;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordActivity extends Activity {
	public Uri mUri;
	public ProgressDialog myDialog;
	/* progressDialog message string */
	public final CharSequence strDialogTitle = "Please for wait for some minutes...";
	public final CharSequence strDialogBody = "Recording...";
	private Cursor mCursor;
	private int recordResult;
	private Runnable  RecordRunnable = null;	
	private Thread RecordThread = null;	
	private Handler RecordHandler = null;

	private final int RECORD_FINISH = 1;
	private final int RECORD_FAILED = 2;
	private final int RECORD_SUCCESS = 3;	
	
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     
        setContentView(R.layout.record_program);

        myDialog = ProgressDialog.show(RecordActivity.this, strDialogTitle, strDialogBody,true);

        Intent intent = getIntent();
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
            String topBarStr1 = mCursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_NAME);
            Logger.e("topbarstr11111:"+topBarStr1);
        }
        else{
            mCursor.moveToFirst();
        }			

	    /* Record Handler */		
    	RecordHandler = new Handler()
    	{
    		public void handleMessage(Message msg)
    		{
    			switch(msg.what)
    			{
	    			case RECORD_FINISH:
	    			{
					myDialog.dismiss();
					buildRecordFinishDialog(recordResult);							
	    			}
	    			break;
	    			
	    			default:
	    			{
	    			}
	    			break;   			
	    		}
    		}
    	};		
    }

	protected void onStart() 
	{	
		super.onStart();	
	    	RecordRunnable = new Runnable()  
	    	{
			public void run() 
			{
				Logger.e("execute into the function run in RecordRunnable");
				recordResult = CommonStaticData.jniIF.DVBRecordProgram_native(mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ),mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ID));
				Message Msg = RecordHandler.obtainMessage(RECORD_FINISH);
				RecordHandler.sendMessage(Msg);
				Logger.e("execute outof the function run in RecordRunnable");
			}		
	    	};
			
		RecordThread = new Thread(RecordRunnable);
		RecordThread.start();			
	}	

	protected void onPause() {
	    super.onPause();   
           CommonStaticData.jniIF.DVBStopRecordProgram_native();
	}	

    protected void onDestroy() {
        super.onDestroy();
    } 		
	
    public void buildRecordFinishDialog(final int result) {
        new AlertDialog.Builder(RecordActivity.this).setTitle(R.string.recordFinish)
            .setMessage((result !=0) ? R.string.recordFailed : R.string.recordSuccess)
            .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                     dialog.cancel();
		       finish();			
                }
            })
            .show();
    }	
}


    
