package com.luozi.fireeyewatcher.adapter;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.luozi.fireeyewatcher.R;
import com.luozi.fireeyewatcher.model.SettingOption;

import java.util.List;

public class SettingListAdapter extends BaseAdapter {

    private Context context;
    private List<SettingOption> functionList;

    public SettingListAdapter(Context context, List<SettingOption> functionList) {
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
        public ImageView iv_function;
        public TextView tv_function;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.item_user, viewGroup, false);
            holder.iv_function = view.findViewById(R.id.iv_function);
            holder.tv_function = view.findViewById(R.id.tv_function);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        SettingOption option = functionList.get(i);
        holder.iv_function.setImageResource(option.drawable);
        holder.tv_function.setText(option.text);

        return view;
    }
}
