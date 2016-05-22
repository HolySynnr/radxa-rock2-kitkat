/********************************************************************************************************************/
/**
 *  @skip   $Id:$
 *  @file   ServiceListActivity.java
 *  @brief  Displays a list of services. Will display notes from the {@link Uri}
 * 			provided in the intent if there is one, otherwise defaults to displaying the
 * 			contents of the {@link TVProgramProvider}
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
import com.rockchip.tvbox.utils.ExcuteScan;
import com.rockchip.tvbox.utils.Logger;
import com.rockchip.tvbox.utils.ScanPrograms;

import android.app.AlertDialog;
import android.app.ListActivity;
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
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceListActivity extends ListActivity implements NumberPickerDialog.OnNumberSetListener,  
OnScrollListener{
    public volatile boolean threadSuspended;
    public Uri mUri;
    public ProgressDialog myDialog;
    /* progressDialog message string */
    public final CharSequence strDialogTitle = "waiting...";
    public final CharSequence strDialogBody = "Scaning...";


    private Cursor mCursor;
    private int menuID;
    
    public SimpleCursorAdapter adapter;
//    public View mLoadLayout;
    
    public ProgressBar scanProgressBar;
    public TextView scanFreqText;
    public TextView scanDTVResultText;
    public TextView scanRadioResultText;
    
    public Thread scanProgramthd;
    /***************************************************************************************************************/
    /*!
     *  @brief  Called when the activity is first created.
     *  @note   This function treats following :\n
     *              - case bolHasChannel is false
     *                     Start scanPrograms thread to scan channels
     *              - Query the URI of the content provider and set to mCursor.
     *              - Set list adapter.
     *              - case CommonStaticData.bolHasChannel && mCursor.getCount() <= 0
     *                      Show toast about "no content" and finish this activity.
     *  @param  savedInstanceState      [in] Bundle
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /* set full screen */
        CommonStaticData.setFullScreen(this);
        
        setContentView(R.layout.servicelist);
        
        scanProgressBar = (ProgressBar)this.findViewById(R.id.scanprogressbar);
        scanFreqText = (TextView)this.findViewById(R.id.scantitle);
        scanDTVResultText = (TextView)this.findViewById(R.id.scandtvresult);
        scanRadioResultText = (TextView)this.findViewById(R.id.scanradioresult);
//        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
//        findViewById(R.id.scanlayout).setVisibility(View.GONE);
//        ListView programList = (ListView) findViewById(R.id.programList);
        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        Intent intent = getIntent();
        mUri = intent.getData();
        if (mUri == null) {
            mUri = Programs.CONTENT_URI;
            intent.setData(mUri);
        }
        
        // Inform the list we provide context menus for items
//        getListView().setOnCreateContextMenuListener(this);
        
        if(!CommonStaticData.bolHasChannel){
//            myDialog = ProgressDialog.show(ServiceListActivity.this, strDialogTitle, strDialogBody,true);
//            if(CommonStaticData.bolScanning){
//                CommonStaticData.bolCancelScan = true;
//            }
            getContentResolver().delete(mUri, null, null);
            
            addListFooterView();
            this.findViewById(R.id.scanlayout).setVisibility(View.VISIBLE);
            this.getListView().setEnabled(false);
//            this.getListView().setClickable(false);
            ScanPrograms scanPrograms = new ScanPrograms(ServiceListActivity.this);
            scanProgramthd = new Thread(scanPrograms);
            scanProgramthd.start();
        }
//        else if(CommonStaticData.bolScanning){
//            addListFooterView();
//        }
        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        //Logger.d("URI:"+mUri);
        menuID = intent.getIntExtra("menu_id", 0);
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
            //mCursor = managedQuery(mUri, CommonStaticData.PROJECTION, null, null,orderSetStr);
            mCursor = managedQuery(mUri, CommonStaticData.PROJECTION, Programs.TYPE + "=?", 
                CommonStaticData.selectionArgsTV,
                orderSetStr);
        }
        else if(menuID == CommonStaticData.MENU_ID_FAVORITE){
            mCursor = managedQuery(mUri, CommonStaticData.PROJECTION, Programs.FAV + "=?", 
                    CommonStaticData.selectionArgsFav,
                    orderSetStr);
        }
        else {
            mCursor = managedQuery(mUri, CommonStaticData.PROJECTION, Programs.TYPE + "=?", 
                    menuID == CommonStaticData.MENU_ID_TV ? CommonStaticData.selectionArgsTV : CommonStaticData.selectionArgsRadio,
                            orderSetStr);
        }
        // Used to map notes entries from the database to views
        adapter = new ServiceListSimpleAdapter(this, R.layout.servicelist_item, mCursor,
                new String[] { Programs.SERVICENAME, Programs.FREQ }, 
                new int[] { R.id.ptitle, R.id.freq });

