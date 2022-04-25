package com.example.bpmonitorbleintegration;

import android.content.Context;
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
        holder.textViewTask.setText(t.getName());
        holder.textViewDesc.setText(t.getDate());
//        holder.textViewFinishBy.setText(t.getFinishBy());

//        if (t.isFinished())
//            holder.textViewStatus.setText("Completed");
//        else
//            holder.textViewStatus.setText("Not Completed");
    }

    @Override
    public int getItemCount() {
        return readingList.size();
    }
    public class ReadingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textViewStatus, textViewTask, textViewDesc;

        public ReadingViewHolder(View itemView) {
            super(itemView);

//                   textViewStatus = itemView.findViewById(R.id.textViewStatus);
//            textViewTask = itemView.findViewById(R.id.textViewMessage);
//            textViewDesc = itemView.findViewById(R.id.textViewAddress);
//            textViewFinishBy = itemView.findViewById(R.id.textViewFinishBy);


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
