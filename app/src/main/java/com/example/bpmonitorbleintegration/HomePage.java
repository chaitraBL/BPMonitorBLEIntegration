package com.example.bpmonitorbleintegration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class HomePage extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    TextView bloodpressureText, pulseText, nameText, addressText;
    private String TAG = HomePage.class.getName();
    List<BloodPressureDB> newTask = new ArrayList<>();
    BottomNavigationView bottomNavigationView;
//    ArrayList<CandleEntry> yVal = new ArrayList<>();
//    CandleStickChart candleStick;
    ArrayList<CombinedChart> yVal = new ArrayList<>();
    CombinedChart combinedChart;

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
        combinedChart = findViewById(R.id.reading_list);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
//        candleStick = findViewById(R.id.reading_list);
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

        getSupportActionBar().setTitle(R.string.dashboard);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#151B54")));

        nameText.setText("Welcome Chaitra");
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
            case R.id.profile:
//                startActivity(new Intent(HomePage.this, LogActivity.class));
                break;
            case R.id.logs:
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
//                candleStick.clear();
                combinedChart.clear();
                if (tasks.size() > 0) {
                    @SuppressLint("SimpleDateFormat") DateFormat df1 = new SimpleDateFormat("dd-MM-yyyy"); // Format date
                    String date = df1.format(Calendar.getInstance().getTime());

                    BloodPressureDB list = tasks.get(tasks.size() - 1);

                    bloodpressureText.setText(list.getSystolic() + " / " + list.getDystolic() + " mmHg");
                    pulseText.setText(list.getHeartRate() + " bpm");

                   changeSystolicProgress(list.getSystolic());
                   changeDiastolicProgress(list.getDystolic());

                    for (int i = 0; i < tasks.size(); i++) {
                    if ("27-05-2022".equals(tasks.get(i).getDate())) {
                        newTask.add(tasks.get(i));
//                        plotCandleStickTimeWise(newTask);
                        plotCombinedChart(newTask);
                        combinedChart.invalidate();
                        combinedChart.notifyDataSetChanged();
//                        candleStick.invalidate();
//                        candleStick.notifyDataSetChanged();
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

    private void changeSystolicProgress(int systolic) {
        if (systolic < 80) {
            progressBar1.setProgress(systolic);
            progressBar1.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#90EE90")));
        }
        else if (systolic <= 120) {
            progressBar1.setProgress(systolic);
            progressBar1.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#008000")));
        }
        else if (systolic <= 139) {
            progressBar1.setProgress(systolic);
            progressBar1.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFD700")));
        }
        else if (systolic <= 159) {
            progressBar1.setProgress(systolic);
            progressBar1.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFA500")));
        }
        else if (systolic <= 179) {
            progressBar1.setProgress(systolic);
            progressBar1.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FF8C00")));
        }
        else {
            progressBar1.setProgress(systolic);
            progressBar1.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
        }
    }

    private void changeDiastolicProgress(int diastolic) {
        if (diastolic < 60) {
            progressBar2.setProgress(diastolic);
            progressBar2.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#90EE90")));
        }
        else if (diastolic <= 80) {
            progressBar2.setProgress(diastolic);
            progressBar2.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#008000")));
        }
        else if (diastolic <= 89) {
            progressBar2.setProgress(diastolic);
            progressBar2.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFD700")));
        }
        else if (diastolic <= 99) {
            progressBar2.setProgress(diastolic);
            progressBar2.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFA500")));
        }
        else if (diastolic <= 109) {
            progressBar2.setProgress(diastolic);
            progressBar2.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FF8C00")));
        }
        else {
            progressBar2.setProgress(diastolic);
            progressBar2.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
        }
    }

    // Combined chart with candle stick & line chart.
    public void plotCombinedChart(List<BloodPressureDB> task) {
        timeList.clear();
        combinedChart.clear();

        if (task != null && task.size() > 0) {
            combinedChart.getDescription().setEnabled(false);
//            combinedChart.setBackgroundColor(Color.parseColor("#f9f9f9"));
            combinedChart.setDrawGridBackground(false);
            combinedChart.setDrawBarShadow(false);
            combinedChart.setHighlightFullBarEnabled(false);

            for (BloodPressureDB list : task) {
                timeList.add(list.getTime());
            }

            // draw bars behind lines
            combinedChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                    CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE
            });

            // Legend
            Legend legend = combinedChart.getLegend();
            legend.setWordWrapEnabled(false);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setDrawInside(true);
            legend.setEnabled(false);

//            X axis
            XAxis xAxis = combinedChart.getXAxis();
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
            xAxis.setTextSize(8);
            xAxis.setEnabled(true);


            //Y axis
            YAxis yAxisRight = combinedChart.getAxisRight();
            yAxisRight.setEnabled(false);
            YAxis yAxisLeft = combinedChart.getAxisLeft();
            yAxisLeft.setLabelCount(6,true);
            yAxisLeft.setDrawAxisLine(false);
            yAxisLeft.setTextSize(8);
            yAxisLeft.setAxisMinimum(50);
            yAxisLeft.setAxisMaximum(200);

            // Set color as per the mode - Dark mode/Light mode.
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    xAxis.setTextColor(Color.WHITE);
                    yAxisLeft.setTextColor(Color.WHITE);
                    yAxisRight.setTextColor(Color.WHITE);
                    break;

                case Configuration.UI_MODE_NIGHT_NO:
                    xAxis.setTextColor(Color.BLACK);
                    yAxisLeft.setTextColor(Color.BLACK);
                    yAxisRight.setTextColor(Color.BLACK);
                    break;
            }

            combinedChart.setDragEnabled(true);
            combinedChart.setScaleEnabled(true);

            // force pinch zoom along both axis
            combinedChart.setPinchZoom(true);

            // enable touch gestures
            combinedChart.setTouchEnabled(true);
            CombinedData data = new CombinedData();

            data.setData(generateCandleData(task));
            data.setData(generateLineData(task));

            combinedChart.setData(data);
            combinedChart.notifyDataSetChanged();
            combinedChart.invalidate();
        }
    }

    // Candle stick chart time based.
        private CandleData generateCandleData(List<BloodPressureDB> task) {
            CandleData d = null;

            if (task != null && task.size() > 0) {

            int count = 0;
                ArrayList<CandleEntry> entries1 = new ArrayList<>();

            for (BloodPressureDB list : task) {
                entries1.add(new CandleEntry(count, list.getSystolic(),list.getDystolic(),list.getSystolic(),list.getDystolic()));
                count++;
            }

            Collections.sort(entries1,new EntryXComparator());

            CandleDataSet cds = new CandleDataSet(entries1, "");
            cds.setColor(Color.rgb(80, 80, 80));
            cds.setShadowColor(Color.DKGRAY);
            cds.setBarSpace(1f);
            cds.setDecreasingColor(Color.parseColor("#FFA500"));
            cds.setDecreasingPaintStyle(Paint.Style.FILL);
            cds.setIncreasingColor(Color.parseColor("#FFA500"));
            cds.setIncreasingPaintStyle(Paint.Style.STROKE);
            cds.setDrawValues(false);
            cds.setNeutralColor(Color.BLUE);
            CustomMarkerView mv = new CustomMarkerView(HomePage.this, R.layout.marker_view);
                // Set the marker to the chart
                 mv.setChartView(combinedChart);
                combinedChart.setMarker(mv);

            d = new CandleData(cds);

            if (entries1.size() >= 5) {
                combinedChart.setVisibleXRangeMaximum(5);
            }
            else
            {
                combinedChart.invalidate();
            }
        }
            return d;
        }

    // Line chart time based.
        private LineData generateLineData(List<BloodPressureDB> task) {
            LineData d = null;
            if (task != null && task.size() > 0) {

            d = new LineData();
//            ArrayList<Integer> colors = new ArrayList<Integer>();
//            for (String color : details.getSequenceColors()) {
//                colors.add(Color.parseColor(color));
//            }
            ArrayList<Entry> entries = new ArrayList<>();
            ArrayList<Entry> entries1 = new ArrayList<>();

            int count = 0;
            for (int i = 0; i < task.size(); i++) {
                entries.add(new Entry(count, task.get(i).getSystolic()));
                entries1.add(new Entry(count,task.get(i).getDystolic()));
                count++;
            }

                Collections.sort(entries,new EntryXComparator());

            LineDataSet set = new LineDataSet(entries, "");
            set.setDrawHorizontalHighlightIndicator(false);
            set.setDrawVerticalHighlightIndicator(false);
            set.setDrawCircles(true);
            set.setColors(Color.MAGENTA);
            set.setCircleColor(Color.parseColor("#50EBEC"));
            set.setCircleRadius(5f);
            set.setDrawCircleHole(false);
            set.enableDashedLine(10,5,0);
            set.setValueTextSize(10f);
            set.setDrawValues(true);

            Collections.sort(entries1,new EntryXComparator());
            LineDataSet set2 = new LineDataSet(entries1,"");
            set2.setDrawHorizontalHighlightIndicator(false);
            set2.setDrawVerticalHighlightIndicator(false);
            set2.setDrawCircles(true);
            set2.setDrawCircleHole(false);
            set2.enableDashedLine(10,5,0);
            set2.setColors(Color.RED);
            set2.setCircleColor(Color.parseColor("#50EBEC"));
            set2.setCircleRadius(5f);
            set2.setValueTextSize(10f);
            set2.setDrawValues(true);

                // Set color as per the mode - Dark mode/Light mode.
                switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        set.setValueTextColor(Color.WHITE);
                        set2.setValueTextColor(Color.WHITE);
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        set.setValueTextColor(Color.BLACK);
                        set2.setValueTextColor(Color.BLACK);
                        break;
                }

                d.addDataSet(set);
                d.addDataSet(set2);

                if (entries.size() >= 5) {
                    combinedChart.setVisibleXRangeMaximum(5);
                }
                else
                {
                    combinedChart.invalidate();
                }

                if (entries1.size() >= 5) {
                    combinedChart.setVisibleXRangeMaximum(5);
                }
                else
                {
                    combinedChart.invalidate();
                }

//                if (entries1.size() > 1){
//                    Entry lastEntry = entries1.get(entries1.size()-1);
//                    Highlight highlight = new Highlight(lastEntry.getX(), lastEntry.getY(), 0);
//                    highlight.setDataIndex(0);
//                    combinedChart.highlightValue(highlight);
//                    combinedChart.moveViewToX(timeList.size()-1);
//                }
//                else
//                {
//                    Log.i(TAG, "No data found!!!");
//                }
        }
            return d;
        }
    }

    // Candle stick chart time based.
