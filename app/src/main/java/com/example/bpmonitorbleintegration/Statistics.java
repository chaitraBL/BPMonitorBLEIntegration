package com.example.bpmonitorbleintegration;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.github.mikephil.charting.utils.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

public class Statistics extends AppCompatActivity {

    ArrayList<String> daysList = new ArrayList<>();
    ArrayList<String> timeList = new ArrayList<>();
    ArrayList<CandleEntry> yAxisCandleStick, yAxisCandleStick1, yAxisCandleStick2, yAxisCandle;
    ArrayList<String> dateList = new ArrayList<>();
    List<String> newList = new ArrayList<>();

    String TAG = Statistics.class.getName();
    CandleStickChart candleStickChart,candleStickTimeChart;
    ArrayList<Integer> heartRate = new ArrayList<>();
    ArrayList<Integer> systolicVal = new ArrayList<>();
    ArrayList<Integer> diastolicVal = new ArrayList<>();
    List<BloodPressureDB> pressureList = new ArrayList<>();
    Button timeButton, dayButton, allButton;
    ViewPager viewPager;

    private boolean isTimeEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        candleStickChart = findViewById(R.id.candleStick);
        timeButton = findViewById(R.id.new_week_label);
        dayButton = findViewById(R.id.new_month_label);
        allButton = findViewById(R.id.new_year_label);
        viewPager = findViewById(R.id.new_view_pager);
        candleStickTimeChart = findViewById(R.id.candleStick1);
        yAxisCandleStick = new ArrayList<CandleEntry>();
        yAxisCandleStick1 = new ArrayList<CandleEntry>();
        yAxisCandleStick2 = new ArrayList<CandleEntry>();
        yAxisCandle = new ArrayList<CandleEntry>();

        getManualTasks();

//        String s = "2022-05-09";
//        String e = "2022-05-15";
//        List<LocalDate> totalDates = new ArrayList<>();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        LocalDate start = LocalDate.parse(s);
//        LocalDate end = LocalDate.parse(e);
//            while (!start.isAfter(end)) {
//                totalDates.add(start);
//                start = start.plusDays(1);
//            }
//            Log.d(TAG, "onCreate: totaldates " + totalDates);
//            compareAndGetValues(pressureList, totalDates);
//        }


        candleStickChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

                if (e instanceof CandleEntry) {
                    CandleEntry ce = (CandleEntry) e;
                    Toast.makeText(getApplicationContext(), "Systolic: " + Utils.formatNumber(ce.getHigh(), 0, true) + " Diastolic: " + Utils.formatNumber(ce.getLow(),0,true), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), " " + Utils.formatNumber(e.getY(),0,true),Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onNothingSelected() {

            }
        });

        candleStickTimeChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

                String xAxisVal = newList.get((int) e.getX());

                Intent intent = new Intent(Statistics.this,Statistics2.class);
                intent.putExtra("xAxisVal", xAxisVal);
                startActivity(intent);
            }

            @Override
            public void onNothingSelected() {

            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (timeButton.isClickable()) {
            timeButton.setBackgroundColor(Color.parseColor("#FFA500"));
            timeButton.setTextColor(Color.BLACK);
            plotCandleStickTimeWise(pressureList);
            candleStickChart.invalidate();
            candleStickChart.notifyDataSetChanged();
            dayButton.setBackground(null);
            allButton.setBackground(null);
            isTimeEnabled = true;
        }

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timeButton.isClickable()) {
                    dayButton.setBackground(null);
                    allButton.setBackground(null);
                    isTimeEnabled = true;
                    timeButton.setBackgroundColor(Color.parseColor("#FFA500"));
                    timeButton.setTextColor(Color.BLACK);
                    timeButton.setClickable(true);
                    plotCandleStickTimeWise(pressureList);
                    candleStickChart.invalidate();
                    candleStickChart.notifyDataSetChanged();

                }

            }
        });

        dayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dayButton.isClickable()) {
                    timeButton.setBackground(null);
                    allButton.setBackground(null);
                    isTimeEnabled = false;
                    dayButton.setBackgroundColor(Color.parseColor("#FFA500"));
                    dayButton.setTextColor(Color.BLACK);
                    dayButton.setClickable(true);
                    plotAverageCandleStick(pressureList);
                    candleStickChart.invalidate();
                    candleStickChart.notifyDataSetChanged();


                }

            }
        });

        allButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (allButton.isClickable()) {
                    dayButton.setBackground(null);
                    timeButton.setBackground(null);
                    isTimeEnabled = false;
                    allButton.setBackgroundColor(Color.parseColor("#FFA500"));
                    allButton.setTextColor(Color.BLACK);
                    allButton.setClickable(true);
                    plotCandleStick(pressureList);
                    candleStickChart.invalidate();
                    candleStickChart.notifyDataSetChanged();

                }

            }
        });

    }

    public void compareAndGetValues(List<BloodPressureDB> tasks, List<LocalDate> dates) {
        List<String> finalDate = new ArrayList<>();
        List<String> datesInDB = new ArrayList<>();
        Date date1 = null;
        List<Integer> finalSysta = new ArrayList<>();

        for (BloodPressureDB task : tasks) {
            datesInDB.add(task.getDate());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            newList = datesInDB.stream().distinct().collect(Collectors.toList());
        }

        for (LocalDate date : dates) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                ZoneId defaultZoneId = ZoneId.systemDefault();
                date1 = Date.from(date.atStartOfDay(defaultZoneId).toInstant());
            }
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd");
            String newDates = DATE_FORMAT.format(date1);

            Log.d(TAG, "compareAndGetValues: newDates " + newDates);
            for (int i = 0; i < tasks.size(); i++) {
                if (newDates.equals(tasks.get(i).getDate())) {
                    Log.d(TAG, "compareAndGetValues: systa" + tasks.get(i).getSystolic());
                    List<Integer> systolicDate = new ArrayList<>();
                    systolicDate.add(tasks.get(i).getSystolic());
                    Log.d(TAG, "compareAndGetValues: systolicDate " + systolicDate);
                    finalSysta.add(systolicDate.get(1));
                }
            }
            Log.d(TAG, "compareAndGetValues: finalSysta " + finalSysta);
