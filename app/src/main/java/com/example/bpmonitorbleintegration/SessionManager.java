package com.example.bpmonitorbleintegration;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    final static String appPrefNames="MyAppInfo";
    public static SharedPreferences myPrefsSession;
    public static SharedPreferences.Editor prefsEditorSession;

    public static void setCuffValue(Context context, int cuff_Val) {

        myPrefsSession = context.getSharedPreferences(appPrefNames, Context.MODE_PRIVATE);
        prefsEditorSession = myPrefsSession.edit();
        try {
            prefsEditorSession.putInt("Cuff", cuff_Val);
//            prefsEditorSession.putString("Cuff", userId);

        } catch (Exception e) {
            e.printStackTrace();
        }
        prefsEditorSession.commit();
    }


    public static int getCuffValue(Context c) {
        myPrefsSession = c.getSharedPreferences(appPrefNames, Context.MODE_PRIVATE);
        int cuff_value = myPrefsSession.getInt("Cuff", 0);
//        String userId= myPrefsSession.getString("Cuff", "");
        return cuff_value;
    }

    public static void setPressureValue(Context context, int pressure_Val) {

        myPrefsSession = context.getSharedPreferences(appPrefNames, Context.MODE_PRIVATE);
        prefsEditorSession = myPrefsSession.edit();
        try {
            prefsEditorSession.putInt("Pressure", pressure_Val);
//            prefsEditorSession.putString("Cuff", userId);

        } catch (Exception e) {
            e.printStackTrace();
        }
        prefsEditorSession.commit();
    }


    public static int getPressureValue(Context c) {
        myPrefsSession = c.getSharedPreferences(appPrefNames, Context.MODE_PRIVATE);
        int pressure_value = myPrefsSession.getInt("Pressure", 0);
//        String userId= myPrefsSession.getString("Cuff", "");
        return pressure_value;
    }
}
