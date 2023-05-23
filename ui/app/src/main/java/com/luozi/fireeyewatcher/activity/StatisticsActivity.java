package com.luozi.fireeyewatcher.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.luozi.fireeyewatcher.R;
import com.luozi.fireeyewatcher.http.Common;
import com.luozi.fireeyewatcher.manager.AppManager;
import com.luozi.fireeyewatcher.model.Statistics;
import com.luozi.fireeyewatcher.model.Statistics.DailyFreq;
import com.luozi.fireeyewatcher.utils.DateUtil;
import com.luozi.fireeyewatcher.utils.ToastCustom;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class StatisticsActivity extends AppCompatActivity implements OnChartValueSelectedListener, View.OnClickListener {
    private Toolbar toolbar_chart;
    private PieChart pieChart;
    private BarChart barChart;
    private RelativeLayout ll_selection;
    private TextView tv_date_start;
    private ImageButton ib_search;
    private ThreadPoolExecutor executor;
    private CloseableHttpClient client;
    private Statistics statistics;
    private List<String> dates;
    private static final String LOG_TAG = "STATISTICS";

    @Override
    public void onClick(View view) {
        String startTimeStr = tv_date_start.getText().toString();
        try {
            long startTime = DateUtil.convertToTimeStamp(startTimeStr);
//            Log.d(LOG_TAG, String.format("%d", startTime));

            List<DailyFreq> dailyFreqs = statistics.dailyFreqs;
            int i = 0;
            for (; i < dailyFreqs.size(); i++) {
                if ((long) dailyFreqs.get(i).date * 1000 >= startTime) {
                    break;
                }
            }

            barChart.moveViewToX((float) i - 6.5f);
            barChart.invalidate();

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private class TimeSelectOnClickListener implements View.OnClickListener {

        private Context context;
        private TextView tv;

        TimeSelectOnClickListener(Context context, TextView tv) {
            this.context = context;
            this.tv = tv;
        }

        @Override
        public void onClick(View view) {
            String cur = tv.getText().toString();
            int curYear = Integer.parseInt(cur.substring(0, 4));
            int curMonth = Integer.parseInt(cur.substring(5, 7));
            int curDay = Integer.parseInt(cur.substring(8, 10));
            new DatePickerDialog(context, 0, (datePicker, year, monthOfYear, dayOfMonth) -> {
                try {
                    String text = DateUtil.convertToDate(year, monthOfYear, dayOfMonth);
                    tv.setText(text);
                } catch (ParseException e) {
                    Log.e(LOG_TAG, "convert date failed");
                    throw new RuntimeException(e);
                }
            }, curYear, curMonth, curDay)
                    .show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getInstance().addActivity(this);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_statistics);
        client = HttpClients.createDefault();
        executor = new ThreadPoolExecutor(1, 2, 1000, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

        toolbar_chart = findViewById(R.id.toolbar_chart);
        pieChart = findViewById(R.id.pie_chart);
        barChart = findViewById(R.id.bar_chart);
        ll_selection = findViewById(R.id.ll_selection);
        tv_date_start = findViewById(R.id.tv_date_start);
        ib_search = findViewById(R.id.ib_search);

        setSupportActionBar(toolbar_chart);
        toolbar_chart.setNavigationOnClickListener(view -> {
            AppManager.getInstance().finishActivity();
        });

        Future future =  executor.submit(() -> {
            getStatistcs();
            return null;
        });
        try {
            future.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (statistics == null) {
            ll_selection.setVisibility(View.INVISIBLE);
        } else {
            tv_date_start.setOnClickListener(new TimeSelectOnClickListener(this, tv_date_start));
            ib_search.setOnClickListener(this);

            List<DailyFreq> dailyFreqs = statistics.dailyFreqs;
            tv_date_start.setText(DateUtil.convertToDateNormal(dailyFreqs.get(dailyFreqs.size() - 1).date));
        }

        initPieChart();
        initBarChart();
    }

    private void getStatistcs() {
        HttpGet httpGet = new HttpGet(String.format("http://121.37.255.1:8080/api/v1/statistics?uid=%d", Common.loginUser.id));
        httpGet.setHeader("Authorization", Common.access_token);
        httpGet.setHeader("Accept", "*/*");

        String res = null;
        try {
            res = client.execute(httpGet, response -> {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return null;
                }

                InputStream inputStream = entity.getContent();
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String read;

                while ((read = bufferedReader.readLine()) != null) {
                    stringBuilder.append(read);
                }

                return stringBuilder.toString();
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (res == null || res.length() == 0) {
            Log.d(LOG_TAG, "empty response");
        }

        try {
            JSONObject jsonResponse = new JSONObject(res);

            int statusCode = jsonResponse.getInt("status_code");
            String desc = jsonResponse.getString("desc");
            // empty data
            if (desc.compareTo(String.format("user=%d has no record", Common.loginUser.id)) == 0) {
                Log.d(LOG_TAG, String.format("user=%d has no statistics info", Common.loginUser.id));
                return;
            }
            JSONObject data = jsonResponse.getJSONObject("data");

            Log.d(LOG_TAG, data.toString());

            if (statusCode >= Common.STATUS_SERVER_ERROR) {
                ToastCustom.custom(this, "远程服务器异常");
                throw new RuntimeException("remote server error");
            } else if (statusCode >= Common.STATUS_UNAUTHORIZED) {
                ToastCustom.custom(this, "会话已失效，请重新登录");
                AppManager.getInstance().finishOtherActivity((Activity) this);
                Intent intent = new Intent();
                intent.setClass(this, LoginActivity.class);
                startActivity(intent);
                AppManager.getInstance().finishActivity((Activity) this);
            } else if (statusCode >= Common.STATUS_REQUEST_ERROR) {
                ToastCustom.custom(this, "请求错误");
                throw new RuntimeException("get records request error");
            }

            statistics = Statistics.parseFromJson(data);
        } catch (RuntimeException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void initPieChart() {
        pieChart.setNoDataText("目前账户暂无使用记录");
        pieChart.setNoDataTextColor(getResources().getColor(R.color.gray, null));

        if (statistics == null) {
            Log.d(LOG_TAG, "empty statistics info");
            return;
        }

        pieChart.setUsePercentValues(true);
        pieChart.setCenterText(generateCenterSpannableText());
        pieChart.setExtraOffsets(5f, 5f, 5f, 5f);
        pieChart.setDrawHoleEnabled(true);

        // set hole
        pieChart.setHoleColor(getResources().getColor(R.color.white, null));
        pieChart.setHoleRadius(60f);

        // set transparent circle
        pieChart.setTransparentCircleColor(Color.YELLOW);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setTransparentCircleRadius(61f);

        pieChart.setDrawCenterText(true);

        // set rotation
        pieChart.setRotationAngle(20);
        pieChart.setRotationEnabled(true);

        pieChart.setOnChartValueSelectedListener(this);

        // set legend
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(8f);
        legend.setFormToTextSpace(4f);
        legend.setXEntrySpace(6f);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);

        setPieChartData();
        pieChart.animateY(1400, Easing.EaseInOutQuad);
        pieChart.invalidate();
    }

    private void setPieChartData() {
        double overheatRate = statistics.overheatRate;
        double normalRate = statistics.normalRate;
        double underheatRate = statistics.underheatRate;

        DecimalFormat df = new DecimalFormat("0.00%");

        // names
        List<String> xValues = new ArrayList<>(Arrays.asList("过热", "正常", "过低"));

        // overheat -> normal -> underheat
        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry((float) overheatRate, xValues.get(0)));
        pieEntries.add(new PieEntry((float) normalRate, xValues.get(1)));
        pieEntries.add(new PieEntry((float) underheatRate, xValues.get(2)));

        // rates
        List<String> yValues = new ArrayList<>();
        yValues.add(df.format(overheatRate));
        yValues.add(df.format(normalRate));
        yValues.add(df.format(underheatRate));

        // set dataset
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "过热度识别情况");
        pieDataSet.setSliceSpace(3f);
        pieDataSet.setSelectionShift(5f);
        pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        pieDataSet.setColors(colors);
        pieDataSet.setValueLinePart1OffsetPercentage(80.f);
        pieDataSet.setValueLinePart1Length(0.3f);
        pieDataSet.setValueLinePart2Length(0.4f);
        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(Color.BLACK);
        pieData.setValueFormatter(new PercentFormatter(pieChart));

        // set description
        Description description = pieChart.getDescription();
        description.setEnabled(false);

        pieChart.setData(pieData);
        pieChart.highlightValues(null);
    }

    private void initBarChart() {
        barChart.setNoDataText("目前账户暂无使用记录");
        barChart.setNoDataTextColor(getResources().getColor(R.color.gray, null));

        if (statistics == null) {
            Log.d(LOG_TAG, "empty statistics info");
            return;
        }

        List<DailyFreq> dailyFreqs = statistics.dailyFreqs;
        if (dailyFreqs == null || dailyFreqs.size() == 0) {
            Log.d(LOG_TAG, "empty dailyFreqs");
            return;
        }

        dates = new ArrayList<>();
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < dailyFreqs.size(); i++) {
            dates.add(DateUtil.convertToDate(dailyFreqs.get(i).date));
            entries.add(new BarEntry(i, dailyFreqs.get(i).freq));
        }

        // set BarDataSet
        BarDataSet barDataSet = new BarDataSet(entries, "");
        barDataSet.setColor(getResources().getColor(R.color.bar_chart_red, null));
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value == 0 ? "" : String.valueOf((int) value);
            }
        });
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.3f);
        barChart.setData(barData);

        // set XAxis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value < dates.size() ? dates.get((int) value) : "";
            }
        });

        // set YAxis
        YAxis axisLeft = barChart.getAxisLeft();
        YAxis axisRight = barChart.getAxisRight();
        axisRight.setEnabled(false);
        axisLeft.setGranularity(1f);
        axisLeft.setAxisMinimum(0f);

        int maxFreq = dailyFreqs.get(0).freq;
        for (DailyFreq dailyFreq : dailyFreqs) {
            if (dailyFreq.freq > maxFreq) {
                maxFreq = dailyFreq.freq;
            }
        }
        if (maxFreq <= 5) {
            axisLeft.setAxisMaximum(5f);
        } else if (maxFreq <= 10) {
            axisLeft.setAxisMaximum(10f);
        } else {
            axisLeft.setAxisMaximum(maxFreq);
        }

        axisLeft.setLabelCount(5);
        axisLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        // set Legend
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);

        // set description
        Description description = new Description();
        description.setEnabled(false);
        barChart.setDescription(description);

        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setTouchEnabled(true);
        barChart.setDoubleTapToZoomEnabled(true);
        barChart.setVisibleXRange(0f, 7f);
        barChart.moveViewToX(dates.size() - 7.5f);

        barChart.animateY(1400, Easing.EaseInOutQuad);
        barChart.invalidate();
    }

    private SpannableString generateCenterSpannableText() {
        SpannableString s = new SpannableString("FireEye Watcher\ndeveloped by luozi");
        s.setSpan(new RelativeSizeSpan(1.7f), 0, 15, 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), 15, s.length() - 6, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 15, s.length() - 6, 0);
        s.setSpan(new RelativeSizeSpan(.8f), 15, s.length() - 6, 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 6, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 6, s.length(), 0);
        return s;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}