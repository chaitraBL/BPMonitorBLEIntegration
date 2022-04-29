package com.example.bpmonitorbleintegration;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RawDataModel {
    public static final String TAG = "RawDataModel";
    // Save to the Local Room DB.
//    private void saveTask(String address, int systolic, int dystolic, int heartRate, int range) {
////        final String sMessage = message.trim();
//        final String sAddress = address.trim();
//
//        DateFormat df = new SimpleDateFormat("HH:mm"); // Format time
//        String time = df.format(Calendar.getInstance().getTime());
//        Log.i(TAG, "Time " + time);
//
//        DateFormat df1 = new SimpleDateFormat("yyyy/MM/dd"); // Format date
//        String date = df1.format(Calendar.getInstance().getTime());
//        Log.i(TAG, "date " + date);
////
////        if (sMessage.isEmpty())
////        {
////            edCreateMessage.setError("Task required");
////            edCreateMessage.requestFocus();
////            return;
////        }
//
//        class SaveTask extends AsyncTask<Void, Void, Void> {
//
//            @Override
//            protected Void doInBackground(Void... voids) {
//
//                BloodPressureDB reading = new BloodPressureDB();
//                reading.setName(sAddress);
//                reading.setDate(date);
//                reading.setTime(time);
//                reading.setDystolic(dystolic);
//                reading.setSystolic(systolic);
//                reading.setHeartRate(heartRate);
//                reading.setRange(range);
//
//                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().bpReadingsDao().insert(reading);
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void unused) {
//                super.onPostExecute(unused);
////                finish();
////                startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                Toast.makeText(getApplicationContext(),"Saved",Toast.LENGTH_SHORT).show();
//            }
//        }
//
//        SaveTask st = new SaveTask();
//        st.execute();
//    }
}
