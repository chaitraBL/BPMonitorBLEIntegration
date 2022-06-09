package com.example.bpmonitorbleintegration.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Canvas;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bpmonitorbleintegration.FirstFragment;
import com.example.bpmonitorbleintegration.logs.LogActivity;
import com.example.bpmonitorbleintegration.reading.MainActivity;
import com.example.bpmonitorbleintegration.R;
import com.example.bpmonitorbleintegration.SecondFragment;
import com.example.bpmonitorbleintegration.ThirdFragment;
import com.example.bpmonitorbleintegration.charts.CustomMarkerView;
import com.example.bpmonitorbleintegration.charts.Statistics;
import com.example.bpmonitorbleintegration.database.BloodPressureDB;
import com.example.bpmonitorbleintegration.database.DatabaseClient;
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
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class HomePage extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    TextView bloodpressureText, pulseText, nameText, addressText, dateText;
    private final String TAG = HomePage.class.getName();
    List<BloodPressureDB> newTask = new ArrayList<>();
    BottomNavigationView bottomNavigationView;
    ArrayList<BloodPressureDB> pressureVal = new ArrayList<>();
//    CandleStickChart candleStick;
    ArrayList<CombinedChart> yVal = new ArrayList<>();
    CombinedChart combinedChart;

    ArrayList<String> timeList = new ArrayList<>();
    ImageButton previousDateBtn, nextDateBtn;
    ProgressBar progressBar1, progressBar2;
    String selectedDate = null;
    String year = null;
    boolean isData = false;
    Button allBtn;
    ProgressBar progress;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
//        Activity.reCreate();
        nameText = findViewById(R.id.profile_name);
        addressText = findViewById(R.id.profile_address);
        bloodpressureText = findViewById(R.id.blood_pressure);
        pulseText = findViewById(R.id.pulse);
        combinedChart = findViewById(R.id.reading_list);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
//        candleStick = findViewById(R.id.reading_list);
        nextDateBtn = findViewById(R.id.next_btn);
        previousDateBtn = findViewById(R.id.previous_btn);
        progressBar1 = findViewById(R.id.pb_systa);
        progressBar2 = findViewById(R.id.pb_diasta);
        bottomNavigationView.setOnNavigationItemSelectedListener(HomePage.this);
        bottomNavigationView.setSelectedItemId(R.id.home);
        dateText = findViewById(R.id.date_text);
        allBtn = findViewById(R.id.all_values);
        progress = findViewById(R.id.progress_home);

        nextDateBtn.setBackgroundDrawable(null);
        previousDateBtn.setBackgroundDrawable(null);

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

        progress.setVisibility(View.VISIBLE);
        getManualTasks();

        @SuppressLint("SimpleDateFormat") DateFormat df1 = new SimpleDateFormat("dd-MM-yyyy"); // Format date
        nextDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.setVisibility(View.VISIBLE);
                newTask.clear();
                combinedChart.clear();
                dateText.setTextColor(Color.BLACK);
//                allBtn.setBackgroundColor(0);
                String newDateFormat = dateText.getText().toString() + "-" + year;
                String changedDate = convertDateStringFormat(newDateFormat,"dd - MMM-yyyy", "dd-MM-yyyy");
                Date incrementedDate = incrementDateByOne(changedDate);
                String newDate = df1.format(incrementedDate);
                selectedDate = changeDateFormat(newDate);
                dateText.setText(selectedDate);
                for (BloodPressureDB i : pressureVal) {
                    if (newDate.equals(i.getDate())) {
                        newTask.add(i);
//                        Log.i(TAG, "onClick: new task in next " + newTask);
                        if (newTask.size() > 0) {
                            plotForSelectedDate(newTask);
                            combinedChart.notifyDataSetChanged();
                            combinedChart.invalidate();
                            progress.setVisibility(View.GONE);

                        }
                        else{
                            combinedChart.setNoDataText("No chart data found");
                            combinedChart.notifyDataSetChanged();
                            combinedChart.invalidate();
                            progress.setVisibility(View.GONE);
                        }
                    }
                    else {
                        combinedChart.setNoDataText("No chart data found");
                        combinedChart.notifyDataSetChanged();
                        combinedChart.invalidate();
                        progress.setVisibility(View.GONE);
                    }

                }

            }
        });

        previousDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newTask.clear();
                combinedChart.clear();
                dateText.setTextColor(Color.BLACK);
