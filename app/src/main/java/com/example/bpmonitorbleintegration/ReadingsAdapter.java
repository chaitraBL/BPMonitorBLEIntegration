package com.example.bpmonitorbleintegration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReadingsAdapter extends RecyclerView.Adapter<ReadingsAdapter.ReadingViewHolder> {
    private Context mCtx;
    private List<BloodPressureDB> readingList;

    public ReadingsAdapter(Context mCtx, List<BloodPressureDB> taskList) {
        this.mCtx = mCtx;
        this.readingList = taskList;
    }

    @Override
    public ReadingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.recyclerview_tasks, parent, false);
        return new ReadingViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ReadingViewHolder holder, int position) {
        BloodPressureDB t = readingList.get(position);
//        holder.textViewAddress.setText(t.getName());
        String date = t.getDate();
        String[] showDate = date.split("-");

        if (showDate[1].equalsIgnoreCase("01")) {
            holder.textViewDate.setText(showDate[0]+"-"+"JAN");
        }else if(showDate[1].equalsIgnoreCase("02")){
            holder.textViewDate.setText(showDate[0]+"-"+"FEB");
        }else if(showDate[1].equalsIgnoreCase("03")){
            holder.textViewDate.setText(showDate[0]+"-"+"MAR");
        }else if(showDate[1].equalsIgnoreCase("04")){
            holder.textViewDate.setText(showDate[0]+"-"+"APR");
        }else if(showDate[1].equalsIgnoreCase("05")){
            holder.textViewDate.setText(showDate[0]+"-"+"MAY");
        }else if(showDate[1].equalsIgnoreCase("06")){
            holder.textViewDate.setText(showDate[0]+"-"+"JUN");
        }else if(showDate[1].equalsIgnoreCase("07")){
            holder.textViewDate.setText(showDate[0]+"-"+"JLY");
        }else if(showDate[1].equalsIgnoreCase("08")){
            holder.textViewDate.setText(showDate[0]+"-"+"AUG");
        }else if(showDate[1].equalsIgnoreCase("09")){
            holder.textViewDate.setText(showDate[0]+"-"+"SEP");
        }else if(showDate[1].equalsIgnoreCase("10")){
            holder.textViewDate.setText(showDate[0]+"-"+"OCT");
        }else if(showDate[1].equalsIgnoreCase("11")){
            holder.textViewDate.setText(showDate[0]+"-"+"NOV");
        }else if(showDate[1].equalsIgnoreCase("12")){
            holder.textViewDate.setText(showDate[0]+"-"+"DEC");
        }
        holder.textViewTime.setText(t.getTime());
        holder.textViewSysta.setText(String.valueOf(t.getSystolic()));
        holder.textViewDiasta.setText(String.valueOf(t.getDystolic()));
        holder.textViewRate.setText(String.valueOf(t.getHeartRate()));
//        holder.textViewRange.setText(String.valueOf(t.getRange()));
    }

    @Override
    public int getItemCount() {
        return readingList.size();
    }
    public class ReadingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Button textViewDate, textViewTime, textViewSysta, textViewDiasta, textViewRate, textViewRange;

        public ReadingViewHolder(View itemView) {
            super(itemView);

            textViewDate = itemView.findViewById(R.id.date);
            textViewTime = itemView.findViewById(R.id.time1);
            textViewSysta = itemView.findViewById(R.id.systalic);
            textViewDiasta = itemView.findViewById(R.id.dystalic);
            textViewRate = itemView.findViewById(R.id.heartRate);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            BloodPressureDB task = readingList.get(getAdapterPosition());



//            Intent intent = new Intent(mCtx, UpdateTaskActivity.class);
//            intent.putExtra("task", task);
//
//            mCtx.startActivity(intent);
        }
    }
}
