package com.example.dailyanchor;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// CALENDAR
public class CalendarFragment extends Fragment {

    private static final String[] WEEKDAYS = {"S", "M", "T", "W", "H", "F", "S"};

    private MoodStore store;
    private View root;
    private final Calendar displayedMonth = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_calendar, container, false);
        store = new MoodStore(requireContext());
        if (getArguments() != null) {
            displayedMonth.set(Calendar.YEAR, getArguments().getInt("year", displayedMonth.get(Calendar.YEAR)));
            displayedMonth.set(Calendar.MONTH, getArguments().getInt("month", displayedMonth.get(Calendar.MONTH)));
        }
        root.findViewById(R.id.month_previous).setOnClickListener(v -> moveMonth(-1));
        root.findViewById(R.id.month_next).setOnClickListener(v -> moveMonth(1));
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        build();
    }

    private int dp(int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }

    private void build() {
        Calendar month = (Calendar) displayedMonth.clone();
        ((TextView) root.findViewById(R.id.month_title)).setText(
                new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(month.getTime()));

        // weekday header
        LinearLayout header = root.findViewById(R.id.weekday_row);
        header.removeAllViews();
        for (String w : WEEKDAYS) {
            TextView t = new TextView(requireContext());
            t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            t.setGravity(Gravity.CENTER);
            t.setText(w);
            t.setTextSize(12);
            t.setTypeface(null, Typeface.BOLD);
            t.setTextColor(getResources().getColor(R.color.text_dark, null));
            header.addView(t);
        }

        // day grid
        LinearLayout grid = root.findViewById(R.id.calendar_grid);
        grid.removeAllViews();
        grid.setClickable(true);
        attachSwipe(grid);

        String todayKey = MoodStore.key(Calendar.getInstance());
        Calendar first = (Calendar) month.clone();
        first.set(Calendar.DAY_OF_MONTH, 1);
        int offset = first.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
        int days = month.getActualMaximum(Calendar.DAY_OF_MONTH);
        int cells = ((offset + days + 6) / 7) * 7;

        LinearLayout row = null;
        for (int i = 0; i < cells; i++) {
            if (i % 7 == 0) {
                row = new LinearLayout(requireContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setWeightSum(7);
                LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rp.topMargin = dp(6);
                grid.addView(row, rp);
            }
            int day = i - offset + 1;
            FrameLayout cell = new FrameLayout(requireContext());
            cell.setLayoutParams(new LinearLayout.LayoutParams(0, dp(44), 1f));
            if (day >= 1 && day <= days) {
                month.set(Calendar.DAY_OF_MONTH, day);
                cell.addView(makeTile(day, MoodStore.key(month), todayKey));
            }
            row.addView(cell);
        }

        SummaryBinder.bind(root.findViewById(R.id.summary), store);
        ChartCarouselBinder.bind(root.findViewById(R.id.charts), store);
    }

    private View makeTile(int day, String key, String todayKey) {
        Mood m = DemoSession.getMood(key, store);
        FrameLayout tile = new FrameLayout(requireContext());
        tile.setLayoutParams(new FrameLayout.LayoutParams(dp(40), dp(44), Gravity.CENTER));

        ImageView bg = new ImageView(requireContext());
        bg.setScaleType(ImageView.ScaleType.FIT_XY);
        bg.setImageResource(m != null ? m.tile : R.drawable.calendar_extra);
        tile.addView(bg, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView num = new TextView(requireContext());
        num.setText(String.valueOf(day));
        num.setTextSize(8);
        num.setTextColor(getResources().getColor(R.color.text_dark, null));
        num.setTypeface(null, key.equals(todayKey) ? Typeface.BOLD : Typeface.NORMAL);
        FrameLayout.LayoutParams np = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        np.topMargin = dp(3);
        tile.addView(num, np);

        tile.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), DayActivity.class);
            i.putExtra(DayActivity.EXTRA_DATE, key);
            startActivity(i);
        });
        attachSwipe(tile);
        return tile;
    }

    private void moveMonth(int delta) {
        displayedMonth.set(Calendar.DAY_OF_MONTH, 1);
        displayedMonth.add(Calendar.MONTH, delta);
        build();
    }

    private void attachSwipe(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private float downX;
            private float downY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    downX = event.getRawX();
                    downY = event.getRawY();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    float dx = event.getRawX() - downX;
                    float dy = event.getRawY() - downY;
                    if (Math.abs(dx) > dp(60) && Math.abs(dx) > Math.abs(dy)) {
                        moveMonth(dx > 0 ? -1 : 1);
                        return true;
                    }
                }
                return false;
            }
        });
    }
}
