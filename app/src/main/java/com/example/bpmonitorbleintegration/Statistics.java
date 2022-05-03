package com.example.bpmonitorbleintegration;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class Statistics extends AppCompatActivity {

    LineChart lineChart;
    LineData lineData;
    List<Entry> entryList = new ArrayList<Entry>();
    ArrayList<String> xAxisValues;
    LineDataSet lineDataSet;
    ArrayList<String> systolicVal;
    String TAG = Statistics.class.getName();
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        lineChart = findViewById(R.id.linechart);
        systolicVal = new ArrayList<>();
        xAxisValues = new ArrayList<>();

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
                plotLineGraph(tasks);

            }
        }
        GetTasks gt = new GetTasks();
        gt.execute();
    }

    public void plotLineGraph(List<BloodPressureDB> task){
        Long tsLong = System.currentTimeMillis()/1000;
        for (BloodPressureDB list : task) {
            Log.i(TAG, "value in loop " + list.getSystolic());
//            Log.i(TAG, "tsLong in loop " + tsLong);
            entryList.add(new Entry(Integer.valueOf(list.getDate()),list.getSystolic()));
            xAxisValues.add(list.getDate());
        }
        Log.i(TAG, "in entry list " + entryList.toString());

//        xAxisValues = new ArrayList<>(Arrays.asList(date));

        lineDataSet = new LineDataSet(entryList, "Systolic");
        lineDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
//        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setValueTextSize(16f);
        lineDataSet.setLineWidth(2);
        lineData = new LineData(lineDataSet);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return xAxisValues.get((int)value);
////                return String.valueOf(xAxisValues);
//            }
//
//        });

        lineChart.setData(lineData);
        lineChart.invalidate();
    }

}