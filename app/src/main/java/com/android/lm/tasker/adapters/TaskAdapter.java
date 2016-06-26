package com.android.lm.tasker.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.lm.tasker.R;
import com.android.lm.tasker.utils.Task;

import java.util.ArrayList;

/**
 * Created by Lara on 6/22/2016.
 */
public class TaskAdapter extends ArrayAdapter<Task> {
    private Context mContext;
    private int mLayoutResourceId;
    private ArrayList<Task> mData;

    public TaskAdapter(Context context, int layoutResourceId, ArrayList<Task> data) {
        super(context, layoutResourceId, data);
        this.mLayoutResourceId = layoutResourceId;
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public View getView(final int position, View row, ViewGroup parent) {
        TaskHolder holder = null;

        if(row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);

            row.setFocusable(false);
            row.setClickable(false);

            CheckBox tmp = (CheckBox) row.findViewById(R.id.cbTaskStatus);
            tmp.setFocusable(false);

            holder = new TaskHolder();
            holder.cbStatus = (CheckBox) row.findViewById(R.id.cbTaskStatus);
            holder.tvName = (TextView) row.findViewById(R.id.tvName);
            holder.tvDate = (TextView) row.findViewById(R.id.tvDate);
            holder.ivPriority = (ImageView) row.findViewById(R.id.ivPriorityStar);

            row.setTag(holder);
        } else {
            holder = (TaskHolder) row.getTag();
        }


        Task task = mData.get(position);
        holder.tvName.setText(task.getName());

        holder.cbStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Need to update database at this point ...
                mData.get(position).setIsChecked(isChecked);
            }
        });

        holder.cbStatus.setChecked(task.getIsChecked());

        holder.tvDate.setText(task.getDate());
        if(task.getDate().isEmpty()) {
            holder.tvDate.setVisibility(View.GONE);
        } else {
            holder.tvDate.setVisibility(View.VISIBLE);
        }
        if(task.getPriority()) {
            holder.ivPriority.setImageResource(R.drawable.star_priority_multicolor);
        } else {
            holder.ivPriority.setImageResource(0);
        }

        return row;
    }

    public Task getItem(int position) {
        return mData.get(position);
    }

    static class TaskHolder {
        CheckBox cbStatus;
        TextView tvName;
        TextView tvNotes;
        boolean priority;
        TextView tvDate;
        ImageView ivPriority;
    }
}
