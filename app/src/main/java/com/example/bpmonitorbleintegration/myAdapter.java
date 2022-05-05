package com.example.bpmonitorbleintegration;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class myAdapter extends FragmentPagerAdapter {

    private Context myContext;
    int totalTabs;

    public myAdapter(Context context, @NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        myContext = context;
        this.totalTabs = behavior;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                TimeWiseGraph timeWiseChart = new TimeWiseGraph();
                return timeWiseChart;
            case 1:
                DateWiseGraph dateWiseChart = new DateWiseGraph();
                return dateWiseChart;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return totalTabs;
    }
}
