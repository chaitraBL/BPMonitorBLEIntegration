package com.example.bpmonitorbleintegration;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class CustomMarkerView extends MarkerView {
    private TextView markerText;
    List<String> mXLabels;

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */

    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        markerText = findViewById(R.id.tvContent);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        super.refreshContent(e, highlight);
        if (e instanceof CandleEntry) {
            CandleEntry ce = (CandleEntry) e;
            markerText.setText("Systolic: " + Utils.formatNumber(ce.getHigh(), 0, true) + " Diastolic: " + Utils.formatNumber(ce.getLow(),0,true));
        }
        else{
            markerText.setText("" + Utils.formatNumber(e.getY(),0,true));
        }

    }

    @Override
    public float getX() {
        return -(getWidth());
    }

    @Override
    public float getY() {
        return -getHeight();
    }
}
