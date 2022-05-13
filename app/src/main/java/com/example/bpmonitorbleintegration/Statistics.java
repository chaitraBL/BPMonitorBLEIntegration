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
import android.widget.Toast;

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
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
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

    List<String> daysList = new ArrayList<>();
    ArrayList<String> timeList = new ArrayList<>();
    ArrayList<CandleEntry> yAxisCandleStick, yAxisCandleStick1;

    String TAG = Statistics.class.getName();
    CandleStickChart candleStickChart, candleStickTimeChart;
    List<String> dateList = new ArrayList<>();
    List<Integer> systolic = new ArrayList<>();
    List<Integer> diastolic = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        candleStickChart = findViewById(R.id.candleStick);
        candleStickTimeChart = findViewById(R.id.candleStick1);
        yAxisCandleStick = new ArrayList<CandleEntry>();
        yAxisCandleStick1 = new ArrayList<CandleEntry>();
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

                if (tasks.isEmpty()) {
                   Log.d(TAG,"No data found");
                }
                else{
                    plotCandleStick(tasks);
                    plotCandleStickTimeWise(tasks);
                }

//                for (int i = 0; i < tasks.size(); i++){
//                    dateList.add(tasks.get(i).getDate());
//                    systolic.add(tasks.get(i).getSystolic());
//                    diastolic.add(tasks.get(i).getDystolic());
//                    dateFormateForValue(dateList,systolic,diastolic);
//                }
            }
        }
        GetTasks gt = new GetTasks();
        gt.execute();
    }

