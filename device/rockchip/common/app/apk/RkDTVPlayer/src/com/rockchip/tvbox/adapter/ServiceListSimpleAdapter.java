/********************************************************************************************************************/
/**
 *  @skip   $Id:$
 *  @file   ServiceListSimpleAdapter.java
 *  @brief  Customized ListSimpleAdapter.
 *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
 *
 *  ALL Rights Reserved, Copyright(C) ROCKCHIP LIMITED 2011
 */
/********************************************************************************************************************/
package com.rockchip.tvbox.adapter;

import com.rockchip.tvbox.activity.R;
import com.rockchip.tvbox.utils.CommonStaticData;
import com.rockchip.tvbox.utils.Logger;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter;
public class ServiceListSimpleAdapter extends SimpleCursorAdapter {
    Cursor cursor;
    public ServiceListSimpleAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
        // TODO Auto-generated constructor stub
        this.cursor = c;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);
        cursor.moveToPosition(position);
        Button favBtn = (Button)convertView.findViewById(R.id.favBtn);
        if(cursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_FAV) == 1){
            favBtn.setBackgroundResource(R.drawable.favorite_set);
        }
        else{
            favBtn.setBackgroundResource(R.drawable.favorite_cancel);
        }
        if(cursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_TYPE)
                == Integer.parseInt(CommonStaticData.SERVICE_TYPE_RADIO)){
            ((ImageView)convertView.findViewById(R.id.channel_icon)).
                setBackgroundResource(R.drawable.radio_icon);
        }
        else{
            ((ImageView)convertView.findViewById(R.id.channel_icon)).
            setBackgroundResource(R.drawable.tv_icon_small);
        }
        
        Button encryptBtn = (Button)convertView.findViewById(R.id.encryptBtn);
        if(cursor.getInt(CommonStaticData.COLUMN_INDEX_SERVICE_ENCRYPT) == 1){
//            Logger.e("encryptBtn visible!!!!");
            encryptBtn.setVisibility(View.VISIBLE);
        }
        else{
            encryptBtn.setVisibility(View.INVISIBLE);
//            Logger.e("encryptBtn invisible!!!!");
        }

        TextView serviceName = (TextView)convertView.findViewById(R.id.ptitle);
         serviceName.setText(String.format("%03d", (position+1))+"  "+cursor.getString(CommonStaticData.COLUMN_INDEX_SERVICE_NAME));
		
        return convertView;
    }
}

