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
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Objects;
import java.util.TimeZone;

public class LogActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private final String TAG = LogActivity.class.getName();
    List<BloodPressureDB> newTask = new ArrayList<>();
    BottomNavigationView logBottomNavigationView;
    List<Date> resultDate = new ArrayList<>();

    RecyclerView logRecycleView;
    Button selectBtn,startBtn,endBtn;
    DatePickerDialog picker;
    TextView no_data_found,startDate,endDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        ActionBar actioBar = getSupportActionBar();
        Objects.requireNonNull(actioBar).setTitle(R.string.logs);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#151B54")));

//        actioBar.setHomeAsUpIndicator(R.drawable.ic_baseline_keyboard_arrow_left_24);
//        actioBar.setDisplayHomeAsUpEnabled(true);
        logRecycleView = findViewById(R.id.log_list);
        logBottomNavigationView = findViewById(R.id.log_bottomNavigationView);
        startDate = findViewById(R.id.txt_start_date);
        endDate = findViewById(R.id.txt_end_date);
        selectBtn = findViewById(R.id.btn_filter_logs);
        startBtn = findViewById(R.id.btn_start_date);
        endBtn = findViewById(R.id.btn_end_date);

        no_data_found = findViewById(R.id.txt_no_data_found);
        logBottomNavigationView.setOnNavigationItemSelectedListener(LogActivity.this);
        logBottomNavigationView.setSelectedItemId(R.id.logs);

        logRecycleView.setLayoutManager(new LinearLayoutManager(this));

        getManualTasks();

        startDate.setInputType(InputType.TYPE_NULL);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDateCalendar();
            }
        });

        endDate.setInputType(InputType.TYPE_NULL);
        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               endDateCalendar();
            }
        });
    }

    private void startDateCalendar(){
        Calendar c=Calendar.getInstance();
        int month=c.get(Calendar.MONTH);
        int day=c.get(Calendar.DAY_OF_MONTH);
        int year=c.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog =new DatePickerDialog(LogActivity.this, new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                c.set(year,month,dayOfMonth);
                int get_month = month+1;
                if(dayOfMonth < 10){
                    String day = "0"+dayOfMonth;
                    Log.d(TAG,"day:::"+day);
                    if(get_month < 10){
                        startDate.setText(day+"-"+"0"+get_month+"-"+year);
                    }else {

                        startDate.setText(day+"-"+get_month+"-"+year);
                    }
                }else {
                    if(get_month < 10){
                        startDate.setText(dayOfMonth+"-"+"0"+get_month+"-"+year);
                    }else {
                        startDate.setText(dayOfMonth+"-"+get_month+"-"+year);
                    }
                }
            }
        },year,month,day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()-1000);
        datePickerDialog.show();
}

private  void endDateCalendar() {
    Calendar c=Calendar.getInstance();
    int month=c.get(Calendar.MONTH);
    int day=c.get(Calendar.DAY_OF_MONTH);
    int year=c.get(Calendar.YEAR);
    DatePickerDialog datePickerDialog =new DatePickerDialog(LogActivity.this, new DatePickerDialog.OnDateSetListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            c.set(year,month,dayOfMonth);
            int get_month = month+1;
            if(dayOfMonth < 10){
                String day = "0"+dayOfMonth;
                Log.d(TAG,"day:::"+day);
                if(get_month < 10){
                    endDate.setText(day+"-"+"0"+get_month+"-"+year);
                }else {

                    endDate.setText(day+"-"+get_month+"-"+year);
                }
            }else {
                if(get_month < 10){
                    endDate.setText(dayOfMonth+"-"+"0"+get_month+"-"+year);
                }else {
                    endDate.setText(dayOfMonth+"-"+get_month+"-"+year);
                }
            }
        }
    },year,month,day);
    datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()-1000);
    datePickerDialog.show();
}

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.home:
                startActivity(new Intent(LogActivity.this, HomePage.class));
                break;
            case R.id.profile:
                break;
            case R.id.logs:
                break;
        }
        return true;
    }

    //To retrieve data from Room DB.
    private void getManualTasks() {
        @SuppressLint("StaticFieldLeak")
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

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(List<BloodPressureDB> tasks) {
                super.onPostExecute(tasks);

                if (tasks.size() == 0) {
                    no_data_found.setVisibility(View.VISIBLE);
                    logRecycleView.setVisibility(View.INVISIBLE);
                }
                else {
                    no_data_found.setVisibility(View.INVISIBLE);
                    logRecycleView.setVisibility(View.VISIBLE);
                        ReadingsAdapter adapter = new ReadingsAdapter(LogActivity.this, tasks);
                        logRecycleView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                    selectBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (startDate.getText().toString().equals("")) {
                                Toast.makeText(getApplicationContext(), "Please select start date", Toast.LENGTH_SHORT).show();
                            }
                            else if (endDate.getText().toString().equals("")){
                                Toast.makeText(getApplicationContext(), "Please select end date", Toast.LENGTH_SHORT).show();
                            }
                            else if ((startDate.getText().toString().equals("")) && (endDate.getText().toString().equals(""))){
                                Toast.makeText(getApplicationContext(), "Please select start & end date", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String start_date = startDate.getText().toString().replaceAll("-","");
                                String end_date = endDate.getText().toString().replaceAll("-","");

                                for (BloodPressureDB i : tasks) {
                                    String date = i.getDate().replaceAll("-","");

                                    if (Integer.parseInt(date) >= Integer.parseInt(start_date) && Integer.parseInt(date) <= Integer.parseInt(end_date)) {
                                        newTask.add(i);
                                    }

                                    if (newTask.size() == 0){
                                        no_data_found.setVisibility(View.VISIBLE);
                                        logRecycleView.setVisibility(View.INVISIBLE);
                                    }
                                    else {
                                        no_data_found.setVisibility(View.INVISIBLE);
                                        logRecycleView.setVisibility(View.VISIBLE);
                                        ReadingsAdapter adapter = new ReadingsAdapter(LogActivity.this, newTask);
                                        logRecycleView.setAdapter(adapter);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }
        GetTasks gt = new GetTasks();
        gt.execute();
    }

}