package com.example.bpmonitorbleintegration;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class RawDataModel {
    int cuff_val;
    int pressure_val;

//    public int getCuff_val() {
//        return cuff_val;
//    }
    public int getCuff_val(Context context) {
        SharedPreferences pref = context.getSharedPreferences("myAppInfo", Context.MODE_PRIVATE);
        cuff_val = pref.getInt("Cuff",0);
        return cuff_val;
    }

//    public void setCuff_val(int cuff_val) {
//        this.cuff_val = cuff_val;
//    }

    public void setCuff_val(Context context,int cuff_val) {
        this.cuff_val = cuff_val;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor  = pref.edit();
        editor.putInt("Cuff",cuff_val);
        editor.commit();
    }

    public int getPressure_val() {
        return pressure_val;
    }

    public void setPressure_val(int pressure_val) {
        this.pressure_val = pressure_val;
    }
}