//                allBtn.setBackgroundColor(0);
                progress.setVisibility(View.VISIBLE);
                String newDateFormat = dateText.getText().toString() + "-" + year;
                String changedDate = convertDateStringFormat(newDateFormat,"dd - MMM-yyyy", "dd-MM-yyyy");

                Date decrementedDate = decrementDateByOne(changedDate);
                String newDate = df1.format(decrementedDate);
                selectedDate = changeDateFormat(newDate);
                dateText.setText(selectedDate);
                for (BloodPressureDB i : pressureVal) {
                    if (newDate.equals(i.getDate())) {
//                        Log.i(TAG, "onClick: date in model previous " + i.getDate());
                        newTask.add(i);
//                        Log.i(TAG, "onClick: new task in previous " + newTask);
                        if (newTask.size() > 0) {
                            plotForSelectedDate(newTask);
                            combinedChart.notifyDataSetChanged();
                            combinedChart.invalidate();
                            progress.setVisibility(View.GONE);

                        }
                        else {
                            combinedChart.setNoDataText("No chart data found");
                            combinedChart.notifyDataSetChanged();
                            combinedChart.invalidate();
                            progress.setVisibility(View.GONE);

                        }
                    }
                    else {
                        combinedChart.setNoDataText("No chart data found");
                        combinedChart.notifyDataSetChanged();
                        combinedChart.invalidate();
                        progress.setVisibility(View.GONE);
                    }
                }

            }
        });

        allBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                dateText.setTextColor(Color.GRAY);
                String date = df1.format(Calendar.getInstance().getTime());
                selectedDate = changeDateFormat(date);
//                    Log.i(TAG, "onPostExecute: date " + date);
                dateText.setText(selectedDate);
