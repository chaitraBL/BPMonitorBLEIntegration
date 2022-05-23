package com.example.bpmonitorbleintegration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LogActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private final String TAG = LogActivity.class.getName();
    List<BloodPressureDB> newTask = new ArrayList<>();
    BottomNavigationView logBottomNavigationView;

    RecyclerView logRecycleView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        getSupportActionBar().setTitle("Logs");
        logRecycleView = findViewById(R.id.log_list);
        logBottomNavigationView = findViewById(R.id.log_bottomNavigationView);

        logBottomNavigationView.setOnNavigationItemSelectedListener(LogActivity.this);
        logBottomNavigationView.setSelectedItemId(R.id.device_connect);

        logRecycleView.setLayoutManager(new LinearLayoutManager(this));

        getManualTasks();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.home:
                startActivity(new Intent(LogActivity.this, HomePage.class));
                break;
            case R.id.device_connect:
                break;
            case R.id.analytics:
                break;
        }
        return true;
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

                        ReadingsAdapter adapter = new ReadingsAdapter(LogActivity.this, tasks);
                        logRecycleView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

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