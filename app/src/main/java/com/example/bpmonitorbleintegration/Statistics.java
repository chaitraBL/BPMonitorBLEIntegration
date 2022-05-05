package com.example.bpmonitorbleintegration;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarEntry;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class Statistics extends AppCompatActivity {

    LineChart lineChart;
    LineData lineData;
    List<Entry> entryList1 = new ArrayList<Entry>();
    List<Entry> entryList2 = new ArrayList<Entry>();
    List<Entry> entryList3 = new ArrayList<Entry>();
    List<Entry> entryList4 = new ArrayList<Entry>();
    List<String> daysList = new ArrayList<>();
    List<String> timeList = new ArrayList<>();
    LineDataSet lineDataSet1,lineDataSet2, lineDataSet3;
    ArrayList<String> systolicVal;
    String TAG = Statistics.class.getName();
    ScatterChart scatterChart;
    ScatterData scatterData;
    ScatterDataSet scatterDataSet1, scatterDataSet2, scatterDataSet3;

    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        lineChart = findViewById(R.id.linechart);
        scatterChart = findViewById(R.id.scatterChart);
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

        systolicVal = new ArrayList<>();
//        xAxisValues = new ArrayList<>();
//        xAxisValues1 = new ArrayList<>();

        getManualTasks();
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

//                task = tasks;
                // To get current date.
                DateFormat df1 = new SimpleDateFormat("MMM dd"); // Format date
                String date = df1.format(Calendar.getInstance().getTime());

                // To get current date and time.
                SimpleDateFormat TimeFormat = new SimpleDateFormat("hh:mm"); // Format time

                Calendar ATime = Calendar.getInstance();
                String Timein12hourFormat = TimeFormat.format(ATime.getTime());

//                for (i = 0; i < tasks.size(); i++){
//                    Log.i(TAG,"Date " + tasks.get(i).getDate());
//                    Log.i(TAG,"time " + tasks.get(i).getTime());
//                    Log.i(TAG,"Time " + Timein12hourFormat);
//                     Log.i(TAG, "date " + date);
//                     if (date.equals(tasks.get(i).getDate()));
//                    {
//                getDate(tasks);
                        plotLineGraph(tasks);
//                setLineChart(tasks);

                        plotScatterGraph(tasks);
//                        list.addAll(i,tasks);
//                        Log.i(TAG, "tasks " + list);
//                    }
//                }

            }
        }
        GetTasks gt = new GetTasks();
        gt.execute();
    }
//
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

    public ArrayList<String> getData(List<BloodPressureDB> list) {

//        Log.i(TAG, "List " + list);
//        Log.i(TAG, "size " + list.size());
        ArrayList<String> label = new ArrayList<>();
        for (int i = 0; i < list.size(); i++)
            label.add(list.get(i).getDate());
        return label;
    }

//    // Multiple dataset line graph (Current day wise graph).
//    public void plotLineGraph(List<BloodPressureDB> task) {
//        lineData = lineChart.getData();
//        // To get current date.
//        DateFormat df1 = new SimpleDateFormat("MMM dd"); // Format date
//        String date = df1.format(Calendar.getInstance().getTime());
//
//        int count = 0;
//        for (i = 0; i < task.size(); i++){
//            if (date.equals(task.get(i).getDate())) {
//
//                entryList1.add(new Entry(count, task.get(i).getSystolic()));
//                entryList2.add(new Entry(count, task.get(i).getDystolic()));
////                entryList3.add(new Entry(i, task.get(i).getHeartRate()));
//                timeList.add(task.get(i).getTime());
//                count++;
//            }
//        }
//
//        Log.i(TAG, "systolic " + entryList1);
//        Log.i(TAG, "Diastolic " + entryList2);
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
//        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
////        xAxis.setValueFormatter(new IndexAxisValueFormatter(getDate()));
//        xAxis.setDrawAxisLine(true);
//////        xAxis.setLabelCount(xAxisValues.size());
//        xAxis.setDrawLabels(true);
//        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(timeList));
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

    // Multiple dataset line graph.
    public void plotLineGraph(List<BloodPressureDB> task) {
        lineData = lineChart.getData();
        // To get current date.
        DateFormat df1 = new SimpleDateFormat("MMM dd"); // Format date
        String date = df1.format(Calendar.getInstance().getTime());

        int count = 0;
        for (i = 0; i < task.size(); i++){
//            if (date.equals(task.get(i).getDate())) {

                entryList1.add(new Entry(count, task.get(i).getSystolic()));
                entryList2.add(new Entry(count, task.get(i).getDystolic()));
//                entryList3.add(new Entry(i, task.get(i).getHeartRate()));
//                timeList.add(task.get(i).getTime());
            daysList.add(task.get(i).getDate());
                count++;
//            }
        }

        lineDataSet1 = new LineDataSet(entryList1,"Systolic");
        lineDataSet1.setColor(Color.MAGENTA);
        lineDataSet1.setValueTextColor(Color.BLACK);
        lineDataSet1.setValueTextSize(12f);
        lineDataSet1.setLineWidth(2);

        lineDataSet2 = new LineDataSet(entryList2,"Diastolic");
        lineDataSet2.setColor(Color.RED);
        lineDataSet2.setValueTextColor(Color.BLACK);
        lineDataSet2.setValueTextSize(12f);
        lineDataSet2.setLineWidth(2);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet1);
        dataSets.add(lineDataSet2);

        lineData = new LineData(dataSets);

        // To remove duplicates from arraylist.
        List<String> newList = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            newList = daysList.stream()
                    .distinct()
                    .collect(Collectors.toList());
            Log.i(TAG, "new list " + newList);
        }

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawLabels(true);
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(newList));
        lineChart.setData(lineData);
        //Leave some space before the line
//        xAxis.setSpaceMin(50f);
////Leave some space after the line
//        xAxis.setSpaceMax(50f);
        if (entryList1.size() > 10 && entryList2.size() > 10){
            lineChart.setVisibleXRangeMaximum(10f);
        }

        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    // Multiple datasets scatter chart.
    public void plotScatterGraph(List<BloodPressureDB> task) {
        scatterData = scatterChart.getData();
        for (BloodPressureDB tasks : task) {
        entryList3.add(new Entry(0,tasks.getSystolic()));
        entryList4.add(new Entry(1,tasks.getDystolic()));
    }
        scatterDataSet1 = new ScatterDataSet(entryList3,"Systolic");
        scatterDataSet1.setColor(Color.MAGENTA);
        scatterDataSet1.setValueTextColor(Color.BLACK);
        scatterDataSet1.setValueTextSize(12f);

        scatterDataSet2 = new ScatterDataSet(entryList4,"Diastolic");
        scatterDataSet2.setColor(Color.RED);
        scatterDataSet2.setValueTextColor(Color.BLACK);
        scatterDataSet2.setValueTextSize(12f);

        ArrayList<IScatterDataSet> dataSets = new ArrayList<>();
        dataSets.add(scatterDataSet1);
        dataSets.add(scatterDataSet2);
//        dataSets.add(scatterDataSet3);

        scatterData = new ScatterData(dataSets);

        XAxis xAxis = scatterChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getData(task)));
        scatterChart.setData(scatterData);
        scatterChart.setVisibleXRangeMaximum(65f);
        scatterChart.notifyDataSetChanged();
        scatterChart.invalidate();

    }

}