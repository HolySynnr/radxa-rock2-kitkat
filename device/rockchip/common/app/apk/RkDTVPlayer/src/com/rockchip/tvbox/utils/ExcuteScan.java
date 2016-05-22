/********************************************************************************************************************/
/**
 *  @skip   $Id:$
 *  @file   ScanPrograms.java
 *  @brief  Programs Scan thread.
 *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
 *
 *  ALL Rights Reserved, Copyright(C) ROCKCHIP LIMITED 2011
 */
/********************************************************************************************************************/
package com.rockchip.tvbox.utils;

import com.rockchip.tvbox.activity.R;
import com.rockchip.tvbox.activity.ServiceListActivity;
import com.rockchip.tvbox.activity.SettingPreferences;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ExcuteScan
{
    public CharSequence[] freqPointStrArrayList;
    private final int SINGLESCAN = 0;
    private final int FULLSCAN = 1;
    private final static int BASE_FREQ = 473143;
    private Context ctx;
    private int menuID;
    public ExcuteScan(Context ctx, int menuID)
    {
        this.ctx = ctx;
        this.menuID = menuID;
        initFreqPoint("");
    }
    public void initFreqPoint(String areaSetStr){
//        String areaSetStr = CommonStaticData.settings.getString(CommonStaticData.areaSetKey, "");
        Logger.e("areaStr:"+areaSetStr);
        CommonStaticData.freqPointArrayList.clear();
        CommonStaticData.bandWidthArrayList.clear();           
        
        if(areaSetStr.equals(ctx.getResources().getStringArray(R.array.array_area_set_atsc)[0])||
           areaSetStr.equals(ctx.getResources().getStringArray(R.array.array_area_set_atsc)[1]) ){//America & Canada
            CommonStaticData.totalFreqCount = 68;
            freqPointStrArrayList = new CharSequence[CommonStaticData.totalFreqCount];
            
            int[] ATSC_ChanFreq_Info = new int[] {
                    57000, 63000, 69000, 79000, 85000, 177000, 183000, 189000, 195000, 201000, // channel 2-11
                    207000, 213000, 473000, 479000, 485000, 491000, 497000, 503000, 509000, 515000, // channel 12-21
                    521000, 527000, 533000, 539000, 545000, 551000, 557000, 563000, 569000, 575000, // channel 22-31
                    581000, 587000, 593000, 599000, 605000, 611000, 617000, 623000, 629000, 635000, // channel 32-41
                    641000, 647000, 653000, 659000, 665000, 671000, 677000, 683000, 689000, 695000, // channel 42-51
                    701000, 707000, 713000, 719000, 725000, 731000, 737000, 743000, 749000, 755000, // channel 52-61
                    761000, 767000, 773000, 779000, 785000, 791000, 797000, 803000, // channel 62-69
            };

            for (int Index = 0; Index < CommonStaticData.totalFreqCount; Index++) {
                int freq_KHz = ATSC_ChanFreq_Info[Index];
                int bw_KHz = 6000;
                CommonStaticData.freqPointArrayList.add(freq_KHz);
                CommonStaticData.bandWidthArrayList.add(bw_KHz);

                freqPointStrArrayList[Index] = (freq_KHz / 1000) + "."
                        + (freq_KHz % 1000) + "MHz";
//                freqPointStrArrayList[Index] = "Channel "+ (Index+1) + ": Freq " + (freq_KHz / 1000) + "."
//                        + (freq_KHz % 1000) + "MHz" + "  " + "BW " + bw_KHz + "KHz";
            }
        }
        else if(areaSetStr.equals(ctx.getResources().getStringArray(R.array.array_area_set_isdb_oneseg)[1])
                ||areaSetStr.equals(ctx.getResources().getStringArray(R.array.array_area_set_isdb_fullseg)[1])){
            CommonStaticData.totalFreqCount = 50;
            freqPointStrArrayList = new CharSequence[CommonStaticData.totalFreqCount];
            for(int Index = 0; Index < CommonStaticData.totalFreqCount; Index++){
                int freq_KHz = BASE_FREQ + Index*6000;
                int bw_KHz = 6000;
                CommonStaticData.freqPointArrayList.add(freq_KHz);
                CommonStaticData.bandWidthArrayList.add(bw_KHz);
//                freqPointStrArrayList[Index] = "Freq: "+(freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz"+"  "+"BW: "+bw_KHz+"KHz";
                freqPointStrArrayList[Index] = (freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz";
                
            }
        }
        else if(areaSetStr.equals(ctx.getResources().getStringArray(R.array.array_area_set_isdb_oneseg)[0])
                ||areaSetStr.equals(ctx.getResources().getStringArray(R.array.array_area_set_isdb_fullseg)[0])){
            CommonStaticData.totalFreqCount = 63;
            freqPointStrArrayList = new CharSequence[CommonStaticData.totalFreqCount];
            for(int Index = 0; Index < 8; Index++)
            {
                int freq_KHz = 177143 + Index*6000;
                int bw_KHz = 6000;
                CommonStaticData.freqPointArrayList.add(freq_KHz);
                CommonStaticData.bandWidthArrayList.add(bw_KHz);             
//                freqPointStrArrayList[Index] = "Freq: "+(freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz"+"  "+"BW: "+bw_KHz+"KHz";
                freqPointStrArrayList[Index] = (freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz";
            }

            for(int Index = 8; Index < CommonStaticData.totalFreqCount; Index++)
            {
                int freq_KHz = BASE_FREQ + (Index-8)*6000;
                int bw_KHz = 6000;
                CommonStaticData.freqPointArrayList.add(freq_KHz);
                CommonStaticData.bandWidthArrayList.add(bw_KHz);             
                freqPointStrArrayList[Index] = (freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz";
//                freqPointStrArrayList[Index] = "Freq: "+(freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz"+"  "+"BW: "+bw_KHz+"KHz";
            }       
        }
        else if(areaSetStr.equals(ctx.getResources().getStringArray(R.array.array_area_set_dvb)[7])){
            CommonStaticData.totalFreqCount = 63;
            freqPointStrArrayList = new CharSequence[CommonStaticData.totalFreqCount];
            for(int Index = 0; Index < 7; Index++)
            {
                int freq_KHz = 177000 + Index*6000;
                int bw_KHz = 6000;
                CommonStaticData.freqPointArrayList.add(freq_KHz);
                CommonStaticData.bandWidthArrayList.add(bw_KHz);             
//                freqPointStrArrayList[Index] = "Freq: "+(freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz"+"  "+"BW: "+bw_KHz+"KHz";
                freqPointStrArrayList[Index] = (freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz";
            }

            for(int Index = 7; Index < CommonStaticData.totalFreqCount; Index++)
            {
                int freq_KHz = 473000 + (Index-7)*6000;
                int bw_KHz = 6000;
                CommonStaticData.freqPointArrayList.add(freq_KHz);
                CommonStaticData.bandWidthArrayList.add(bw_KHz);             
                freqPointStrArrayList[Index] = (freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz";
//                freqPointStrArrayList[Index] = "Freq: "+(freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz"+"  "+"BW: "+bw_KHz+"KHz";
            }       
        }
        else if(areaSetStr.equals(ctx.getResources().getStringArray(R.array.array_area_set_dvb)[4])){
            CommonStaticData.totalFreqCount = 57;
            freqPointStrArrayList = new CharSequence[CommonStaticData.totalFreqCount];
            for(int Index = 0; Index < 8; Index++)
            {
                int freq_KHz = 177500 + Index*7000;
                int bw_KHz = 7000;
                CommonStaticData.freqPointArrayList.add(freq_KHz);
                CommonStaticData.bandWidthArrayList.add(bw_KHz);             
                freqPointStrArrayList[Index] = (freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz";
//                freqPointStrArrayList[Index] = "Freq: "+(freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz"+"  "+"BW: "+bw_KHz+"KHz";
            }

            for(int Index = 8; Index < CommonStaticData.totalFreqCount; Index++)
            {
                int freq_KHz = 474000 + (Index-8)*8000;
                int bw_KHz = 8000;
                CommonStaticData.freqPointArrayList.add(freq_KHz);
                CommonStaticData.bandWidthArrayList.add(bw_KHz);             
                freqPointStrArrayList[Index] = (freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz";
//                freqPointStrArrayList[Index] = "Freq: "+(freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz"+"  "+"BW: "+bw_KHz+"KHz";
            }       
        }
        
        else{
            CommonStaticData.totalFreqCount = 57;
            freqPointStrArrayList = new CharSequence[CommonStaticData.totalFreqCount];
            for(int Index = 0; Index < 8; Index++)
            {
                int freq_KHz = 177500 + Index*7000;
                int bw_KHz = 7000;
                CommonStaticData.freqPointArrayList.add(freq_KHz);
                CommonStaticData.bandWidthArrayList.add(bw_KHz);             
                freqPointStrArrayList[Index] = (freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz";
//                freqPointStrArrayList[Index] = "Freq: "+(freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz"+"  "+"BW: "+bw_KHz+"KHz";
            }

            for(int Index = 8; Index < CommonStaticData.totalFreqCount; Index++)
            {
                int freq_KHz = 474000 + (Index-8)*8000;
                int bw_KHz = 8000;
                CommonStaticData.freqPointArrayList.add(freq_KHz);
                CommonStaticData.bandWidthArrayList.add(bw_KHz);             
                freqPointStrArrayList[Index] = (freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz";
//                freqPointStrArrayList[Index] = "Freq: "+(freq_KHz/1000)+"."+(freq_KHz%1000)+"MHz"+"  "+"BW: "+bw_KHz+"KHz";
            }       
        }
    }
    /***************************************************************************************************************/
    /*!
     *  @brief  Called when there is no program or never execute channel scan.
     *  @note   This function treats following :\n
     *              - Build alert dialog about channel scan.
     *              - Process button click.
     *  @param  menuID        [in] The menuID of the view in the gallery adapter.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    public void buildChannelScanDialog() {
//        initFreqPoint();
        new AlertDialog.Builder(ctx).setTitle(CommonStaticData.bolHasChannel?R.string.rescan_title:R.string.no_channel_tip)
                .setMessage(CommonStaticData.bolHasChannel?R.string.rescan_tip:R.string.scan_tip)
                .setNegativeButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(menuID == CommonStaticData.MENU_ID_SEARCH){
                            CommonStaticData.bolHasChannel = false;
                        }

                int mode = CommonStaticData.jniIF.getDVBMode_native();
                if(mode == CommonStaticData.DVB_FE_TYPE_QPSK){
                    buildManualScanDVBSDialog();
                }
                else {
                        buildChannelScanModeDialog();
                }                      
                    }
        })
                .setPositiveButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                })
                .show();

    }
    
    /***************************************************************************************************************/
    /*!
     *  @brief  Called when click Positive Button in ChannelScanDialog.
     *  @note   This function treats following :\n
     *              - Build alert dialog about channel scan mode select dialog.
     *              - Process button click.
     *  @param  menuID        [in] The menuID of the view in the gallery adapter.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    public void buildChannelScanModeDialog() {

        new AlertDialog.Builder(ctx).setMessage(R.string.scan_mode).setTitle("Scan Mode")
                .setNegativeButton(R.string.auto_scan, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        buildAutoScanDialog();
                    }
                })
                .setNeutralButton(R.string.manual_scan, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        buildManualScanSDialog();
                    }
                })
                .setPositiveButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                })
                .show();

    }
    
    /***************************************************************************************************************/
    /*!
     *  @brief  Called when click Neutral Button in ChannelScanModeDialog.
     *  @note   This function treats following :\n
     *              - Build alert dialog about manual scan list dialog.
     *              - Process button click.
     *  @param  menuID        [in] The menuID of the view in the gallery adapter.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    public void buildManualScanListDialog(){
        new AlertDialog.Builder(ctx).setTitle(R.string.select_freq)
            .setSingleChoiceItems(freqPointStrArrayList, 0, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // User clicked on a radio button do some stuff
                    buildStartScanDialog(dialog,whichButton);
                }
            })
            /*
            .setPositiveButton(R.string.start_scan, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                }
            })*/
            .show();
    }
    
    /***************************************************************************************************************/
    /*!
     *  @brief  Called when click Neutral Button in ChannelScanModeDialog.
     *  @note   This function treats following :\n
     *              - Build alert dialog about manual scan list dialog.
     *              - Process button click.
     *  @param  menuID        [in] The menuID of the view in the gallery adapter.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    public void buildManualScanDVBSDialog(){
        final Dialog scanInputDialog = new Dialog(ctx, R.style.MyDialog);
        //设置它的ContentView
        scanInputDialog.setContentView(R.layout.scanchannelinput);
        final EditText freqEditTxt = (EditText) scanInputDialog.findViewById(R.id.freqEdit);
        final EditText freqLNBEditTxt = (EditText) scanInputDialog.findViewById(R.id.lnbfreqEdit);      
        final EditText rateEditTxt = (EditText) scanInputDialog.findViewById(R.id.rateEdit);
        final Spinner polSpinner = (Spinner) scanInputDialog.findViewById(R.id.polSpinner);
        final Spinner s22kSpinner = (Spinner) scanInputDialog.findViewById(R.id.s22kSpinner);       
        Button okBtn = (Button) scanInputDialog.findViewById(R.id.okbtn);
        
        okBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(freqEditTxt.getText().toString().equals("")){
                    Toast.makeText(ctx, R.string.input_freq, Toast.LENGTH_SHORT).show();
                }
                else if(rateEditTxt.getText().toString().equals("")){
                    Toast.makeText(ctx, R.string.input_rate, Toast.LENGTH_SHORT).show();
                }
                else{
                    CommonStaticData.scanMode = SINGLESCAN;
                    CommonStaticData.scanFreq = Integer.parseInt(freqEditTxt.getText().toString());
                    CommonStaticData.scanLNBFreq = Integer.parseInt(freqLNBEditTxt.getText().toString());                   
                    CommonStaticData.symRate = Integer.parseInt(rateEditTxt.getText().toString());
                    CommonStaticData.scanBandWidth = Integer.parseInt(rateEditTxt.getText().toString());                    
                    CommonStaticData.polMode = polSpinner.getSelectedItemPosition();
                    CommonStaticData.s22kMode = s22kSpinner.getSelectedItemPosition();
                    
                    Logger.e("polMode:"+CommonStaticData.polMode);
                    Logger.e("22kMode:"+CommonStaticData.s22kMode);                 
                    Intent intent = new Intent();
                    intent.putExtra("menu_id", menuID);
                    intent.setClass(ctx, ServiceListActivity.class);
                    ctx.startActivity(intent);
                   
                    scanInputDialog.dismiss();
                }
            }
        });

        
        scanInputDialog.show();
    }
    public void buildAutoScanDialog(){
        final Dialog autoScanDialog = new Dialog(ctx, R.style.MyDialog);    
        autoScanDialog.setContentView(R.layout.autoscan);
        CheckBox scanLCNCb = (CheckBox) autoScanDialog.findViewById(R.id.scanlcn);
        Button okBtn = (Button) autoScanDialog.findViewById(R.id.okbtn);
        okBtn.requestFocus();
        final TextView frontendTxt = (TextView) autoScanDialog.findViewById(R.id.dvttlabel);  		
        final Spinner areaSpinner = (Spinner) autoScanDialog.findViewById(R.id.areaSpinner);
        scanLCNCb.setChecked(CommonStaticData.bolScanLCN);
        int mode = CommonStaticData.jniIF.getDVBMode_native();
        arrayId = R.array.array_area_set_dvb;
        if(mode == CommonStaticData.DVB_FE_TYPE_DVBT){
            arrayId = R.array.array_area_set_dvb;
			frontendTxt.setText(R.string.dvbt_label);
        }
        else if(mode == CommonStaticData.DVB_FE_TYPE_ATSC){
            arrayId = R.array.array_area_set_atsc;
			frontendTxt.setText(R.string.atsct_label);
        }
        else if(mode == CommonStaticData.DVB_FE_TYPE_ISDB_ONESEG){
            arrayId = R.array.array_area_set_isdb_oneseg;
			frontendTxt.setText(R.string.isdbt_oneseg_label);
        }
        else if(mode == CommonStaticData.DVB_FE_TYPE_ISDB_FULLSEG){
            arrayId = R.array.array_area_set_isdb_fullseg;
			frontendTxt.setText(R.string.isdbt_fullseg_label);
        }		

        initFreqPoint(ctx.getResources().getStringArray(arrayId)[0]);
        
        ArrayAdapter<CharSequence> areaAdapter=new ArrayAdapter<CharSequence>(ctx,
                R.layout.myspinnerstyle1, 
                ctx.getResources().getStringArray(arrayId)); 
		areaAdapter.setDropDownViewResource(R.layout.myspinnerstyle);
        areaSpinner.setAdapter(areaAdapter);
        
        areaSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                
                initFreqPoint(ctx.getResources().getStringArray(arrayId)[arg2]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                
            }
        });
//        
        scanLCNCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                CommonStaticData.bolScanLCN = isChecked;
                
            }
        });
        okBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                CommonStaticData.scanMode = FULLSCAN;
                CommonStaticData.scanFreq = BASE_FREQ;
                Intent intent = new Intent();
                intent.putExtra("menu_id", menuID);
                intent.setClass(ctx, ServiceListActivity.class);
                ctx.startActivity(intent);
            }
        });

        autoScanDialog.show();
    }
    int arrayId;
    int whichItem = 0;
    ArrayAdapter<CharSequence> freqAdapter;
    public void buildManualScanSDialog(){
        final Dialog manualScanDialog = new Dialog(ctx, R.style.MyDialog);

        //设置它的ContentView
        manualScanDialog.setContentView(R.layout.manaualscan);
        final Spinner areaSpinner = (Spinner) manualScanDialog.findViewById(R.id.areaSpinner);
        final Spinner freqSpinner = (Spinner) manualScanDialog.findViewById(R.id.freqSpinner);
        final TextView bandValueTxt = (TextView) manualScanDialog.findViewById(R.id.bandwidthvalue);  
        final TextView frontendTxt = (TextView) manualScanDialog.findViewById(R.id.dvttlabel);  		
        CheckBox scanLCNCb = (CheckBox) manualScanDialog.findViewById(R.id.scanlcn);
        Button okBtn = (Button) manualScanDialog.findViewById(R.id.okbtn);
        scanLCNCb.setChecked(CommonStaticData.bolScanLCN);
        int mode = CommonStaticData.jniIF.getDVBMode_native();
        arrayId = R.array.array_area_set_dvb;
        if(mode == CommonStaticData.DVB_FE_TYPE_DVBT){
            arrayId = R.array.array_area_set_dvb;
			frontendTxt.setText(R.string.dvbt_label);
        }
        else if(mode == CommonStaticData.DVB_FE_TYPE_ATSC){
            arrayId = R.array.array_area_set_atsc;
			frontendTxt.setText(R.string.atsct_label);
        }
        else if(mode == CommonStaticData.DVB_FE_TYPE_ISDB_ONESEG){
            arrayId = R.array.array_area_set_isdb_oneseg;
			frontendTxt.setText(R.string.isdbt_oneseg_label);
        }
        else if(mode == CommonStaticData.DVB_FE_TYPE_ISDB_FULLSEG){
            arrayId = R.array.array_area_set_isdb_fullseg;
			frontendTxt.setText(R.string.isdbt_fullseg_label);
        }

        initFreqPoint(ctx.getResources().getStringArray(arrayId)[0]);
        
        ArrayAdapter<CharSequence> areaAdapter=new ArrayAdapter<CharSequence>(ctx,
                R.layout.myspinnerstyle1, 
                ctx.getResources().getStringArray(arrayId)); 
        areaAdapter.setDropDownViewResource(R.layout.myspinnerstyle);
        areaSpinner.setAdapter(areaAdapter);
        
        
        
        freqAdapter=new ArrayAdapter<CharSequence>(ctx,
                R.layout.myspinnerstyle1, 
                freqPointStrArrayList); 
        freqAdapter.setDropDownViewResource(R.layout.myspinnerstyle);
        freqSpinner.setAdapter(freqAdapter);

        bandValueTxt.setText(""+CommonStaticData.bandWidthArrayList.get(0)+"KHz");
        
        areaSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                
                initFreqPoint(ctx.getResources().getStringArray(arrayId)[arg2]);
                freqAdapter = new ArrayAdapter<CharSequence>(ctx,
                        R.layout.myspinnerstyle1, 
                        freqPointStrArrayList); 
                freqAdapter.setDropDownViewResource(R.layout.myspinnerstyle);
                freqSpinner.setAdapter(freqAdapter);
                bandValueTxt.setText(""+CommonStaticData.bandWidthArrayList.get(arg2)+"KHz");
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                
            }
        });
        freqSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                whichItem = arg2;
                bandValueTxt.setText(""+CommonStaticData.bandWidthArrayList.get(arg2)+"KHz");
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                
            }
        });
//        
        scanLCNCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                CommonStaticData.bolScanLCN = isChecked;
                
            }
        });
        okBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                buildStartScanDialog(manualScanDialog,whichItem);
            }
        });

        
        manualScanDialog.show();
    }
    
    /***************************************************************************************************************/
    /*!
     *  @brief  Called when click Single Choice Item in ManualScanListDialog.
     *  @note   This function treats following :\n
     *              - Build alert dialog about start manual scan dialog.
     *              - Process button click.
     *  @param  menuID        [in] The menuID of the view in the gallery adapter.
     *  @return none.
     *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
     */
    /***************************************************************************************************************/
    public void buildStartScanDialog(final DialogInterface parentDialog,final int whichItem){
        new AlertDialog.Builder(ctx)
		    .setTitle("Start scan now?")
            .setMessage("Frequency: " + freqPointStrArrayList[whichItem]+ "   BW: " + CommonStaticData.bandWidthArrayList.get(whichItem)+"KHz" + "?")
            .setNegativeButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    CommonStaticData.scanMode = SINGLESCAN;
                    CommonStaticData.scanFreq = CommonStaticData.freqPointArrayList.get(whichItem);
                    CommonStaticData.scanBandWidth = CommonStaticData.bandWidthArrayList.get(whichItem);
                    
                    Intent intent = new Intent();
                    intent.putExtra("menu_id", menuID);
                    intent.setClass(ctx, ServiceListActivity.class);
                    ctx.startActivity(intent);
                    parentDialog.cancel();
                }
            })
            .setPositiveButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                }
            })
            .show();
    }
    
    
}
    
