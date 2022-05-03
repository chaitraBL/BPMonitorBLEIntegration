package com.example.bpmonitorbleintegration;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;

import java.util.ArrayList;
import java.util.List;

public class Statistics extends AppCompatActivity {

    LineChart lineChart;
    LineData lineData;
    List<Entry> entryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        lineChart = findViewById(R.id.linechart);
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

            }
        }
        GetTasks gt = new GetTasks();
        gt.execute();
    }

    private void plotLineGraph(){

    }
}