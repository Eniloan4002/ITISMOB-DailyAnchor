package com.example.dailyanchor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;

public final class ChartCarouselBinder {

    private ChartCarouselBinder() {
    }

    public static void bind(View root, MoodStore store) {
        ViewPager2 pager = root.findViewById(R.id.chart_pager);
        TextView position = root.findViewById(R.id.chart_position);
        if (pager.getAdapter() != null) {
            pager.setAdapter(new ChartAdapter(store));
            position.setText("1 of 3");
            return;
        }
        pager.setAdapter(new ChartAdapter(store));
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int page) {
                position.setText((page + 1) + " of 3");
            }
        });
        root.findViewById(R.id.chart_previous).setOnClickListener(v ->
                pager.setCurrentItem(Math.max(0, pager.getCurrentItem() - 1), true));
        root.findViewById(R.id.chart_next).setOnClickListener(v ->
                pager.setCurrentItem(Math.min(2, pager.getCurrentItem() + 1), true));
    }

    private static final class ChartAdapter extends RecyclerView.Adapter<ChartAdapter.ChartHolder> {

        private final MoodStore store;

        ChartAdapter(MoodStore store) {
            this.store = store;
        }

        @NonNull
        @Override
        public ChartHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chart_page, parent, false);
            return new ChartHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChartHolder holder, int position) {
            Context context = holder.itemView.getContext();
            holder.content.removeAllViews();
            Calendar week = Calendar.getInstance();
            week.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            int[] weekly = DemoSession.moodCounts(week, 7, store);
            if (position == 0) {
                holder.title.setText("Your mood mix");
                holder.message.setText(sum(weekly) == 0
                        ? "Your mood story starts with one check-in."
                        : "You've felt " + strongest(weekly).label.toLowerCase() + " most often this week.");
            } else if (position == 1) {
                holder.title.setText("Your check-in rhythm");
                Calendar day = (Calendar) week.clone();
                int filled = 0;
                for (int i = 0; i < 7; i++) {
                    int count = DemoSession.getMood(MoodStore.key(day), store) == null ? 0 : 1;
                    filled += count;
                    day.add(Calendar.DAY_OF_MONTH, 1);
                }
                holder.message.setText("You've checked in " + filled + " out of 7 days this week. Every moment counts!");
            } else {
                holder.title.setText("This month's mood moments");
                Calendar month = Calendar.getInstance();
                month.set(Calendar.DAY_OF_MONTH, 1);
                int[] counts = DemoSession.moodCounts(month, month.getActualMaximum(Calendar.DAY_OF_MONTH), store);
                ArrayList<BarEntry> values = new ArrayList<>();
                ArrayList<Integer> colors = new ArrayList<>();
                for (Mood mood : Mood.values()) {
                    values.add(new BarEntry(mood.ordinal(), counts[mood.ordinal()]));
                    colors.add(ContextCompat.getColor(context, mood.color));
                }
                BarDataSet set = new BarDataSet(values, "");
                set.setColors(colors);
                set.setDrawValues(false);
                HorizontalBarChart chart = new HorizontalBarChart(context);
                chart.setData(new BarData(set));
                styleBar(chart, new String[]{"Angry", "Sad", "Neutral", "Happy", "Anxious"}, Math.max(2, max(counts) + 1));
                addChart(holder, chart);
                holder.message.setText(sum(counts) == 0
                        ? "Your month is waiting for its first mood."
                        : "You've logged " + sum(counts) + " mood moments this month.");
            }
        }

        private void addChart(ChartHolder holder, View chart) {
            holder.content.addView(chart, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        private void styleBar(BarChart chart, String[] labels, float maximum) {
            chart.getDescription().setEnabled(false);
            chart.getLegend().setEnabled(false);
            chart.getAxisRight().setEnabled(false);
            chart.getAxisLeft().setAxisMinimum(0f);
            chart.getAxisLeft().setAxisMaximum(maximum);
            chart.getAxisLeft().setDrawGridLines(false);
            chart.getAxisLeft().setTextSize(9f);
            chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            chart.getXAxis().setGranularity(1f);
            chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            chart.getXAxis().setDrawGridLines(false);
            chart.getXAxis().setTextSize(10f);
            chart.setTouchEnabled(false);
            chart.setExtraOffsets(4, 4, 10, 8);
            chart.invalidate();
        }

        private int sum(int[] values) {
            int result = 0;
            for (int value : values) result += value;
            return result;
        }

        private int max(int[] values) {
            int result = 0;
            for (int value : values) result = Math.max(result, value);
            return result;
        }

        private Mood strongest(int[] counts) {
            Mood strongest = Mood.NEUTRAL;
            for (Mood mood : Mood.values()) {
                if (counts[mood.ordinal()] > counts[strongest.ordinal()]) strongest = mood;
            }
            return strongest;
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        static final class ChartHolder extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView message;
            final FrameLayout content;

            ChartHolder(View view) {
                super(view);
                title = view.findViewById(R.id.chart_title);
                message = view.findViewById(R.id.chart_message);
                content = view.findViewById(R.id.chart_content);
            }
        }
    }
}
