package com.example.bpmonitorbleintegration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LogActivity extends AppCompatActivity {

    private final String TAG = LogActivity.class.getName();
    List<BloodPressureDB> newTask = new ArrayList<>();

    RecyclerView logRecycleView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        getSupportActionBar().setTitle("Logs");
        logRecycleView = findViewById(R.id.log_list);

        logRecycleView.setLayoutManager(new LinearLayoutManager(this));

        getManualTasks();
    }

    //To retrieve data from Room DB.
    private void getManualTasks() {
        class GetTasks extends AsyncTask<Void, Void, List<BloodPressureDB>> {

            @Override
            protected List<BloodPressureDB> doInBackground(Void... voids) {
                List<BloodPressureDB> taskList = DatabaseClient
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .bpReadingsDao()
                        .getAll();
                return taskList;
            }

            @Override
            protected void onPostExecute(List<BloodPressureDB> tasks) {
                super.onPostExecute(tasks);

                if (tasks.size() > 0) {

                    DateFormat df1 = new SimpleDateFormat("MMM dd"); // Format date
                    String date = df1.format(Calendar.getInstance().getTime());
//
//                    BloodPressureDB list = tasks.get(tasks.size() - 1);
//
//                    bloodpressureText.setText(list.getSystolic() + " / " + list.getDystolic() + " mmHg");
//                    pulseText.setText(list.getHeartRate() + " bpm");
//
//                    progressBar1.setProgress(list.getSystolic());
//                    progressBar2.setProgress(list.getDystolic());

//                    for (int i = 0; i < tasks.size(); i++) {
//                        if (date.equals(tasks.get(i).getDate())) {
//                            newTask.add(tasks.get(i));
                        ReadingsAdapter adapter = new ReadingsAdapter(LogActivity.this, tasks);
                        logRecycleView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
//                        }
//                    }
                }
                else {
                    Log.i(TAG, "onPostExecute: No data");
                }
            }
        }
        GetTasks gt = new GetTasks();
        gt.execute();
    }

}