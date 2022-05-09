package com.example.bpmonitorbleintegration;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.google.android.material.tabs.TabLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class Statistics extends AppCompatActivity{

//    LineChart lineChart;
//    LineData lineData;
//    List<Entry> entryList1 = new ArrayList<Entry>();
//    List<Entry> entryList2 = new ArrayList<Entry>();
//    List<Entry> entryList3 = new ArrayList<Entry>();
//    List<Entry> entryList4 = new ArrayList<Entry>();
    List<String> daysList = new ArrayList<>();
    ArrayList<String> timeList = new ArrayList<>();
//    LineDataSet lineDataSet1,lineDataSet2, lineDataSet3;
//    ArrayList systolicVal;
//    ArrayList diastolicVal;
    ArrayList<CandleEntry> yAxisCandleStick, yAxisCandleStick1;

    String TAG = Statistics.class.getName();
    BarChart barChart, barchart1;
//    ScatterChart scatterChart;
//    ScatterData scatterData;
//    ScatterDataSet scatterDataSet1, scatterDataSet2, scatterDataSet3;
    CandleStickChart candleStickChart, candleStickTimeChart;
//    BarData barData;
//    BarDataSet barDataSet;
//    ArrayList barEntriesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

//        lineChart = findViewById(R.id.linechart);
//        barChart = findViewById(R.id.barChart);
//        barchart1 = findViewById(R.id.bar_chart1);
//        barEntriesArrayList = new ArrayList<>();


        candleStickChart = findViewById(R.id.candleStick);
        candleStickTimeChart = findViewById(R.id.candleStick1);
        yAxisCandleStick = new ArrayList<CandleEntry>();
        yAxisCandleStick1 = new ArrayList<CandleEntry>();
        getManualTasks();

//        spin = findViewById(R.id.coursesspinner);
//        calendarView = findViewById(R.id.simpleCalendarView);
//        calendarView.setFirstDayOfWeek(2);
//        spin.setOnItemSelectedListener(this);
//        ArrayAdapter ad = new ArrayAdapter(this, android.R.layout.simple_spinner_item, optiion);
//        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spin.setAdapter(ad);
//        tabLayout = findViewById(R.id.tabLayout);
//        viewPager = findViewById(R.id.viewPager);

//        tabLayout.addTab(tabLayout.newTab().setText("Time Wise"));
//        tabLayout.addTab(tabLayout.newTab().setText("Date Wise"));
//        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
//
//        final myAdapter adapter = new myAdapter(this,getSupportFragmentManager(),tabLayout.getTabCount());
//        viewPager.setAdapter(adapter);
//
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                viewPager.setCurrentItem(tab.getPosition());
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });

//        systolicVal = new ArrayList<>();
//        xAxisValues = new ArrayList<>();
//        xAxisValues1 = new ArrayList<>();


    }


