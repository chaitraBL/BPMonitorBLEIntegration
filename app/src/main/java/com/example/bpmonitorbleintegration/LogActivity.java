package com.example.bpmonitorbleintegration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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

    public class ReadingsAdapter extends RecyclerView.Adapter<ReadingsAdapter.ReadingViewHolder> {
        private final Context mCtx;
        private final List<BloodPressureDB> readingList;

        public ReadingsAdapter(Context mCtx, List<BloodPressureDB> taskList) {
            this.mCtx = mCtx;
            this.readingList = taskList;
        }

        @Override
        public ReadingsAdapter.ReadingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mCtx).inflate(R.layout.recyclerview_tasks, parent, false);
            return new ReadingsAdapter.ReadingViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull ReadingViewHolder holder, int position) {
            BloodPressureDB t = readingList.get(position);

            String date = t.getDate();
            String[] showDate = date.split("-");

            if (showDate[1].equalsIgnoreCase("01")) {
                holder.textViewDate.setText(showDate[0]+"-"+"JAN");
            }else if(showDate[1].equalsIgnoreCase("02")){
                holder.textViewDate.setText(showDate[0]+"-"+"FEB");
            }else if(showDate[1].equalsIgnoreCase("03")){
                holder.textViewDate.setText(showDate[0]+"-"+"MAR");
            }else if(showDate[1].equalsIgnoreCase("04")){
                holder.textViewDate.setText(showDate[0]+"-"+"APR");
            }else if(showDate[1].equalsIgnoreCase("05")){
                holder.textViewDate.setText(showDate[0]+"-"+"MAY");
            }else if(showDate[1].equalsIgnoreCase("06")){
                holder.textViewDate.setText(showDate[0]+"-"+"JUN");
            }else if(showDate[1].equalsIgnoreCase("07")){
                holder.textViewDate.setText(showDate[0]+"-"+"JLY");
            }else if(showDate[1].equalsIgnoreCase("08")){
                holder.textViewDate.setText(showDate[0]+"-"+"AUG");
            }else if(showDate[1].equalsIgnoreCase("09")){
                holder.textViewDate.setText(showDate[0]+"-"+"SEP");
            }else if(showDate[1].equalsIgnoreCase("10")){
                holder.textViewDate.setText(showDate[0]+"-"+"OCT");
            }else if(showDate[1].equalsIgnoreCase("11")){
                holder.textViewDate.setText(showDate[0]+"-"+"NOV");
            }else if(showDate[1].equalsIgnoreCase("12")){
                holder.textViewDate.setText(showDate[0]+"-"+"DEC");
            }
            holder.textViewTime.setText(t.getTime());
            holder.textViewSysta.setText(String.valueOf(t.getSystolic()));
            holder.textViewDiasta.setText(String.valueOf(t.getDystolic()));
            holder.textViewRate.setText(String.valueOf(t.getHeartRate()));
        }

        @Override
        public int getItemCount() {
            return readingList.size();
        }
        public class ReadingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

//            Button textViewDate, textViewTime, textViewSysta, textViewDiasta, textViewRate, textViewRange;
            TextView textViewDate, textViewTime, textViewSysta, textViewDiasta, textViewRate, textViewRange;

            public ReadingViewHolder(View itemView) {
                super(itemView);

                textViewDate = itemView.findViewById(R.id.date);
                textViewTime = itemView.findViewById(R.id.time1);
                textViewSysta = itemView.findViewById(R.id.systalic);
                textViewDiasta = itemView.findViewById(R.id.dystalic);
                textViewRate = itemView.findViewById(R.id.heartRate);

                itemView.setOnClickListener(this);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                BloodPressureDB task = readingList.get(getAdapterPosition());

                Dialog shareDialog = new Dialog(mCtx);
                shareDialog.setContentView(R.layout.share_readings);
                shareDialog.setCancelable(false);
                TextView systolicText = shareDialog.findViewById(R.id.reading_sys_txt);
                TextView diastolicText = shareDialog.findViewById(R.id.reading_dia_txt);
                TextView heartRateText = shareDialog.findViewById(R.id.reading_rate_txt);
                TextView statusText = shareDialog.findViewById(R.id.reading_status_txt);
                TextView dateText = shareDialog.findViewById(R.id.reading_date_txt);
                Button btnShare = shareDialog.findViewById(R.id.btn_share_reading);
                Button btnCancel = shareDialog.findViewById(R.id.btn_share_reading_cancel);
                systolicText.setText(task.getSystolic() + " mmHg");
                diastolicText.setText(task.getDystolic() + " mmHg");
                heartRateText.setText(task.getHeartRate() + " bpm");
                String status = changeStatus(task.getSystolic(),task.getDystolic());
                statusText.setText(status);
                dateText.setText(task.getDate());
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        shareDialog.dismiss();
                    }
                });
                btnShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        shareDialog.dismiss();
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        String shareBody = "Name :" +task.getName()+"\n"+
                                "Blood Pressure Reading :" +systolicText.getText().toString() + " / " + diastolicText.getText().toString() + " / " + heartRateText.getText().toString() +"\n"+
                                "Date of Reading : "+ dateText.getText().toString()+"\n"+
                                "Time of Reading : "+ task.getTime()+"\n"+
                                "Status :"+ statusText.getText().toString();
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Blood Pressure Readings For "+task.getName());
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                    }
                });
                shareDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Window window = shareDialog.getWindow();
                WindowManager.LayoutParams wlp = window.getAttributes();
                wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
                wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                wlp.gravity = Gravity.CENTER;
                shareDialog.create();
                shareDialog.show();
            }

            private String changeStatus(int systolic, int diastolic) {
                String msg = null;
                if ((systolic < 50) && (diastolic < 33)) {
                    msg = "Very Serious Hypotension";
                }
                else if ((systolic <= 60) && (diastolic <= 40)) {
                    msg = "Serious Hypotension";
                }
                else if ((systolic <= 90) && (diastolic <= 60)) {
                    msg = "Borderline Hypotension";
                }
                else if ((systolic <= 110) && (diastolic <= 75)) {
                    msg = "Low Blood Pressure";
                }
                else if ((systolic <= 120 && (diastolic <= 80))) {
                    msg = "Normal Blood Pressure";
                }
                else if ((systolic <= 130) && (diastolic <= 85)) {
                    msg = "High Normal Blood Pressure";
                }
                else if ((systolic <= 140) && (diastolic <= 90)) {
                    msg = "Hypertension Stage 1";
                }
                else if ((systolic <= 160) && (diastolic <= 100)) {
                    msg = "Hypertension Stage 2";
                }
                else if ((systolic <= 180 && (diastolic <= 110))) {
                    msg = "Hypertension Stage 3";
                }
                else {
                    msg = "Hypertension Stage 4";
                }
                return msg;
            }

        }
    }

}