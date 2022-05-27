package com.example.bpmonitorbleintegration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
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
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
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
//        currentReading = findViewById(R.id.reading_list);
//        currentReading.setLayoutManager(new LinearLayoutManager(this));
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

        getSupportActionBar().setTitle("DashBoard");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#151B54")));

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
//                candleStick.clear();
                combinedChart.clear();
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

    public void plotCombinedChart(List<BloodPressureDB> task) {
        timeList.clear();
        combinedChart.clear();

        combinedChart.getDescription().setEnabled(false);
        combinedChart.setBackgroundColor(Color.parseColor("#f9f9f9"));
        combinedChart.setDrawGridBackground(false);
        combinedChart.setDrawBarShadow(false);
        combinedChart.setHighlightFullBarEnabled(false);

        // draw bars behind lines
        combinedChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.SCATTER
        });

        combinedChart.getAxisRight().setEnabled(false);
        combinedChart.getAxisLeft().setEnabled(false);
        combinedChart.getXAxis().setEnabled(false);

        Legend legend = combinedChart.getLegend();
        legend.setEnabled(false);

        CustomMarkerView mv = new CustomMarkerView(HomePage.this, R.layout.marker_view);
        // Set the marker to the chart
        mv.setChartView(combinedChart);
        combinedChart.setMarker(mv);
        combinedChart.setDragEnabled(false);
        combinedChart.setScaleEnabled(true);

        // force pinch zoom along both axis
        combinedChart.setPinchZoom(false);

        // enable touch gestures
        combinedChart.setTouchEnabled(true);
        CombinedData data = new CombinedData();

        data.setData(generateBarData(task));
        data.setData(generateScatterData(task));

        combinedChart.setData(data);
        combinedChart.invalidate();
    }

        private BarData generateBarData(List<BloodPressureDB> task) {

            ArrayList<BarEntry> entries1 = new ArrayList<>();
            ArrayList<BarEntry> entries2 = new ArrayList<>();

            int count = 0;
//            for (int index = 0; index < count; index++) {
//                entries1.add(new BarEntry(0, getRandom(25, 25)));
//
//                // stacked
//                entries2.add(new BarEntry(0, new float[]{getRandom(13, 12), getRandom(13, 12)}));
//            }

            for (int i = 0; i < task.size();i++)
            {
                entries1.add(new BarEntry(count, task.get(i).getSystolic()));
            }

            BarDataSet set1 = new BarDataSet(entries1, "");
            set1.setColor(Color.rgb(60, 220, 78));
            set1.setValueTextColor(Color.rgb(60, 220, 78));
            set1.setValueTextSize(10f);
            set1.setAxisDependency(YAxis.AxisDependency.LEFT);

            BarDataSet set2 = new BarDataSet(entries2, "");
            set2.setStackLabels(new String[]{"Stack 1", "Stack 2"});
            set2.setColors(Color.rgb(61, 165, 255), Color.rgb(23, 197, 255));
            set2.setValueTextColor(Color.rgb(61, 165, 255));
            set2.setValueTextSize(10f);
            set2.setAxisDependency(YAxis.AxisDependency.LEFT);

            float groupSpace = 0.06f;
            float barSpace = 0.02f; // x2 dataset
            float barWidth = 0.45f; // x2 dataset
            // (0.45 + 0.02) * 2 + 0.06 = 1.00 -> interval per "group"

            BarData d = new BarData(set1, set2);
            d.setBarWidth(barWidth);

            // make this BarData object grouped
            d.groupBars(0, groupSpace, barSpace); // start at x = 0

            return d;
        }

        private ScatterData generateScatterData(List<BloodPressureDB> task) {

            ScatterData d = new ScatterData();
            ArrayList<Integer> colors = new ArrayList<Integer>();
//            for (String color : details.getSequenceColors()) {
//                colors.add(Color.parseColor(color));
//            }
            ArrayList<Entry> entries = new ArrayList<>();

            int count = 0;
            for(int i = 0; i <task.size(); i++)
            {
                entries.add(new Entry(count,task.get(i).getSystolic()));
            }
//            for (float index = 0; index < count; index += 0.5f)
//                entries.add(new Entry(index + 0.25f, getRandom(10, 55),"title;"+colors.get((int) index)));

            ScatterDataSet set = new ScatterDataSet(entries, "");
            set.setDrawHorizontalHighlightIndicator(false);
            set.setDrawVerticalHighlightIndicator(false);
            set.setColors(colors);
            set.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
            set.setScatterShapeSize(20f);
            set.setDrawValues(false);
            set.setValueTextSize(10f);
            d.addDataSet(set);

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