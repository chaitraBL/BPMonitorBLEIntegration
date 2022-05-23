package com.example.bpmonitorbleintegration;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Override
    public void onBindViewHolder(ReadingViewHolder holder, int position) {
        BloodPressureDB t = readingList.get(position);
//        holder.textViewAddress.setText(t.getName());
        holder.textViewDate.setText(t.getDate());
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

        TextView textViewAddress, textViewDate, textViewTime, textViewSysta, textViewDiasta, textViewRate, textViewRange;

        public ReadingViewHolder(View itemView) {
            super(itemView);

//            textViewAddress = itemView.findViewById(R.id.address);
            textViewDate = itemView.findViewById(R.id.date);
            textViewTime = itemView.findViewById(R.id.time1);
            textViewSysta = itemView.findViewById(R.id.systalic);
            textViewDiasta = itemView.findViewById(R.id.dystalic);
            textViewRate = itemView.findViewById(R.id.heartRate);
//            textViewRange = itemView.findViewById(R.id.map);

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
