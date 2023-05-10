package com.luozi.fireeyewatcher.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.luozi.fireeyewatcher.R;
import com.luozi.fireeyewatcher.model.Record;

import java.util.List;

public class RecordAdapter extends BaseAdapter {

    private Context context;
    private List<Record> recordList;

    public RecordAdapter(Context context, List<Record> recordList) {
        this.context = context;
        this.recordList = recordList;
    }

    @Override
    public int getCount() {
        return recordList.size();
    }

    @Override
    public Object getItem(int i) {
        return recordList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public static final class ViewHolder {
        public TextView tv_record_title;
        public TextView tv_request_time;
        public TextView tv_finish_time;
        public TextView tv_result;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.item_record, viewGroup, false);
            holder.tv_record_title = view.findViewById(R.id.tv_record_title);
            holder.tv_request_time = view.findViewById(R.id.tv_request_time);
            holder.tv_finish_time = view.findViewById(R.id.tv_finish_time);
            holder.tv_result = view.findViewById(R.id.tv_result);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Record record = recordList.get(i);
        holder.tv_record_title.setText(String.format("记录(ID:%d)", record.id));
        holder.tv_request_time.setText(record.requestTime);
        holder.tv_finish_time.setText(record.finishedTime);

        switch (record.result) {
            case -1:
                holder.tv_result.setText("识别进行中...");
                holder.tv_result.setTextColor(context.getColor(R.color.gray));
                break;
            case 0:
                holder.tv_result.setText("过低");
                holder.tv_result.setTextColor(context.getColor(R.color.red));
                break;
            case 1:
                holder.tv_result.setText("正常");
                holder.tv_result.setTextColor(context.getColor(R.color.gray));
                break;
            case 2:
                holder.tv_result.setText("过热");
                holder.tv_result.setTextColor(context.getColor(R.color.red));
                break;
        }

        return view;
    }
}