//        programList.setAdapter(adapter);
//        ServiceListAdapter adapter = new ServiceListAdapter(this,getData());
        setListAdapter(adapter);
        if(CommonStaticData.bolHasChannel && mCursor.getCount() <= 0){
            CommonStaticData.bolHasChannel = false;
            ExcuteScan excuteScan = new ExcuteScan(ServiceListActivity.this, menuID);
            excuteScan.buildChannelScanDialog();
//            Toast.makeText(ServiceListActivity.this, R.string.noContent, Toast.LENGTH_SHORT).show();
//            this.finish();
//            return;
        }
        
//        NumberPickerDialog dialog = new NumberPickerDialog(this, -1, 5);
//        dialog.setTitle(getString(R.string.dialog_picker_title));
//        dialog.setOnNumberSetListener(this);
//        dialog.show();
    }
    public void addListFooterView(){
//        mLoadLayout = new LinearLayout(this);
//        mLoadLayout = this.getLayoutInflater().inflate(R.layout.servicescanprogress, null);
//        mLoadLayout.setMinimumHeight(260);  
//        mLoadLayout.setGravity(Gravity.CENTER);  
//        mLoadLayout.setOrientation(LinearLayout.HORIZONTAL); 
//        mLoadLayout.setBackgroundColor(Color.RED);
//        ProgressBar mProgressBar = new ProgressBar(this);  
//        mProgressBar.setPadding(0, 0, 15, 0);  
//        mLoadLayout.addView(mProgressBar, new LinearLayout.LayoutParams(  
//                LinearLayout.LayoutParams.WRAP_CONTENT,  
//                LinearLayout.LayoutParams.WRAP_CONTENT)); 
//        
//        TextView mTipContent = new TextView(this);  
//        mTipContent.setText(strDialogBody);  
//        mLoadLayout.addView(mTipContent, new LinearLayout.LayoutParams(  
//                LinearLayout.LayoutParams.WRAP_CONTENT,  
//                LinearLayout.LayoutParams.WRAP_CONTENT)); 
//        ListView mListView = this.getListView();  
////        mListView.addFooterView(mLoadLayout);
//        mListView.addHeaderView(mLoadLayout);
//        mListView.setOnScrollListener(ServiceListActivity.this);
    }
    public void onScroll(AbsListView view, int mFirstVisibleItem,  
            int mVisibleItemCount, int mTotalItemCount) {  
//        mLastItem = mFirstVisibleItem + mVisibleItemCount - 1;  
//        if (mListViewAdapter.count > mCount) {  
//            mListView.removeFooterView(mLoadLayout);  
//        }  
    }  
    public void onScrollStateChanged(AbsListView view, int mScrollState) {  
          
//        if (mLastItem == mListViewAdapter.count  
//                && mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {  
//            if (mListViewAdapter.count <= mCount) {  
//                mHandler.postDelayed(new Runnable() {  
//                    @Override  
//                    public void run() {  
//                        mListViewAdapter.count += 10;  
//                        mListViewAdapter.notifyDataSetChanged();  
//                        mListView.setSelection(mLastItem);  
//                    }  
//                }, 1000);  
//            }  
//        }  
    }  
    
    /***************************************************************************************************************/
    /*!
     *  @brief  Transform cursor content to a map list.
     *  @note   This function treats following :\n
     *              - Returns the value of the requested column as a String. 
     *                and assigns the return value to a list
     *  @param  none.
     *  @return Map list is returned.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    public List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        mCursor.moveToFirst();
        Logger.d("cursor cnt:"+mCursor.getCount());
        for(int i = 0; i < mCursor.getCount(); i++){
            Map<String, Object> map = new HashMap<String, Object>();
//            map.put("serviceimg", R.drawable.service_icon);
            map.put("name", mCursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_NAME));
            map.put("freq", mCursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_FREQ));
            map.put("fav", mCursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FAV));
            list.add(map);
            mCursor.moveToNext();
        }
        return list;
    }

    public final byte MSG_NO_SERVICE = 2;
    public final byte MSG_UPDATE_SERVICE_LIST = 3;
    public final byte MSG_UPDATE_SCAN_FREQ = 4;
    public final byte MSG_UPDATE_SCAN_RESULT = 5;
    public final byte MSG_REMOVE_SCAN_VIEW = 6;
    /***************************************************************************************************************/
    /*!
     *  @brief  Handle message
     *  @note   This Handler treats following :\n
     *                     abandon!
     *              - case MSG_NO_SERVICE:
     *                     Build AlertDialog about if needs to scan channel again.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    public Handler myHandler = new Handler()
    {

        String [] msgStr = new String[2];
        public void handleMessage(Message msg)
        {
            switch (msg.what) 
            { 
                case MSG_NO_SERVICE:
                    new AlertDialog.Builder(ServiceListActivity.this)
                    .setTitle(R.string.no_channel_tip)
                    .setMessage(getString(R.string.scan_again))
                    .setNegativeButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
		            getContentResolver().delete(mUri, null, null);
		            addListFooterView();
		            ServiceListActivity.this.findViewById(R.id.scanlayout).setVisibility(View.VISIBLE);
		            ServiceListActivity.this.getListView().setEnabled(false);
		            ScanPrograms scanPrograms = new ScanPrograms(ServiceListActivity.this);
		            scanProgramthd = new Thread(scanPrograms);
		            scanProgramthd.start();
                        }
                    })
                    .setPositiveButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ServiceListActivity.this.finish();
                            dialog.cancel();
                           
                        }
                    })
                    .show();
                    break;
                    
                case MSG_UPDATE_SERVICE_LIST:
                    adapter.notifyDataSetChanged();
                    break;
                case MSG_UPDATE_SCAN_FREQ:
                    msgStr = (String[])(msg.obj);
                    scanFreqText.setText(msgStr[0]+msgStr[1]);
                    break;
                case MSG_UPDATE_SCAN_RESULT:
                    msgStr = (String[])(msg.obj);
                    scanDTVResultText.setText(msgStr[0]);
                    scanRadioResultText.setText(msgStr[1]);
                    ServiceListActivity.this.getListView().setSelection(ServiceListActivity.this.getListView().getCount()-1);
                    break;
                case MSG_REMOVE_SCAN_VIEW:
//                    ServiceListActivity.this.getListView().removeFooterView(mLoadLayout);
//                    ServiceListActivity.this.getListView().removeHeaderView(mLoadLayout);
                    ServiceListActivity.this.getListView().setEnabled(true);
                    ServiceListActivity.this.findViewById(R.id.scanlayout).setVisibility(View.GONE);
                    ServiceListActivity.this.getListView().setSelection(0);
                    break;
                default:
                    break;
            }
        }
        
    };
    /***************************************************************************************************************/
    /*!
     *  @brief  TBD(Removable)
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
/*
        // This is our one standard application action -- inserting a
        // new note into the list.
        menu.add(0, MENU_ITEM_INSERT_TEXTNOTE, 0, R.string.menu_insert_textnote)
                .setShortcut('3', 'a')
                .setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, MENU_ITEM_INSERT_PAINTNOTE, 0, R.string.menu_insert_paintnote)
		        .setShortcut('3', 'p')
		        .setIcon(android.R.drawable.ic_menu_add);
        
        menu.add(0, MENU_NOTE_EXPORT, 0, R.string.menu_export);
        menu.add(0, MENU_NOTE_IMPORT, 0, R.string.menu_import);
        
        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, TVProgramListActivity.class), null, intent, 0, null);
*/
        return true;
    }

    /***************************************************************************************************************/
    /*!
     *  @brief  TBD(Removable)
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /***************************************************************************************************************/
    /*!
     *  @brief  TBD(Removable)
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Logger.e("bad menuInfo:" + e);
            return;
        }

//        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
//        if (cursor == null) {
//            // For some reason the requested item isn't available, do nothing
//            return;
//        }
        // Setup the menu header
//        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));

        // Add a menu item to send the note by message
//        menu.add(0, MENU_ITEM_SEND_MSG, 0, R.string.menu_send_msg);
        
        // Add a menu item to delete the note
//        menu.add(0, MENU_ITEM_DELETE, 0, R.string.menu_delete);
    }
        
    /***************************************************************************************************************/
    /*!
     *  @brief  TBD(Removable)
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Logger.e("bad menuInfo:" + e);
            return false;
        }
        return false;
    }

    /***************************************************************************************************************/
    /*!
     *  @brief  Process list item click event.
     *  @note   This function treats following :\n
     *              - start VideoPlayerActivity.
     *  @param  l        [in]  The ListView where the click happened
     *  @param  v        [in]  The view that was clicked within the ListView
     *  @param  position [in]  The position of the view in the list
     *  @param  id       [in]  The row id of the item that was clicked
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
//        final Intent intent = getIntent();
        Intent intent = new Intent(null, mUri,
                                //ServiceListActivity.this, RecordActivity.class);
                                ServiceListActivity.this, VideoPlayerActivity.class);
        intent.putExtra("cursorid", (int)position);
        intent.putExtra("menu_id", menuID);
        Logger.e("id:"+id+" pos:"+position);
        startActivity(intent);
    }
    public void onNumberSet(int number) {
        Log.d("NumberPicker", "Number selected: " + number);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Logger.e("ondestroy1");
        threadSuspended = true;
        Logger.e("ondestroy2");
//        scanProgramthd.stop();
    }
}
