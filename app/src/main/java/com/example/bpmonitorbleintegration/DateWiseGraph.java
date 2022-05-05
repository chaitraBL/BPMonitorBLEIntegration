package com.example.bpmonitorbleintegration;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DateWiseGraph#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DateWiseGraph extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    LineChart lineChart;
    LineData lineData;
    List<Entry> entryList1 = new ArrayList<Entry>();
    List<Entry> entryList2 = new ArrayList<Entry>();
    List<Entry> entryList3 = new ArrayList<Entry>();
    ArrayList<String> xAxisValues;
    String xVal;
    LineDataSet lineDataSet1,lineDataSet2, lineDataSet3;
    ArrayList<String> systolicVal;
    String TAG = DateWiseGraph.class.getName();
    ScatterChart scatterChart;
    ScatterData scatterData;
    ScatterDataSet scatterDataSet1, scatterDataSet2, scatterDataSet3;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DateWiseGraph() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DateWiseGraph.
     */
    // TODO: Rename and change types and number of parameters
    public static DateWiseGraph newInstance(String param1, String param2) {
        DateWiseGraph fragment = new DateWiseGraph();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        lineChart = lineChart.findViewById(R.id.linechart_date);
        scatterChart = scatterChart.findViewById(R.id.scatterChart_date);
        systolicVal = new ArrayList<>();
        xAxisValues = new ArrayList<>();
        getManualTasks();
        return inflater.inflate(R.layout.fragment_date_wise_graph, container, false);
    }

    //To retrieve data from Room DB.
    private void getManualTasks() {
        class GetTasks extends AsyncTask<Void, Void, List<BloodPressureDB>> {

            @Override
            protected List<BloodPressureDB> doInBackground(Void... voids) {
                List<BloodPressureDB> taskList = DatabaseClient
                        .getInstance(getContext())
                        .getAppDatabase()
                        .bpReadingsDao()
                        .getAll();
                return taskList;
            }

            @Override
            protected void onPostExecute(List<BloodPressureDB> tasks) {
                super.onPostExecute(tasks);
                plotLineGraph(tasks);
                plotScatterGraph(tasks);

            }
        }
        GetTasks gt = new GetTasks();
        gt.execute();
    }

    // Single Line Graph.
//    public void plotLineGraph(List<BloodPressureDB> task){
//        Long tsLong = System.currentTimeMillis()/1000;
//        for (BloodPressureDB list : task) {
//            Log.i(TAG, "value in loop " + list.getSystolic());
////            Log.i(TAG, "tsLong in loop " + tsLong);
//            entryList.add(new Entry(Integer.valueOf(list.getDate()),list.getSystolic()));
//            xAxisValues.add(list.getDate());
//        }
//        Log.i(TAG, "in entry list " + entryList.toString());
//
////        xAxisValues = new ArrayList<>(Arrays.asList(date));
//
//        lineDataSet1 = new LineDataSet(entryList, "Systolic");
//        lineDataSet1.setColors(ColorTemplate.JOYFUL_COLORS);
////        lineDataSet.setValueTextColor(Color.BLACK);
//        lineDataSet1.setValueTextSize(16f);
//        lineDataSet1.setLineWidth(2);
//        lineData = new LineData(lineDataSet1);
//
//        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
////        xAxis.setValueFormatter(new IAxisValueFormatter() {
////            @Override
////            public String getFormattedValue(float value, AxisBase axis) {
////                return xAxisValues.get((int)value);
//////                return String.valueOf(xAxisValues);
////            }
////
////        });
//
//        lineChart.setData(lineData);
//        lineChart.invalidate();
//    }

    // Multiple dataset line graph.
    public void plotLineGraph(List<BloodPressureDB> task) {
        lineData = lineChart.getData();

        for (BloodPressureDB list : task) {
            entryList1.add(new Entry(0,list.getSystolic()));
            entryList2.add(new Entry(1,list.getDystolic()));
            entryList3.add(new Entry(2,list.getHeartRate()));
            xAxisValues.add(list.getDate());
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

        lineDataSet3 = new LineDataSet(entryList3,"Heart Rate");
        lineDataSet3.setColor(Color.BLUE);
        lineDataSet3.setValueTextColor(Color.BLACK);
        lineDataSet3.setValueTextSize(12f);
        lineDataSet3.setLineWidth(2);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet1);
        dataSets.add(lineDataSet2);
        dataSets.add(lineDataSet3);

        lineData = new LineData(dataSets);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xAxisValues.get((int) value);
            }
        });
        lineChart.setData(lineData);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    // Multiple datasets scatter chart.
    public void plotScatterGraph(List<BloodPressureDB> task) {
        scatterData = scatterChart.getData();
        for (BloodPressureDB list : task) {
            entryList1.add(new Entry(0,list.getSystolic()));
            entryList2.add(new Entry(1,list.getDystolic()));
            entryList3.add(new Entry(2,list.getHeartRate()));
            xAxisValues.add(list.getDate());
        }
        scatterDataSet1 = new ScatterDataSet(entryList1,"Systolic");
        scatterDataSet1.setColor(Color.MAGENTA);
        scatterDataSet1.setValueTextColor(Color.BLACK);
        scatterDataSet1.setValueTextSize(12f);

        scatterDataSet2 = new ScatterDataSet(entryList2,"Diastolic");
        scatterDataSet2.setColor(Color.RED);
        scatterDataSet2.setValueTextColor(Color.BLACK);
        scatterDataSet2.setValueTextSize(12f);

        scatterDataSet3 = new ScatterDataSet(entryList3,"Heart Rate");
        scatterDataSet3.setColor(Color.BLUE);
        scatterDataSet3.setValueTextColor(Color.BLACK);
        scatterDataSet3.setValueTextSize(12f);

        ArrayList<IScatterDataSet> dataSets = new ArrayList<>();
        dataSets.add(scatterDataSet1);
        dataSets.add(scatterDataSet2);
        dataSets.add(scatterDataSet3);

        scatterData = new ScatterData(dataSets);

        XAxis xAxis = scatterChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xAxisValues.get((int) value);
            }
        });
        scatterChart.setData(scatterData);
        scatterChart.notifyDataSetChanged();
        scatterChart.invalidate();

    }
}