//    public void plotCandleStickTimeWise(List<BloodPressureDB> tasks) {
//        yVal.clear();
//        timeList.clear();
//        candleStick.clear();
//
//        if (tasks != null && tasks.size() > 0) {
//            int count = 0;
//
//            for (BloodPressureDB list : tasks) {
//                    yVal.add(new CandleEntry(count, list.getSystolic(),list.getDystolic(),list.getSystolic(),list.getDystolic()));
//                    timeList.add(list.getTime());
//                    count++;
//            }
//
//            Collections.sort(yVal,new EntryXComparator());
//
//            CandleDataSet cds = new CandleDataSet(yVal, "");
//            cds.setColor(Color.rgb(80, 80, 80));
//            cds.setShadowColor(Color.DKGRAY);
//            cds.setBarSpace(1f);
//            cds.setDecreasingColor(Color.parseColor("#FFA500"));
//            cds.setDecreasingPaintStyle(Paint.Style.FILL);
//            cds.setIncreasingColor(Color.parseColor("#FFA500"));
//            cds.setIncreasingPaintStyle(Paint.Style.STROKE);
//            cds.setNeutralColor(Color.BLUE);
//
//            // Set color as per the mode - Dark mode/Light mode.
//            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
//                case Configuration.UI_MODE_NIGHT_YES:
//                    cds.setValueTextColor(Color.WHITE);
//                    break;
//                case Configuration.UI_MODE_NIGHT_NO:
//                    cds.setValueTextColor(Color.BLACK);
//                    break;
//            }
//            cds.setValueTextSize(10);
//            CandleData cd = new CandleData(cds);
//            candleStick.setData(cd);
//            candleStick.getDescription().setEnabled(false);
//
//            //X axis
//            XAxis xAxis = candleStick.getXAxis();
//            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//            xAxis.setLabelCount(timeList.size());
//            xAxis.setValueFormatter(new IndexAxisValueFormatter(timeList));
//            xAxis.setAvoidFirstLastClipping(true);
//            xAxis.setLabelRotationAngle(-45);
//            xAxis.setDrawGridLines(false);
//            xAxis.setDrawAxisLine(false);
//            xAxis.setGranularity(1f);
//            xAxis.setGranularityEnabled(true);
//            xAxis.setCenterAxisLabels(false);
//            xAxis.setTextSize(8);
//            xAxis.setEnabled(true);
//            CustomMarkerView mv = new CustomMarkerView(HomePage.this, R.layout.marker_view);
//            candleStick.setMarkerView(mv);
//
//            //Y axis
//            YAxis yAxisRight = candleStick.getAxisRight();
//            yAxisRight.setEnabled(false);
//            YAxis yAxisLeft = candleStick.getAxisLeft();
//            yAxisLeft.setLabelCount(6,true);
//            yAxisLeft.setDrawAxisLine(false);
//            yAxisLeft.setTextSize(8);
//            yAxisLeft.setAxisMinimum(50);
//            yAxisLeft.setAxisMaximum(200);
//
//            // Set color as per the mode - Dark mode/Light mode.
//            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
//                case Configuration.UI_MODE_NIGHT_YES:
//                    xAxis.setTextColor(Color.WHITE);
//                    yAxisLeft.setTextColor(Color.WHITE);
//                    yAxisRight.setTextColor(Color.WHITE);
//                    break;
//
//                case Configuration.UI_MODE_NIGHT_NO:
//                    xAxis.setTextColor(Color.BLACK);
//                    yAxisLeft.setTextColor(Color.BLACK);
//                    yAxisRight.setTextColor(Color.BLACK);
//                    break;
//            }
//
//            if (yVal.size() > 1){
//                Entry lastEntry = yVal.get(yVal.size()-1);
//                Highlight highlight = new Highlight(lastEntry.getX(), lastEntry.getY(), 0);
//                highlight.setDataIndex(0);
//                candleStick.highlightValue(highlight);
//                candleStick.moveViewToX(timeList.size()-1);
//            }
//            else
//            {
//                Log.i(TAG, "No data found!!!");
//            }
//
//            if (yVal.size() >= 6) {
//                candleStick.setVisibleXRangeMaximum(6);
//            }
//            else
//            {
//                candleStick.invalidate();
//            }
//            candleStick.invalidate();
//            candleStick.notifyDataSetChanged();
//            candleStick.animateXY(1000,1000);
//        }
//    }
//}

