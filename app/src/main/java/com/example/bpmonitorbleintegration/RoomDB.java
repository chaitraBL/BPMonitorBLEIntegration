package com.example.bpmonitorbleintegration;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RoomDB {
//    public static final String TIME_FORMAT = "hh:mm aa";
    public static final String TIME_FORMAT = "hh:mm aa";
    public static final String TAG = "RawDataModel";
    public RoomDB() {
    }
    //     Save to the Local Room DB.
    public void saveTask(String address, int systolic, int dystolic, int heartRate, int range, Context context) {
        final String sAddress = address.trim();

//        DateFormat df = new SimpleDateFormat("HH:mm a"); // Format time
//        String time = df.format(Calendar.getInstance().getTime());

        // To get current date and time.
        SimpleDateFormat TimeFormat = new SimpleDateFormat(TIME_FORMAT); // Format time

        Calendar ATime = Calendar.getInstance();
        String Timein12hourFormat = TimeFormat.format(ATime.getTime());
//        Log.i(TAG, "Time " + time);

        DateFormat df1 = new SimpleDateFormat("MMM dd"); // Format date
        String date = df1.format(Calendar.getInstance().getTime());
//        Log.i(TAG, "date " + date)

        class SaveTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

                BloodPressureDB reading = new BloodPressureDB();
                reading.setName(sAddress);
                reading.setDate(date);
                reading.setTime(Timein12hourFormat);
                reading.setDystolic(dystolic);
                reading.setSystolic(systolic);
                reading.setHeartRate(heartRate);
                reading.setRange(range);

                DatabaseClient.getInstance(context).getAppDatabase().bpReadingsDao().insert(reading);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
//                finish();
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                Toast.makeText(context,"Saved",Toast.LENGTH_SHORT).show();
            }
        }

        SaveTask st = new SaveTask();
        st.execute();
    }

    //     Save to the Local Room DB.
    public void saveAverageTask(String address, int systolic, int diastolic, String date, Context context) {
        final String sAddress = address.trim();

//        Log.i(TAG, "date " + date)

        class SaveTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

                AverageBPDB reading = new AverageBPDB();
                reading.setName(sAddress);
                reading.setDate(date);
                reading.setDystolic(diastolic);
                reading.setSystolic(systolic);

                DatabaseClient.getInstance(context).getAppDatabase1().averageReadingsDoa().insert(reading);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
//                finish();
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                Toast.makeText(context,"Saved",Toast.LENGTH_SHORT).show();
            }
        }

        SaveTask st = new SaveTask();
        st.execute();
    }
}