//                allBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), Color.parseColor("#FFA500")));
                if (pressureVal.size() > 0) {
                    for (int i = 0; i < pressureVal.size(); i++) {
//                        Comparator<BloodPressureDB> firstNameSorter = (o1, o2) -> o1.getDate().compareTo(o2.getDate());
//                        Collections.sort(pressureVal, firstNameSorter);
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            pressureVal.sort(Comparator.comparing(BloodPressureDB::getDate));
//                        }
                        Log.i(TAG, "onClick: sorted val " + pressureVal);
                        plotCombinedChart(pressureVal);
//                        plotCombinedChart(sortedList);
                        combinedChart.notifyDataSetChanged();
                        combinedChart.invalidate();
                    }
                }
                else {
                    combinedChart.setNoDataText("No chart data found");
                    combinedChart.notifyDataSetChanged();
                    combinedChart.invalidate();
                }

            }
        });
    }

    //To convert date into specified format.
    public String convertDateStringFormat(String strDate, String fromFormat, String toFormat){
        try{
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(fromFormat);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat2 = new SimpleDateFormat(toFormat.trim());
            return dateFormat2.format(Objects.requireNonNull(sdf.parse(strDate)));
        }catch (Exception e) {
            e.printStackTrace();
            return "";
        }
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
                pressureVal.clear();
                dateText.setTextColor(Color.GRAY);
                if (tasks.size() > 0) {
                    @SuppressLint("SimpleDateFormat") DateFormat df1 = new SimpleDateFormat("dd-MM-yyyy"); // Format date
                    Date date1 = new Date();
                    String date = df1.format(Calendar.getInstance().getTime());
                    selectedDate = changeDateFormat(date);
//                    Log.i(TAG, "onPostExecute: date " + date);
                    dateText.setText(selectedDate);

                    BloodPressureDB list = tasks.get(tasks.size() - 1);

                    bloodpressureText.setText(list.getSystolic() + " / " + list.getDystolic() + " mmHg");
                    pulseText.setText(list.getHeartRate() + " bpm");

                   changeSystolicProgress(list.getSystolic());
                   changeDiastolicProgress(list.getDystolic());

                    for (int i = 0; i < tasks.size(); i++) {
                        pressureVal.add(tasks.get(i));
                        plotCombinedChart(tasks);
                        combinedChart.notifyDataSetChanged();
                        combinedChart.invalidate();
                        progress.setVisibility(View.GONE);
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

    /**
     * Get next date from current selected date
     *
     * @param date date
     */
    @SuppressLint("SimpleDateFormat")
    public Date incrementDateByOne(String date) {
        Date date1= null;
        try {
            date1 = new SimpleDateFormat("dd-MM-yyyy").parse(date);
//            date1 = new SimpleDateFormat("MMM-dd").parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date1);
        c.add(Calendar.DATE, 1);
        Date nextDate = c.getTime();
        return nextDate;
    }

    /**
     * Get previous date from current selected date
     *
     * @param date date
     */
    @SuppressLint("SimpleDateFormat")
    public Date decrementDateByOne(String date) {
        Date date1= null;

        try {
            date1 = new SimpleDateFormat("dd-MM-yyyy").parse(date);
//            date1 = new SimpleDateFormat("MMM-dd").parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date1);
        c.add(Calendar.DATE, -1);
        Date previousDate = c.getTime();
        return previousDate;
    }

    private String changeDateFormat(String date) {
        String[] showDate = date.split("-");
        String holder = null;
        year = showDate[2];

        // Changing the date format
        if (showDate[1].equalsIgnoreCase("01")) {
            holder = showDate[0]+" - "+getString(R.string.jan);
        }else if(showDate[1].equalsIgnoreCase("02")){
            holder = showDate[0]+" - "+getString(R.string.feb);
        }else if(showDate[1].equalsIgnoreCase("03")){
            holder = showDate[0]+" - "+getString(R.string.mar);
        }else if(showDate[1].equalsIgnoreCase("04")){
            holder = showDate[0]+" - "+getString(R.string.apr);
        }else if(showDate[1].equalsIgnoreCase("05")){
            holder = showDate[0]+" - "+getString(R.string.may);
        }else if(showDate[1].equalsIgnoreCase("06")){
            holder = showDate[0]+" - "+getString(R.string.jun);
        }else if(showDate[1].equalsIgnoreCase("07")){
            holder = showDate[0]+" - "+getString(R.string.jly);
        }else if(showDate[1].equalsIgnoreCase("08")){
            holder = showDate[0]+" - "+getString(R.string.aug);
        }else if(showDate[1].equalsIgnoreCase("09")){
            holder = showDate[0]+" - "+getString(R.string.sep);
        }else if(showDate[1].equalsIgnoreCase("10")){
            holder = showDate[0]+" - "+getString(R.string.oct);
        }else if(showDate[1].equalsIgnoreCase("11")){
            holder = showDate[0]+" - "+getString(R.string.nov);
        }else if(showDate[1].equalsIgnoreCase("12")){
            holder = showDate[0]+" - "+getString(R.string.dec);
        }
        return holder;
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
    public void plotForSelectedDate(List<BloodPressureDB> task) {
        timeList.clear();
        combinedChart.clear();

        isData = false;
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
            legend.setYEntrySpace(0.5f);
            legend.setXEntrySpace(0.5f);

//            X axis
            XAxis xAxis = combinedChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(timeList.size());
            xAxis.setValueFormatter(new IndexAxisValueFormatter(timeList));
            xAxis.setAvoidFirstLastClipping(false);
            xAxis.setLabelRotationAngle(-45);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(false);
            xAxis.setGranularity(1f);
            xAxis.setGranularityEnabled(true);
            xAxis.setCenterAxisLabels(false);
            xAxis.setTextSize(8);
            xAxis.setEnabled(true);
            xAxis.setSpaceMin(0.4f);
            xAxis.setSpaceMax(0.7f);
            combinedChart.setXAxisRenderer(new CustomXAxisRenderer(combinedChart.getViewPortHandler(), combinedChart.getXAxis(), combinedChart.getTransformer(YAxis.AxisDependency.LEFT)));

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
            if (timeList.size() >= 5) {
                combinedChart.setVisibleXRangeMaximum(5);
            }
            else
            {
                combinedChart.invalidate();
            }

            combinedChart.setData(data);
            combinedChart.notifyDataSetChanged();
            combinedChart.invalidate();
        }
    }

    // Combined chart with candle stick & line chart.
    public void plotCombinedChart(List<BloodPressureDB> task) {
        timeList.clear();
        combinedChart.clear();

        isData = true;
        if (task != null && task.size() > 0) {
            combinedChart.getDescription().setEnabled(false);
            combinedChart.setDrawGridBackground(false);
            combinedChart.setDrawBarShadow(false);
            combinedChart.setHighlightFullBarEnabled(false);

            String changedDate;
            for (BloodPressureDB list : task) {
                changedDate = changeDateFormat(list.getDate());
                timeList.add(changedDate+ "\n\r" +list.getTime());

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
            legend.setYEntrySpace(0.5f);
            legend.setXEntrySpace(0.5f);

//            X axis
            XAxis xAxis = combinedChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(timeList.size());
            xAxis.setValueFormatter(new IndexAxisValueFormatter(timeList));
            xAxis.setAvoidFirstLastClipping(false);
            xAxis.setLabelRotationAngle(-45);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(false);
            xAxis.setGranularity(1f);
            xAxis.setGranularityEnabled(true);
            xAxis.setCenterAxisLabels(false);
            xAxis.setTextSize(8);
            xAxis.setEnabled(true);
            xAxis.setSpaceMin(0.4f);
            xAxis.setSpaceMax(0.7f);
            combinedChart.setXAxisRenderer(new CustomXAxisRenderer(combinedChart.getViewPortHandler(), combinedChart.getXAxis(), combinedChart.getTransformer(YAxis.AxisDependency.LEFT)));

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
            if (timeList.size() >= 5) {
                combinedChart.setVisibleXRangeMaximum(5);
            }
            else
            {
                combinedChart.invalidate();
            }

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
            cds.setDrawHighlightIndicators(false);
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

        //https://stackoverflow.com/questions/56459470/how-to-highlight-the-whole-stacked-bar
    //https://stackoverflow.com/questions/53283496/how-to-draw-range-chart-mpandroidchart-with-negative-and-positive-value

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
            set.setDrawHighlightIndicators(false);

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
            set2.setDrawHighlightIndicators(false);

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

    public class CustomXAxisRenderer extends XAxisRenderer {
        public CustomXAxisRenderer(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans) {
            super(viewPortHandler, xAxis, trans);
        }

        @Override
        protected void drawLabel(Canvas c, String formattedLabel, float x, float y, MPPointF anchor, float angleDegrees) {
            String line[] = formattedLabel.split("\n");
            if (isData == true) {
                Utils.drawXAxisValue(c, line[0], x, y, mAxisLabelPaint, anchor, angleDegrees);
                Utils.drawXAxisValue(c, line[1], x + mAxisLabelPaint.getTextSize(), y + mAxisLabelPaint.getTextSize(), mAxisLabelPaint, anchor, angleDegrees);
            }
            else {
                Utils.drawXAxisValue(c, line[0], x, y, mAxisLabelPaint, anchor, angleDegrees);
            }
        }
    }
    }