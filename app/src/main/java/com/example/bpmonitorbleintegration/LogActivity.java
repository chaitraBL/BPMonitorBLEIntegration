package com.example.bpmonitorbleintegration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class LogActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private final String TAG = LogActivity.class.getName();
    List<BloodPressureDB> newTask = new ArrayList<>();
    BottomNavigationView logBottomNavigationView;
    EditText startDate, endDate;
    List<Date> resultDate = new ArrayList<>();

    RecyclerView logRecycleView;
    Button selectBtn;
    DatePickerDialog picker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        ActionBar actioBar = getSupportActionBar();
        actioBar.setTitle("Logs");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#151B54")));

//        actioBar.setHomeAsUpIndicator(R.drawable.ic_baseline_keyboard_arrow_left_24);
//        actioBar.setDisplayHomeAsUpEnabled(true);
        logRecycleView = findViewById(R.id.log_list);
        logBottomNavigationView = findViewById(R.id.log_bottomNavigationView);
        startDate = findViewById(R.id.start_date);
        endDate = findViewById(R.id.end_date);
        selectBtn = findViewById(R.id.select);

        logBottomNavigationView.setOnNavigationItemSelectedListener(LogActivity.this);
        logBottomNavigationView.setSelectedItemId(R.id.device_connect);

        logRecycleView.setLayoutManager(new LinearLayoutManager(this));

        getManualTasks();

        startDate.setInputType(InputType.TYPE_NULL);
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(LogActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                startDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

        endDate.setInputType(InputType.TYPE_NULL);
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(LogActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                endDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                picker.show();
            }
        });
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

                        ReadingsAdapter adapter = new ReadingsAdapter(LogActivity.this, tasks);
                        logRecycleView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                    selectBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Date start_var = null, end_var = null;
                            ArrayList<String> selectedDate = new ArrayList<>();
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
                            sdf.setTimeZone(TimeZone.getTimeZone("T"));
//                            TimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH);
//                            LocalDate date = LocalDate.parse(string, formatter);
                            try {
//                                start_var = sdf.parse(startDate.getText().toString());
                                start_var = new java.sql.Date(sdf.parse(startDate.getText().toString()).getTime());
//                                end_var = sdf.parse(endDate.getText().toString());
                                end_var = new java.sql.Date(sdf.parse(endDate.getText().toString()).getTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            resultDate = getDates(start_var,end_var);
                            Log.i(TAG, "onClick: result date " + resultDate);

                            for (Date result : resultDate) {
                                selectedDate.add(String.valueOf(result));
                            }

                           for (BloodPressureDB i : tasks) {
                               if (selectedDate.equals(i.getDate()))
                               {
                                   newTask.add(i);
                               }
                           }

                            Log.i(TAG, "onClick: new task " + newTask);
                            ReadingsAdapter adapter = new ReadingsAdapter(LogActivity.this, newTask);
                            logRecycleView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                    });



                }
                else {
                    Log.i(TAG, "onPostExecute: No data");
                }
            }
        }
        GetTasks gt = new GetTasks();
        gt.execute();
    }

    public static List<Date> getDates(Date startDate, Date endDate) {
        ArrayList<Date> dates = new ArrayList<Date>();
//        DateFormat df1 = new SimpleDateFormat("MMM dd");

//        Date date1 = null;
//        Date date2 = null;
//
//        try {
//            date1 = df1.parse(startDate);
//            date2 = df1.parse(endDate);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(startDate);


        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(endDate);

        while(!cal1.after(cal2))
        {
            dates.add(cal1.getTime());
            cal1.add(Calendar.DATE, 1);
        }
        return dates;
    }

}