//    @Override
//    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
////        Log.i(TAG, "Position " + i);
//        switch (i) {
//            case 0:
//                selected = i;
//                calendarView.setVisibility(View.GONE);
//                getManualTasks();
//                break;
//
//            case 1:
//                 selected = i;
//                calendarView.setVisibility(View.VISIBLE);
//                calendarView.setLayoutMode(8);
//                calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
//                    @Override
//                    public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
////                        DateFormat df1 = new SimpleDateFormat("MMM dd");
//                        Log.i(TAG,"date " + day + "/" + month + "/" + year);
//                    }
//                });
//                break;
//            default:
//                Log.i(TAG, "No selected item");
//                break;
//        }
//    }
//
//    @Override
//    public void onNothingSelected(AdapterView<?> adapterView) {
//
//    }

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

                plotCandleStick(tasks);
                plotCandleStickTimeWise(tasks);
            }
        }
        GetTasks gt = new GetTasks();
        gt.execute();
    }

    public void plotCandleStick(List<BloodPressureDB> tasks) {
//        yAxisCandleStick.clear();
//        daysList.clear();
        int count = 0;
        for (int i = 0; i < tasks.size(); i++){
            yAxisCandleStick.add(new CandleEntry(count, tasks.get(i).getSystolic(),tasks.get(i).getDystolic(),tasks.get(i).getSystolic(),tasks.get(i).getDystolic()));
            daysList.add(tasks.get(i).getDate());
            count++;
        }
        CandleDataSet cds = new CandleDataSet(yAxisCandleStick, "Blood Pressure");
        cds.setColor(Color.rgb(80, 80, 80));
        cds.setShadowColor(Color.DKGRAY);
        cds.setBarSpace(1f);
        cds.setDecreasingColor(Color.parseColor("#FFA500"));
        cds.setDecreasingPaintStyle(Paint.Style.FILL);
        cds.setIncreasingColor(Color.parseColor("#FFA500"));
        cds.setIncreasingPaintStyle(Paint.Style.STROKE);
        cds.setNeutralColor(Color.BLUE);
        cds.setValueTextColor(Color.BLACK);
        CandleData cd = new CandleData(cds);
        candleStickChart.setData(cd);
        candleStickChart.getXAxis().setLabelCount(daysList.size());
        candleStickChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        candleStickChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(daysList));
        candleStickChart.invalidate();
        candleStickChart.notifyDataSetChanged();
    }

    public void plotCandleStickTimeWise(List<BloodPressureDB> tasks) {
//        yAxisCandleStick1.clear();
//        timeList.clear();
        // To get current date.
        DateFormat df1 = new SimpleDateFormat("MMM dd"); // Format date
        String date = df1.format(Calendar.getInstance().getTime());

        getTimeSet(true);
        Log.i(TAG, "time " + timeList);
        Log.i(TAG, "time size " + timeList.size());
        int count = 0;
        for (int i = 0; i < tasks.size(); i++){
            for (int j = 0; j < timeList.size(); j++) {
                if (date.equals(tasks.get(i).getDate())) {
                    yAxisCandleStick1.add(new CandleEntry(count, tasks.get(i).getSystolic(),tasks.get(i).getDystolic(),tasks.get(i).getSystolic(),tasks.get(i).getDystolic()));
//                timeList.add(tasks.get(i).getTime());
                    count++;
                }
            }
        }

        CandleDataSet cds = new CandleDataSet(yAxisCandleStick1, "Blood Pressure");
        cds.setColor(Color.rgb(80, 80, 80));
        cds.setShadowColor(Color.DKGRAY);
        cds.setBarSpace(1.5f);
        cds.setDecreasingColor(Color.parseColor("#FFA500"));
        cds.setDecreasingPaintStyle(Paint.Style.FILL);
        cds.setIncreasingColor(Color.parseColor("#FFA500"));
        cds.setIncreasingPaintStyle(Paint.Style.STROKE);
        cds.setNeutralColor(Color.BLUE);
        cds.setValueTextColor(Color.BLACK);
        CandleData cd = new CandleData(cds);
        candleStickTimeChart.setData(cd);
        candleStickTimeChart.getXAxis().setLabelCount(timeList.size());
        candleStickTimeChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        candleStickTimeChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(timeList));
        candleStickTimeChart.invalidate();
        candleStickTimeChart.notifyDataSetChanged();
    }

    private void getTimeSet(boolean isCurrentDay) {
        ArrayList results = new ArrayList<String>();
        String timeResult = null;
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 0);// what should be the default?
        if(!isCurrentDay)
            calendar.set(Calendar.HOUR_OF_DAY, 24);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        for (int i = 0; i < 24; i++) {
            String  day1 = sdf.format(calendar.getTime());
//            Log.i(TAG, "day1 " + day1);
            timeResult = day1;
            timeList.add(timeResult);

            // add 15 minutes to the current time; the hour adjusts automatically!
            calendar.add(Calendar.MINUTE, 60);

            String day2 = sdf.format(calendar.getTime());

//            String day = day1 + " - " + day2;
            String day = day1;
            results.add(i, day);
        }
//        return timeResult;
    }
}