//    public void dateFormateForValue(List<String> date, List<Integer> systolicVal, List<Integer> diastolic) {
//        Log.d(TAG, "date " + date);
//        Log.d(TAG, "systolic " + systolicVal);
//        Log.d(TAG, "diastolic " + diastolic);
//
//
//    }

    // Candle stick chart date based.
    public void plotCandleStick(List<BloodPressureDB> tasks) {
        yAxisCandleStick.clear();
        daysList.clear();
        if (tasks.isEmpty()) {
            Toast.makeText(Statistics.this, "No data available", Toast.LENGTH_SHORT).show();
        }
        else {
            int count = 0;
            for (int i = 0; i < tasks.size(); i++){
                yAxisCandleStick.add(new CandleEntry(count, tasks.get(i).getSystolic(),tasks.get(i).getDystolic(),tasks.get(i).getSystolic(),tasks.get(i).getDystolic()));
                daysList.add(tasks.get(i).getDate());
                count++;
            }

            // To remove duplicates in array list.
//            List<String> nonDup;
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//               nonDup = daysList.stream().distinct().collect(Collectors.toList());
//               Log.i(TAG, "Non duplicates " + nonDup);
//            }

            Collections.sort(yAxisCandleStick,new EntryXComparator());

            CandleDataSet cds = new CandleDataSet(yAxisCandleStick, "");
            cds.setColor(Color.rgb(80, 80, 80));
            cds.setShadowColor(Color.DKGRAY);
            cds.setBarSpace(1f);
            cds.setDecreasingColor(Color.parseColor("#FFA500"));
            cds.setDecreasingPaintStyle(Paint.Style.FILL);
            cds.setIncreasingColor(Color.parseColor("#FFA500"));
            cds.setIncreasingPaintStyle(Paint.Style.STROKE);
            cds.setNeutralColor(Color.BLUE);
            cds.setValueTextColor(Color.BLACK);
            cds.setValueTextSize(10);
            CandleData cd = new CandleData(cds);
            candleStickChart.setData(cd);
            candleStickChart.getDescription().setEnabled(false);

            // X axis
            XAxis xAxis = candleStickChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(daysList.size());
            xAxis.setValueFormatter(new IndexAxisValueFormatter(daysList));
            xAxis.setAvoidFirstLastClipping(true);
            xAxis.setLabelRotationAngle(-45);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(false);
            xAxis.setGranularity(1f);
            xAxis.setGranularityEnabled(true);
            xAxis.setCenterAxisLabels(false);
            xAxis.setEnabled(true);

            //Y axis
            YAxis yAxisRight = candleStickChart.getAxisRight();
            yAxisRight.setEnabled(false);
            YAxis yAxisLeft = candleStickChart.getAxisLeft();
            yAxisLeft.setLabelCount(6,true); // make it as 12 and check
            yAxisLeft.setDrawAxisLine(false);
            yAxisLeft.setAxisMinimum(50);
            yAxisLeft.setAxisMaximum(150);

            if (yAxisCandleStick.size() > 1){
                Entry lastEntry = yAxisCandleStick.get(yAxisCandleStick.size()-1);
                Highlight highlight = new Highlight(lastEntry.getX(), lastEntry.getY(), 0);
                highlight.setDataIndex(0);
                candleStickChart.highlightValue(highlight);
                candleStickChart.moveViewToX(daysList.size()-1);
            }
            else
            {
                Log.i(TAG, "No data found!!!");
            }

            if (yAxisCandleStick.size() >= 6) {
                candleStickChart.setVisibleXRangeMaximum(6);
            }
            else {
                candleStickChart.invalidate();
            }
            candleStickChart.invalidate();
            candleStickChart.notifyDataSetChanged();
            candleStickChart.animateXY(1000,1000);
        }
    }

    // Candle stick chart time based.
    public void plotCandleStickTimeWise(List<BloodPressureDB> tasks) {
        yAxisCandleStick1.clear();
        timeList.clear();
        if (tasks.isEmpty()) {
            Toast.makeText(Statistics.this, "No data available", Toast.LENGTH_SHORT).show();
        }
        else {
            // To get current date.
            DateFormat df1 = new SimpleDateFormat("MMM dd"); // Format date
            String date = df1.format(Calendar.getInstance().getTime());

//        getTimeSet(true);

            int count = 0;
            for (int i = 0; i < tasks.size(); i++){
                if (date.equals(tasks.get(i).getDate())) {
                    yAxisCandleStick1.add(new CandleEntry(count, tasks.get(i).getSystolic(),tasks.get(i).getDystolic(),tasks.get(i).getSystolic(),tasks.get(i).getDystolic()));
                    timeList.add(tasks.get(i).getTime());
                    count++;
                }
            }

            Collections.sort(yAxisCandleStick1,new EntryXComparator());

            CandleDataSet cds = new CandleDataSet(yAxisCandleStick1, "");
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
            candleStickTimeChart.setData(cd);
            candleStickTimeChart.getDescription().setEnabled(false);

            //X axis
            XAxis xAxis = candleStickTimeChart.getXAxis();
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

            //Y axis
            YAxis yAxisRight = candleStickTimeChart.getAxisRight();
            yAxisRight.setEnabled(false);
            YAxis yAxisLeft = candleStickTimeChart.getAxisLeft();
            yAxisLeft.setLabelCount(6,true);
            yAxisLeft.setDrawAxisLine(false);
            yAxisLeft.setAxisMinimum(50);
            yAxisLeft.setAxisMaximum(150);

            if (yAxisCandleStick1.size() > 1){
                Entry lastEntry = yAxisCandleStick1.get(yAxisCandleStick1.size()-1);
                Highlight highlight = new Highlight(lastEntry.getX(), lastEntry.getY(), 0);
                highlight.setDataIndex(0);
                candleStickTimeChart.highlightValue(highlight);
                candleStickTimeChart.moveViewToX(timeList.size()-1);
            }
            else
            {
                Log.i(TAG, "No data found!!!");
            }

            if (yAxisCandleStick1.size() >= 6) {
                candleStickTimeChart.setVisibleXRangeMaximum(6);
            }
            else
            {
                candleStickTimeChart.invalidate();
            }
            candleStickTimeChart.invalidate();
            candleStickTimeChart.notifyDataSetChanged();
            candleStickTimeChart.animateXY(1000,1000);
        }
    }

    // To get the list of time intervals for an hour.
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
//            timeList.add(timeResult);

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

