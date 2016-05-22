/********************************************************************************************************************/
/**
 *  @skip   $Id:$
 *  @file   NumberPicker.java
 *  @brief  NumberPicker
 *  @date   2012/1/11 ROCKCHIP) Chenqsh create.
 *
 *  ALL Rights Reserved, Copyright(C) ROCKCHIP LIMITED 2012
 */
/********************************************************************************************************************/

package com.rockchip.tvbox.picker;

import com.rockchip.tvbox.activity.R;
import com.rockchip.tvbox.utils.CommonStaticData;
import com.rockchip.tvbox.utils.Logger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;

public class NumberPickerDialog extends AlertDialog implements OnClickListener {
    private OnNumberSetListener mListener;
    private NumberPicker mNumberPicker;
    private NumberPicker mNumberPicker1;
    private NumberPicker mNumberPicker2;
    private NumberPicker mNumberPicker3;
    private NumberPicker mNumberPicker4;
    private int mInitialValue;
    
    public NumberPickerDialog(Context context, int theme, int initialValue) {
        super(context, theme);
        mInitialValue = initialValue;

        setButton(BUTTON_POSITIVE, context.getString(R.string.dialog_set_number), this);
        setButton(BUTTON_NEGATIVE, context.getString(R.string.dialog_cancel), (OnClickListener) null);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.number_picker_pref, null);
        setView(view);

        mNumberPicker = (NumberPicker) view.findViewById(R.id.pref_num_picker);
        mNumberPicker.setCurrent(mInitialValue);
        
        mNumberPicker1 = (NumberPicker) view.findViewById(R.id.pref_num_picker1);
        mNumberPicker1.setCurrent(mInitialValue);
        
        mNumberPicker2 = (NumberPicker) view.findViewById(R.id.pref_num_picker2);
        mNumberPicker2.setCurrent(mInitialValue);
        
        mNumberPicker3 = (NumberPicker) view.findViewById(R.id.pref_num_picker3);
        mNumberPicker3.setCurrent(mInitialValue);
        
        mNumberPicker4 = (NumberPicker) view.findViewById(R.id.pref_num_picker4);
        mNumberPicker4.setCurrent(mInitialValue);
    }

    public void setOnNumberSetListener(OnNumberSetListener listener) {
        mListener = listener;
    }

    public void onClick(DialogInterface dialog, int which) {
        if (mListener != null) {
            Logger.e("set number:"+mNumberPicker.getCurrent()+"  "
                    +mNumberPicker1.getCurrent()+"  "
                    +mNumberPicker2.getCurrent()+"  "
                    +mNumberPicker3.getCurrent()+"  "
                    +mNumberPicker4.getCurrent()+"  ");
            int pageNum = (mNumberPicker3.getCurrent()*10+mNumberPicker4.getCurrent())<<12 |
                            (mNumberPicker.getCurrent()*100+mNumberPicker1.getCurrent()*10+
                                    mNumberPicker2.getCurrent());
            Logger.e("pageNum :"+ pageNum);
                            
            mListener.onNumberSet(pageNum);
        }
    }

    public interface OnNumberSetListener {
        public void onNumberSet(int selectedNumber);
    }
}