//    public void plotBarGraph(List<BloodPressureDB> task)
//    {
////        barEntriesArrayList.add(new BarEntry(1f, 4));
////        barEntriesArrayList.add(new BarEntry(2f, 6));
////        barEntriesArrayList.add(new BarEntry(3f, 8));
////        barEntriesArrayList.add(new BarEntry(4f, 10));
////        barEntriesArrayList.add(new BarEntry(5f, 12));
//        for (BloodPressureDB list : task) {
//            barEntriesArrayList.add(new BarEntry(list.getDystolic(), list.getSystolic()));
//            daysList.add(list.getDate());
//        }
//        barDataSet = new BarDataSet(barEntriesArrayList, "Blood pressure");
//        // adding color to our bar data set.
//        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
//        barData = new BarData(barDataSet);
//        barData.setBarWidth(0.2f);
//        barChart.setData(barData);
//        barDataSet.setValueTextSize(10f);
//        barChart.getDescription().setEnabled(false);
//        XAxis xAxis = barChart.getXAxis();
////        xAxis.setValueFormatter(new IndexAxisValueFormatter(daysList));
//    }
//
//    public void plotStackedGraph(List<BloodPressureDB> task) {
//        barchart1.getDescription().setEnabled(false);
//        barchart1.setMaxVisibleValueCount(40);
//
//    }

//    // Single Line Graph.
////    public void plotLineGraph(List<BloodPressureDB> task){
////        Long tsLong = System.currentTimeMillis()/1000;
////        for (BloodPressureDB list : task) {
////            Log.i(TAG, "value in loop " + list.getSystolic());
//////            Log.i(TAG, "tsLong in loop " + tsLong);
////            entryList.add(new Entry(Integer.valueOf(list.getDate()),list.getSystolic()));
////            xAxisValues.add(list.getDate());
////        }
////        Log.i(TAG, "in entry list " + entryList.toString());
////
//////        xAxisValues = new ArrayList<>(Arrays.asList(date));
////
////        lineDataSet1 = new LineDataSet(entryList, "Systolic");
////        lineDataSet1.setColors(ColorTemplate.JOYFUL_COLORS);
//////        lineDataSet.setValueTextColor(Color.BLACK);
////        lineDataSet1.setValueTextSize(16f);
////        lineDataSet1.setLineWidth(2);
////        lineData = new LineData(lineDataSet1);
////
////        XAxis xAxis = lineChart.getXAxis();
////        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//////        xAxis.setValueFormatter(new IAxisValueFormatter() {
//////            @Override
//////            public String getFormattedValue(float value, AxisBase axis) {
//////                return xAxisValues.get((int)value);
////////                return String.valueOf(xAxisValues);
//////            }
//////
//////        });
////
////        lineChart.setData(lineData);
////        lineChart.invalidate();
////    }
//
//
//    // Multiple dataset line graph (Current day wise graph).
//    public void plotLineGraph(List<BloodPressureDB> task) {
////        Log.i(TAG, "Task list in graph " + task);
//        lineData = lineChart.getData();
//        entryList1.clear();
//        entryList2.clear();
//        timeList.clear();
//        // To get current date.
//        DateFormat df1 = new SimpleDateFormat("MMM dd"); // Format date
//        date = df1.format(Calendar.getInstance().getTime());
//
//        int count = 0;
//        for (i = 0; i < task.size(); i++){
////            if (date.equals(task.get(i).getDate())) {
//
//                entryList1.add(new Entry(count, task.get(i).getSystolic()));
//                entryList2.add(new Entry(count, task.get(i).getDystolic()));
////                entryList3.add(new Entry(i, task.get(i).getHeartRate()));
//                timeList.add(task.get(i).getTime());
//                count++;
////            }
//        }
//
//        Log.i(TAG, "systolic " + entryList1);
//        Log.i(TAG, "Diastolic " + entryList2);
//        Log.i(TAG, "TimeList " + timeList + "Size " + timeList.size());
//        Log.i(TAG, "Count " + count);
//        lineDataSet1 = new LineDataSet(entryList1,"Systolic");
//        lineDataSet1.setColor(Color.MAGENTA);
//        lineDataSet1.setValueTextColor(Color.BLACK);
//        lineDataSet1.setValueTextSize(10f);
//        lineDataSet1.setLineWidth(2);
//
//        lineDataSet2 = new LineDataSet(entryList2,"Diastolic");
//        lineDataSet2.setColor(Color.RED);
//        lineDataSet2.setValueTextColor(Color.BLACK);
//        lineDataSet2.setValueTextSize(10f);
//        lineDataSet2.setLineWidth(2);
//
//        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
//        dataSets.add(lineDataSet1);
//        dataSets.add(lineDataSet2);
//
//        lineData = new LineData(dataSets);
//
//        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setDrawAxisLine(true);
//        xAxis.setDrawLabels(true);
////        xAxis.setLabelCount(timeList.size());
//        xAxis.setValueFormatter(new IndexAxisValueFormatter(timeList));
//        lineChart.setData(lineData);
//        //Leave some space before the line
////        xAxis.setSpaceMin(50f);
//////Leave some space after the line
////        xAxis.setSpaceMax(50f);
//        if (entryList1.size() > 10 && entryList2.size() > 10){
//            lineChart.setVisibleXRangeMaximum(10f);
//        }
//        lineChart.animateXY(1000,1000);
//        lineChart.notifyDataSetChanged();
//        lineChart.invalidate();
//    }