//            finalDate.add(newDates);
//            Log.d(TAG, "compareAndGetValues: final date " + finalDate);
        }

    }

//    public static List<Date> getDaysBetweenDates(Date startdate, Date enddate)
//    {
//        List<Date> dates = new ArrayList<Date>();
//        Calendar calendar = new GregorianCalendar();
//        calendar.setTime(startdate);
//
//        while (calendar.getTime().before(enddate))
//        {
//            Date result = calendar.getTime();
//            dates.add(result);
//            calendar.add(Calendar.DATE, 1);
//        }
//        return dates;
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
                for (BloodPressureDB list : tasks) {
                    pressureList.add(list);
                }

//                String s = "2022-05-09";
//                String e = "2022-05-15";
//                List<LocalDate> totalDates = new ArrayList<>();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    LocalDate start = LocalDate.parse(s);
//                    LocalDate end = LocalDate.parse(e);
//                    while (!start.isAfter(end)) {
//                        totalDates.add(start);
//                        start = start.plusDays(1);
//                    }
//                    Log.d(TAG, "onCreate: totaldates " + totalDates);
//                    compareAndGetValues(pressureList, totalDates);
//                }
                plotCandleStick1(tasks);

            }
        }
        GetTasks gt = new GetTasks();
        gt.execute();
    }

    // Candle stick chart date based.
    public void plotAverageCandleStick(List<BloodPressureDB> task) {
        yAxisCandleStick2.clear();
        candleStickChart.clear();
//
        if(task != null && task.size() > 0) {
//
            ArrayList<Integer> systolicList = new ArrayList<Integer>(Arrays.asList(131, 124, 113, 126, 122, 130));
            ArrayList<Integer> diastolicList = new ArrayList<Integer>(Arrays.asList(91, 95, 89, 92, 92, 91));

            for (int i = 0; i < task.size(); i++){
                dateList.add(task.get(i).getDate());

//                daysList.add(tasks.get(i).getDate());
//                heartRate.add(tasks.get(i).getHeartRate());
            }

            yAxisCandleStick2.add(new CandleEntry(0, 131,91,131,91));
            yAxisCandleStick2.add(new CandleEntry(1, 124,95,124,95));
            yAxisCandleStick2.add(new CandleEntry(2, 113,89,113,89));
            yAxisCandleStick2.add(new CandleEntry(3, 126,92,126,92));
            yAxisCandleStick2.add(new CandleEntry(4, 122,92,122,92));
            yAxisCandleStick2.add(new CandleEntry(5, 130,91,130,91));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                newList = dateList.stream().distinct().collect(Collectors.toList());
            }

            Collections.sort(yAxisCandleStick2,new EntryXComparator());

            CandleDataSet cds = new CandleDataSet(yAxisCandleStick2, "");
            cds.setColor(Color.rgb(80, 80, 80));
            cds.setShadowColor(Color.DKGRAY);
            cds.setBarSpace(1f);
            cds.setDecreasingColor(Color.parseColor("#333355"));
            cds.setDecreasingPaintStyle(Paint.Style.FILL);
            cds.setIncreasingColor(Color.parseColor("#333355"));
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
            xAxis.setLabelCount(newList.size());
            xAxis.setValueFormatter(new IndexAxisValueFormatter(newList));
            xAxis.setAvoidFirstLastClipping(true);
//            xAxis.setLabelRotationAngle(-45);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(false);
            xAxis.setGranularity(1f);
            xAxis.setGranularityEnabled(true);
            xAxis.setCenterAxisLabels(false);
            xAxis.setEnabled(true);
            CustomMarkerView mv = new CustomMarkerView(Statistics.this, R.layout.marker_view);
//            mv.setChartView(candleStickChart);
            candleStickChart.setMarkerView(mv);

            //Y axis
            YAxis yAxisRight = candleStickChart.getAxisRight();
            yAxisRight.setEnabled(false);
            YAxis yAxisLeft = candleStickChart.getAxisLeft();
            yAxisLeft.setLabelCount(6,true);
            yAxisLeft.setDrawAxisLine(false);
            yAxisLeft.setAxisMinimum(50);
            yAxisLeft.setAxisMaximum(200);

            if (yAxisCandleStick2.size() > 1){
                Entry lastEntry = yAxisCandleStick2.get(yAxisCandleStick2.size()-1);
                Highlight highlight = new Highlight(lastEntry.getX(), lastEntry.getY(), 0);
                highlight.setDataIndex(0);
                candleStickChart.highlightValue(highlight);
                candleStickChart.moveViewToX(newList.size()-1);
            }
            else
            {
                Log.i(TAG, "No data found!!!");
            }

            if (yAxisCandleStick2.size() >= 6) {
                candleStickChart.setVisibleXRangeMaximum(6);
            }
            else {
                candleStickChart.invalidate();
            }
            candleStickChart.invalidate();
            candleStickChart.notifyDataSetChanged();
            candleStickChart.animateXY(1000,1000);
        }
        else {
            Log.d(TAG, "Data not found");
        }
    }

    // Candle stick chart date based.
    public void plotCandleStick(List<BloodPressureDB> tasks) {
        yAxisCandleStick.clear();
        daysList.clear();
        candleStickChart.clear();

       if(tasks.size() > 0 && tasks != null) {

            int count = 0;
            for (int i = 0; i < tasks.size(); i++){
                yAxisCandleStick.add(new CandleEntry(count, tasks.get(i).getSystolic(),tasks.get(i).getDystolic(),tasks.get(i).getSystolic(),tasks.get(i).getDystolic()));
                daysList.add(tasks.get(i).getDate() + "\n" + tasks.get(i).getTime());
                heartRate.add(tasks.get(i).getHeartRate());
                count++;
            }

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
            CustomMarkerView mv = new CustomMarkerView(Statistics.this, R.layout.marker_view);
//            mv.setChartView(candleStickChart);
            candleStickChart.setMarkerView(mv);

            //Y axis
            YAxis yAxisRight = candleStickChart.getAxisRight();
            yAxisRight.setEnabled(false);
            YAxis yAxisLeft = candleStickChart.getAxisLeft();
            yAxisLeft.setLabelCount(6,true);
            yAxisLeft.setDrawAxisLine(false);
            yAxisLeft.setAxisMinimum(50);
            yAxisLeft.setAxisMaximum(200);

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
       else {
           Log.d(TAG, "Data not found");
       }
    }

    // Candle stick chart time based.
    public void plotCandleStickTimeWise(List<BloodPressureDB> tasks) {
        yAxisCandleStick1.clear();
        timeList.clear();
        candleStickChart.clear();

       if (tasks.size() > 0 && tasks != null) {
            // To get current date.
            DateFormat df1 = new SimpleDateFormat("MMM dd"); // Format date
            String date = df1.format(Calendar.getInstance().getTime());

            int count = 0;
            for (BloodPressureDB list : tasks) {
                if ("May 15".equals(list.getDate())) {
                    yAxisCandleStick1.add(new CandleEntry(count, list.getSystolic(),list.getDystolic(),list.getSystolic(),list.getDystolic()));
                    timeList.add(list.getTime());
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
           candleStickChart.setData(cd);
           candleStickChart.getDescription().setEnabled(false);

            //X axis
            XAxis xAxis = candleStickChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(timeList.size());
            xAxis.setValueFormatter(new IndexAxisValueFormatter(timeList));
            xAxis.setAvoidFirstLastClipping(true);
//            xAxis.setLabelRotationAngle(-45);
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
            yAxisLeft.setLabelCount(6,true);
            yAxisLeft.setDrawAxisLine(false);
            yAxisLeft.setAxisMinimum(50);
            yAxisLeft.setAxisMaximum(200);

            if (yAxisCandleStick1.size() > 1){
                Entry lastEntry = yAxisCandleStick1.get(yAxisCandleStick1.size()-1);
                Highlight highlight = new Highlight(lastEntry.getX(), lastEntry.getY(), 0);
                highlight.setDataIndex(0);
                candleStickChart.highlightValue(highlight);
                candleStickChart.moveViewToX(timeList.size()-1);
            }
            else
            {
                Log.i(TAG, "No data found!!!");
            }

            if (yAxisCandleStick1.size() >= 6) {
                candleStickChart.setVisibleXRangeMaximum(6);
            }
            else
            {
                candleStickChart.invalidate();
            }
           candleStickChart.invalidate();
           candleStickChart.notifyDataSetChanged();
           candleStickChart.animateXY(1000,1000);
        }
       else {
           Log.d(TAG, "Data not found");
       }
    }

    // Candle stick chart date based.
    public void plotCandleStick1(List<BloodPressureDB> tasks) {
        yAxisCandle.clear();
        daysList.clear();
        candleStickTimeChart.clear();


        if(tasks.size() > 0 && tasks != null) {

            int count = 0;
            for (int i = 0; i < tasks.size(); i++){
                daysList.add(tasks.get(i).getDate());
                count++;
            }

//            for (int j = 0; j < systolicVal.size(); j++) {
                yAxisCandle.add(new CandleEntry(0,112,95,112,95));
            yAxisCandle.add(new CandleEntry(1,115,98,115,98));
            yAxisCandle.add(new CandleEntry(2,118,85,118,85));
            yAxisCandle.add(new CandleEntry(3,105,95,105,95));
            yAxisCandle.add(new CandleEntry(4,116,90,116,90));
            yAxisCandle.add(new CandleEntry(5,122,90,122,90));
//            count++;
//            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                newList = daysList.stream().distinct().collect(Collectors.toList());
            }

            Log.i(TAG, "plotCandleStick1: new list " + newList);

            Collections.sort(yAxisCandle,new EntryXComparator());

            CandleDataSet cds = new CandleDataSet(yAxisCandle, "");
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
            candleStickTimeChart.setData(cd);
            candleStickTimeChart.getDescription().setEnabled(false);

            // X axis
            XAxis xAxis = candleStickTimeChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(newList.size());
            xAxis.setValueFormatter(new IndexAxisValueFormatter(newList));
            xAxis.setAvoidFirstLastClipping(true);
//            xAxis.setLabelRotationAngle(-45);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(false);
            xAxis.setGranularity(1f);
            xAxis.setGranularityEnabled(true);
            xAxis.setCenterAxisLabels(false);
            xAxis.setEnabled(true);
            CustomMarkerView mv = new CustomMarkerView(Statistics.this, R.layout.marker_view);
//            mv.setChartView(candleStickChart);
            candleStickTimeChart.setMarkerView(mv);

            //Y axis
            YAxis yAxisRight = candleStickTimeChart.getAxisRight();
            yAxisRight.setEnabled(false);
            YAxis yAxisLeft = candleStickTimeChart.getAxisLeft();
            yAxisLeft.setLabelCount(6,true);
            yAxisLeft.setDrawAxisLine(false);
            yAxisLeft.setAxisMinimum(50);
            yAxisLeft.setAxisMaximum(200);

            if (yAxisCandle.size() > 1){
                Entry lastEntry = yAxisCandle.get(yAxisCandle.size()-1);
                Highlight highlight = new Highlight(lastEntry.getX(), lastEntry.getY(), 0);
                highlight.setDataIndex(0);
                candleStickTimeChart.highlightValue(highlight);
                candleStickTimeChart.moveViewToX(newList.size()-1);
            }
            else
            {
                Log.i(TAG, "No data found!!!");
            }

            if (yAxisCandle.size() >= 6) {
                candleStickTimeChart.setVisibleXRangeMaximum(6);
            }
            else {
                candleStickTimeChart.invalidate();
            }
            candleStickTimeChart.invalidate();
            candleStickTimeChart.notifyDataSetChanged();
            candleStickTimeChart.animateXY(1000,1000);
        }
        else {
            Log.d(TAG, "Data not found");
        }
    }


    private void average_of_dates(List<BloodPressureDB> list, List<String> dates) {

        int systa = 0;
        int diasta = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            newList = dates.stream().distinct().collect(Collectors.toList());
        }
        Log.d(TAG, "average_of_dates: " + newList);
        if (list != null && list.size() > 0) {
            for (int j = 0; j < list.size(); j++) {
//           for (int i = 0; i < newList.size(); i++) {
                if (list.get(j).getDate().equals("May 9")) {
                    Log.d(TAG, "average_of_dates: before cal systa" + list.get(j).getSystolic() + " diasta " + list.get(j).getDystolic() + " date " + list.get(j).getDate() + " size " + list.size());
                    systolicVal.add(list.get(j).getSystolic());
                    diastolicVal.add(list.get(j).getDystolic());
//                    computedSystaAndDiasta(systolicVal,diastolicVal);
                    Log.d(TAG, "average_of_dates: array of val " + systolicVal + " " + diastolicVal + " Size " + systolicVal.size() + " " + diastolicVal.size());

//                   for (int i = 0; i < systolicVal.size(); i++) {
//                       systa += list.get(j).getSystolic() / list.size();
//                       systolicVal.add(systa);
//                       Log.d(TAG, "average_of_dates: systolic " + systa + " Array " + systolicVal);
//                   }
//
//                   for (j = 0; j < diastolicVal.size(); j++) {
//                       diasta += list.get(j).getDystolic() / list.size();
//                       diastolicVal.add(diasta);
//                       Log.d(TAG, "average_of_dates: diastolic " + diasta + " Array " + diastolicVal);
//                   }

                }

//               if ("May 10".equals(list.get(j).getDate())) {
//                   Log.d(TAG, "average_of_dates: before cal systa" + list.get(j).getSystolic() + " diasta " + list.get(j).getDystolic() + " date " + list.get(j).getDate() + " size " + list.size());
//                   systolicVal.add(list.get(j).getSystolic());
//                   diastolicVal.add(list.get(j).getDystolic());
//                   Log.d(TAG, "average_of_dates: array of val " + systolicVal + " " + diastolicVal + " Size " + systolicVal.size() + " " + diastolicVal.size());
//
////                   for (i = 0; i < systolicVal.size(); i++) {
////                       systa += systolicVal.get(i) / systolicVal.size();
////                       systolicVal.add(systa);
////                       Log.d(TAG, "average_of_dates: systolic " + systa + " Array " + systolicVal);
////                   }
////
////                   for (j = 0; j < diastolicVal.size(); j++) {
////                       diasta += diastolicVal.get(j) / diastolicVal.size();
////                       diastolicVal.add(diasta);
////                       Log.d(TAG, "average_of_dates: diastolic " + diasta + " Array " + diastolicVal);
////                   }
//               }
//
//               if ("May 11".equals(list.get(j).getDate())) {
//                   Log.d(TAG, "average_of_dates: before cal systa" + list.get(j).getSystolic() + " diasta " + list.get(j).getDystolic() + " date " + list.get(j).getDate() + " size " + list.size());
//                   systolicVal.add(list.get(j).getSystolic());
//                   diastolicVal.add(list.get(j).getDystolic());
//                   Log.d(TAG, "average_of_dates: array of val " + systolicVal + " " + diastolicVal + " Size " + systolicVal.size() + " " + diastolicVal.size());
//
////                   for (i = 0; i < systolicVal.size(); i++) {
////                       systa += systolicVal.get(i) / systolicVal.size();
////                       systolicVal.add(systa);
////                       Log.d(TAG, "average_of_dates: systolic " + systa + " Array " + systolicVal);
////                   }
////
////                   for (j = 0; j < diastolicVal.size(); j++) {
////                       diasta += diastolicVal.get(j) / diastolicVal.size();
////                       diastolicVal.add(diasta);
////                       Log.d(TAG, "average_of_dates: diastolic " + diasta + " Array " + diastolicVal);
////                   }
//               }
//
//               if ("May 13".equals(list.get(j).getDate())) {
//                   Log.d(TAG, "average_of_dates: before cal systa" + list.get(j).getSystolic() + " diasta " + list.get(j).getDystolic() + " date " + list.get(j).getDate() + " size " + list.size());
//                   systolicVal.add(list.get(j).getSystolic());
//                   diastolicVal.add(list.get(j).getDystolic());
//                   Log.d(TAG, "average_of_dates: array of val " + systolicVal + " " + diastolicVal + " Size " + systolicVal.size() + " " + diastolicVal.size());
//
////                   for (i = 0; i < systolicVal.size(); i++) {
////                       systa += systolicVal.get(i) / systolicVal.size();
////                       systolicVal.add(systa);
////                       Log.d(TAG, "average_of_dates: systolic " + systa + " Array " + systolicVal);
////                   }
////
////                   for (j = 0; j < diastolicVal.size(); j++) {
////                       diasta += diastolicVal.get(j) / diastolicVal.size();
////                       diastolicVal.add(diasta);
////                       Log.d(TAG, "average_of_dates: diastolic " + diasta + " Array " + diastolicVal);
////                   }
//               }
//
//               if ("May 14".equals(list.get(j).getDate())) {
//                   Log.d(TAG, "average_of_dates: before cal systa" + list.get(j).getSystolic() + " diasta " + list.get(j).getDystolic() + " date " + list.get(j).getDate() + " size " + list.size());
//                   systolicVal.add(list.get(j).getSystolic());
//                   diastolicVal.add(list.get(j).getDystolic());
//                   Log.d(TAG, "average_of_dates: array of val " + systolicVal + " " + diastolicVal + " Size " + systolicVal.size() + " " + diastolicVal.size());
//
////                   for (i = 0; i < systolicVal.size(); i++) {
////                       systa += systolicVal.get(i) / systolicVal.size();
////                       systolicVal.add(systa);
////                       Log.d(TAG, "average_of_dates: systolic " + systa + " Array " + systolicVal);
////                   }
////
////                   for (j = 0; j < diastolicVal.size(); j++) {
////                       diasta += diastolicVal.get(j) / diastolicVal.size();
////                       diastolicVal.add(diasta);
////                       Log.d(TAG, "average_of_dates: diastolic " + diasta + " Array " + diastolicVal);
////                   }
//               }
//
//               if ("May 15".equals(list.get(j).getDate())) {
//                   Log.d(TAG, "average_of_dates: before cal systa" + list.get(j).getSystolic() + " diasta " + list.get(j).getDystolic() + " date " + list.get(j).getDate() + " size " + list.size());
//                   systolicVal.add(list.get(j).getSystolic());
//                   diastolicVal.add(list.get(j).getDystolic());
//                   Log.d(TAG, "average_of_dates: array of val " + systolicVal + " " + diastolicVal + " Size " + systolicVal.size() + " " + diastolicVal.size());
//
////                   for (i = 0; i < systolicVal.size(); i++) {
////                       systa += systolicVal.get(i) / systolicVal.size();
////                       systolicVal.add(systa);
////                       Log.d(TAG, "average_of_dates: systolic " + systa + " Array " + systolicVal);
////                   }
////
////                   for (j = 0; j < diastolicVal.size(); j++) {
////                       diasta += diastolicVal.get(j) / diastolicVal.size();
////                       diastolicVal.add(diasta);
////                       Log.d(TAG, "average_of_dates: diastolic " + diasta + " Array " + diastolicVal);
////                   }
//               }
//           }
            }
        }
//        return newList;
    }

    private void computedSystaAndDiasta(List<Integer> systolic, List<Integer> diastolic){
        int systa = 0;
        int diasta = 0;
        for (int i = 0; i <= systolic.size() ; i++) {
            systa += systolic.get(i);
            Log.d(TAG, "computedSystaAndDiasta: systa " + systa);
        }

        for (int j = 0; j < diastolic.size(); j++) {
            diasta += diastolic.get(j);
            Log.d(TAG, "computedSystaAndDiasta: diasta " + diasta);
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

