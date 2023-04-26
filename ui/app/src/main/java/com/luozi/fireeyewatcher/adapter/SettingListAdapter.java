package com.luozi.fireeyewatcher.adapter;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.luozi.fireeyewatcher.R;

import java.util.List;

public class SettingListAdapter extends BaseAdapter {

    private Context context;
    private List<String> functionList;

    public SettingListAdapter(Context context, List<String> functionList) {
        this.context = context;
        this.functionList = functionList;
    }

    @Override
    public int getCount() {
        return functionList.size();
    }

    @Override
    public Object getItem(int i) {
        return functionList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public static final class ViewHolder {
        public TextView tv_function;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.item_user, viewGroup, false);
            holder.tv_function = view.findViewById(R.id.tv_function);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        String functionName = functionList.get(i);
        holder.tv_function.setText(functionName);

        return view;
    }
}