//    // Multiple dataset line graph.
//    public void plotLineGraph(List<BloodPressureDB> task) {
//        lineData = lineChart.getData();
//
//        int count = 0;
//        for (i = 0; i < task.size(); i++){
//                entryList1.add(new Entry(count, task.get(i).getSystolic()));
//                entryList2.add(new Entry(count, task.get(i).getDystolic()));
////                timeList.add(task.get(i).getTime());
//            daysList.add(task.get(i).getDate());
//                count++;
//        }
//
//        lineDataSet1 = new LineDataSet(entryList1,"Systolic");
//        lineDataSet1.setColor(Color.MAGENTA);
//        lineDataSet1.setValueTextColor(Color.BLACK);
//        lineDataSet1.setValueTextSize(12f);
//        lineDataSet1.setLineWidth(2);
//
//        lineDataSet2 = new LineDataSet(entryList2,"Diastolic");
//        lineDataSet2.setColor(Color.RED);
//        lineDataSet2.setValueTextColor(Color.BLACK);
//        lineDataSet2.setValueTextSize(12f);
//        lineDataSet2.setLineWidth(2);
//
//        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
//        dataSets.add(lineDataSet1);
//        dataSets.add(lineDataSet2);
//
//        lineData = new LineData(dataSets);
//
//        // To remove duplicates from arraylist.
//        List<String> newList = new ArrayList<>();
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//            newList = daysList.stream()
//                    .distinct()
//                    .collect(Collectors.toList());
////            Log.i(TAG, "new list " + newList);
//        }
//
//        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setDrawAxisLine(true);
//        xAxis.setDrawLabels(true);
//        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(newList));
//        lineChart.setData(lineData);
//        //Leave some space before the line
////        xAxis.setSpaceMin(50f);
//////Leave some space after the line
////        xAxis.setSpaceMax(50f);
//        if (entryList1.size() > 10 && entryList2.size() > 10){
//            lineChart.setVisibleXRangeMaximum(10f);
//        }
//
//        lineChart.notifyDataSetChanged();
//        lineChart.invalidate();
//    }

    // Multiple datasets scatter chart.
//    public void plotScatterGraph(List<BloodPressureDB> task) {
//        scatterData = scatterChart.getData();
//        for (BloodPressureDB tasks : task) {
//        entryList3.add(new Entry(0,tasks.getSystolic()));
//        entryList4.add(new Entry(1,tasks.getDystolic()));
//        daysList.add(tasks.getDate());
//    }
//        scatterDataSet1 = new ScatterDataSet(entryList3,"Systolic");
//        scatterDataSet1.setColor(Color.MAGENTA);
//        scatterDataSet1.setValueTextColor(Color.BLACK);
//        scatterDataSet1.setValueTextSize(12f);
//
//        scatterDataSet2 = new ScatterDataSet(entryList4,"Diastolic");
//        scatterDataSet2.setColor(Color.RED);
//        scatterDataSet2.setValueTextColor(Color.BLACK);
//        scatterDataSet2.setValueTextSize(12f);
//
//        ArrayList<IScatterDataSet> dataSets = new ArrayList<>();
//        dataSets.add(scatterDataSet1);
//        dataSets.add(scatterDataSet2);
////        dataSets.add(scatterDataSet3);
//
//        scatterData = new ScatterData(dataSets);
//
//        XAxis xAxis = scatterChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setValueFormatter(new IndexAxisValueFormatter(daysList));
//        scatterChart.setData(scatterData);
//        scatterChart.setVisibleXRangeMaximum(65f);
//        scatterChart.notifyDataSetChanged();
//        scatterChart.invalidate();
//
//    }

