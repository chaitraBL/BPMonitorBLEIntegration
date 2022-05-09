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

    List<String> daysList = new ArrayList<>();
    ArrayList<String> timeList = new ArrayList<>();
    ArrayList<CandleEntry> yAxisCandleStick, yAxisCandleStick1;

    String TAG = Statistics.class.getName();
    CandleStickChart candleStickChart, candleStickTimeChart;

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

                plotCandleStick(tasks);
                plotCandleStickTimeWise(tasks);
            }
        }
        GetTasks gt = new GetTasks();
        gt.execute();
    }

    // Candle stick chart date based.
    public void plotCandleStick(List<BloodPressureDB> tasks) {
        yAxisCandleStick.clear();
        daysList.clear();
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
        candleStickChart.getXAxis().setLabelRotationAngle(-45);
//        candleStickChart.getAxisLeft().setDrawGridLines(false);
        candleStickChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(daysList));
        if (yAxisCandleStick.size() >= 6) {
            candleStickChart.setVisibleXRangeMaximum(6f);
        }
        candleStickChart.invalidate();
        candleStickChart.notifyDataSetChanged();
        candleStickChart.animateXY(1000,1000);
    }

    // Candle stick chart time based.
    public void plotCandleStickTimeWise(List<BloodPressureDB> tasks) {
        yAxisCandleStick1.clear();
        timeList.clear();
        // To get current date.
        DateFormat df1 = new SimpleDateFormat("MMM dd"); // Format date
        String date = df1.format(Calendar.getInstance().getTime());

//        getTimeSet(true);

        int count = 0;
        for (int i = 0; i < tasks.size(); i++){
//            for (int j = 0; j < timeList.size(); j++) {
                if (date.equals(tasks.get(i).getDate())) {
                    yAxisCandleStick1.add(new CandleEntry(count, tasks.get(i).getSystolic(),tasks.get(i).getDystolic(),tasks.get(i).getSystolic(),tasks.get(i).getDystolic()));
                timeList.add(tasks.get(i).getTime());
                    count++;
//                }
            }
        }

        CandleDataSet cds = new CandleDataSet(yAxisCandleStick1, "Blood Pressure");
        cds.setColor(Color.rgb(80, 80, 80));
        cds.setShadowColor(Color.DKGRAY);
        cds.setBarSpace(1f);
        cds.setDecreasingColor(Color.parseColor("#151B54"));
        cds.setDecreasingPaintStyle(Paint.Style.FILL);
        cds.setIncreasingColor(Color.parseColor("#151B54"));
        cds.setIncreasingPaintStyle(Paint.Style.STROKE);
        cds.setNeutralColor(Color.BLUE);
        cds.setValueTextColor(Color.BLACK);
        CandleData cd = new CandleData(cds);
        candleStickTimeChart.setData(cd);
        candleStickTimeChart.getXAxis().setLabelCount(timeList.size());
        candleStickTimeChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        candleStickTimeChart.getXAxis().setLabelRotationAngle(-45);
//        candleStickTimeChart.getAxisLeft().setDrawGridLines(false);
        candleStickTimeChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(timeList));
        if (yAxisCandleStick1.size() >= 6) {
            candleStickTimeChart.setVisibleXRangeMaximum(6f);
        }
        candleStickTimeChart.invalidate();
        candleStickTimeChart.notifyDataSetChanged();
        candleStickTimeChart.animateXY(1000,1000);
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

