package com.example.bpmonitorbleintegration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomePage extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    TextView bloodpressureText, pulseText, nameText, addressText;
   RecyclerView currentReading;
    List<String> readingList;
    ArrayAdapter adapter;
    private String TAG = HomePage.class.getName();
    List<BloodPressureDB> newTask = new ArrayList<>();
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        nameText = findViewById(R.id.profile_name);
        addressText = findViewById(R.id.profile_address);
        bloodpressureText = findViewById(R.id.blood_pressure);
        pulseText = findViewById(R.id.pulse);
        currentReading = findViewById(R.id.reading_list);
        currentReading.setLayoutManager(new LinearLayoutManager(this));
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(HomePage.this);
        bottomNavigationView.setSelectedItemId(R.id.home);

        FirstFragment firstFragment = new FirstFragment();
        SecondFragment secondFragment = new SecondFragment();
        ThirdFragment thirdFragment = new ThirdFragment();

        getSupportActionBar().setTitle("Home");

        nameText.setText("Welcome  Chaitra");
        addressText.setText("Bangalore");

        getManualTasks();
    }
//
//    //Menu item.
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.home_menu_file, menu);
//        return super.onCreateOptionsMenu(menu);
//
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.device_connect:
//                startActivity(new Intent(HomePage.this, MainActivity.class));
//                return true;
//
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//
//    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
//                startActivity(new Intent(HomePage.this, MainActivity.class));
                break;
            case R.id.analytics:
                startActivity(new Intent(HomePage.this, Statistics.class));
                break;
            case R.id.device_connect:
                startActivity(new Intent(HomePage.this, MainActivity.class));
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


                    BloodPressureDB list = tasks.get(tasks.size() - 1);

                    bloodpressureText.setText(list.getSystolic() + "/" + list.getDystolic() + "mmHg");
                    pulseText.setText(list.getHeartRate() + "bpm");

                for (int i = 0; i < tasks.size(); i++) {
                    if (date.equals(tasks.get(i).getDate())) {
                        newTask.add(tasks.get(i));
                        ReadingsAdapter adapter = new ReadingsAdapter(HomePage.this, newTask);
                        currentReading.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                }
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