//
//if (systolic < 80 || diastolic < 60) {
//        progressBar1.setProgress(systolic);
//        progressBar2.setProgress(diastolic);
//        progressBar1.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#90EE90")));
//        progressBar2.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#90EE90")));
//        }
//        else if ((systolic >= 80 && systolic <= 120) || (diastolic >= 60 && diastolic <= 80)) {
//        progressBar1.setProgress(systolic);
//        progressBar2.setProgress(diastolic);
//        progressBar1.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#008000")));
//        progressBar2.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#008000")));
//        }
//        else if ((systolic > 120 && systolic <= 139) || (diastolic > 80 && diastolic <= 89)) {
//        progressBar1.setProgress(systolic);
//        progressBar2.setProgress(diastolic);
//        progressBar1.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFD700")));
//        progressBar2.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFD700")));
//        }
//        else if ((systolic >= 140 && systolic <= 159) || (diastolic >= 90 && diastolic <= 99)) {
//        progressBar1.setProgress(systolic);
//        progressBar2.setProgress(diastolic);
//        progressBar1.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFA500")));
//        progressBar2.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFA500")));
//        }
//        else if ((systolic >= 160 && systolic <= 179) || (diastolic >= 100 && diastolic <= 109)) {
//        progressBar1.setProgress(systolic);
//        progressBar2.setProgress(diastolic);
//        progressBar1.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FF8C00")));
//        progressBar2.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FF8C00")));
//        }
//        else if ((systolic >= 180) || (diastolic >= 110)) {
//        progressBar1.setProgress(systolic);
//        progressBar2.setProgress(diastolic);
//        progressBar1.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
//        progressBar2.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
//        }