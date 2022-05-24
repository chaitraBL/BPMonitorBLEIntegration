package com.example.bpmonitorbleintegration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HomePage extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    TextView bloodpressureText, pulseText, nameText, addressText, systaVal, diastaVal;
//   RecyclerView currentReading;
    List<String> readingList;
    ArrayAdapter adapter;
    private String TAG = HomePage.class.getName();
    List<BloodPressureDB> newTask = new ArrayList<>();
    BottomNavigationView bottomNavigationView;
    ArrayList<CandleEntry> yVal = new ArrayList<>();
    CandleStickChart candleStick;
    ArrayList<String> timeList = new ArrayList<>();
    ImageButton analyticBtn;
    ProgressBar progressBar1, progressBar2;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        nameText = findViewById(R.id.profile_name);
        addressText = findViewById(R.id.profile_address);
        bloodpressureText = findViewById(R.id.blood_pressure);
        pulseText = findViewById(R.id.pulse);
//        currentReading = findViewById(R.id.reading_list);
//        currentReading.setLayoutManager(new LinearLayoutManager(this));
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        candleStick = findViewById(R.id.reading_list);
        analyticBtn = findViewById(R.id.next_btn);
        progressBar1 = findViewById(R.id.pb_systa);
        progressBar2 = findViewById(R.id.pb_diasta);
        bottomNavigationView.setOnNavigationItemSelectedListener(HomePage.this);
        bottomNavigationView.setSelectedItemId(R.id.home);

        analyticBtn.setBackgroundDrawable(null);

        LinearLayout linearLayout = findViewById(R.id.linear_bp);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomePage.this, MainActivity.class));
            }
        });

        FirstFragment firstFragment = new FirstFragment();
        SecondFragment secondFragment = new SecondFragment();
        ThirdFragment thirdFragment = new ThirdFragment();

        Objects.requireNonNull(getSupportActionBar()).setTitle("DashBoard");

        nameText.setText("Welcome  Chaitra");
        addressText.setText("Bangalore");

        getManualTasks();

        analyticBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomePage.this, Statistics.class));
            }
        });
    }

    //Menu item.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu_file, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
//                startActivity(new Intent(HomePage.this, MainActivity.class));
                break;
            case R.id.analytics:
//                startActivity(new Intent(HomePage.this, LogActivity.class));
                break;
            case R.id.device_connect:
                startActivity(new Intent(HomePage.this, LogActivity.class));
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

            @SuppressLint("SetTextI18n")
            @Override
            protected void onPostExecute(List<BloodPressureDB> tasks) {
                super.onPostExecute(tasks);
                newTask.clear();
                candleStick.clear();
                if (tasks.size() > 0) {
                    @SuppressLint("SimpleDateFormat") DateFormat df1 = new SimpleDateFormat("MMM dd"); // Format date
                    String date = df1.format(Calendar.getInstance().getTime());

                    BloodPressureDB list = tasks.get(tasks.size() - 1);

                    bloodpressureText.setText(list.getSystolic() + " / " + list.getDystolic() + " mmHg");
                    pulseText.setText(list.getHeartRate() + " bpm");

                    progressBar1.setProgress(list.getSystolic());
                    progressBar2.setProgress(list.getDystolic());

                    for (int i = 0; i < tasks.size(); i++) {
                    if ("May 23".equals(tasks.get(i).getDate())) {
                        newTask.add(tasks.get(i));
                        plotCandleStickTimeWise(newTask);
                        candleStick.invalidate();
                        candleStick.notifyDataSetChanged();
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

    // Candle stick chart time based.
    public void plotCandleStickTimeWise(List<BloodPressureDB> tasks) {
        yVal.clear();
        timeList.clear();
        candleStick.clear();

        if (tasks != null && tasks.size() > 0) {
            int count = 0;

            for (BloodPressureDB list : tasks) {
                    yVal.add(new CandleEntry(count, list.getSystolic(),list.getDystolic(),list.getSystolic(),list.getDystolic()));
                    timeList.add(list.getTime());
                    count++;
            }

            Collections.sort(yVal,new EntryXComparator());

            CandleDataSet cds = new CandleDataSet(yVal, "");
            cds.setColor(Color.rgb(80, 80, 80));
            cds.setShadowColor(Color.DKGRAY);
            cds.setBarSpace(1f);
            cds.setDecreasingColor(Color.parseColor("#151B54"));
            cds.setDecreasingPaintStyle(Paint.Style.FILL);
            cds.setIncreasingColor(Color.parseColor("#151B54"));
            cds.setIncreasingPaintStyle(Paint.Style.STROKE);
            cds.setNeutralColor(Color.BLUE);
            cds.setValueTextColor(Color.BLACK);
            cds.setValueTextSize(10);
            CandleData cd = new CandleData(cds);
            candleStick.setData(cd);
            candleStick.getDescription().setEnabled(false);

            //X axis
            XAxis xAxis = candleStick.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(timeList.size());
            xAxis.setValueFormatter(new IndexAxisValueFormatter(timeList));
            xAxis.setAvoidFirstLastClipping(true);
            xAxis.setLabelRotationAngle(-45);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(false);
            xAxis.setGranularity(1f);
            xAxis.setGranularityEnabled(true);
            xAxis.setCenterAxisLabels(false);
            xAxis.setEnabled(true);
            CustomMarkerView mv = new CustomMarkerView(HomePage.this, R.layout.marker_view);
            candleStick.setMarkerView(mv);

            //Y axis
            YAxis yAxisRight = candleStick.getAxisRight();
            yAxisRight.setEnabled(false);
            YAxis yAxisLeft = candleStick.getAxisLeft();
            yAxisLeft.setLabelCount(6,true);
            yAxisLeft.setDrawAxisLine(false);
            yAxisLeft.setAxisMinimum(50);
            yAxisLeft.setAxisMaximum(200);

            if (yVal.size() > 1){
                Entry lastEntry = yVal.get(yVal.size()-1);
                Highlight highlight = new Highlight(lastEntry.getX(), lastEntry.getY(), 0);
                highlight.setDataIndex(0);
                candleStick.highlightValue(highlight);
                candleStick.moveViewToX(timeList.size()-1);
            }
            else
            {
                Log.i(TAG, "No data found!!!");
            }

            if (yVal.size() >= 6) {
                candleStick.setVisibleXRangeMaximum(6);
            }
            else
            {
                candleStick.invalidate();
            }
            candleStick.invalidate();
            candleStick.notifyDataSetChanged();
            candleStick.animateXY(1000,1000);
        }
    }
}