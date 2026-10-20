package com.example.dailyanchor;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// HOME CONTENTS (THE CALENDAR PART)
public class HomeFragment extends Fragment {

    private MoodStore store;
    private View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        store = new MoodStore(requireContext());
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        String today = MoodStore.key(Calendar.getInstance());

        String firstName = store.getName().trim().split("\\s+")[0];
        ((TextView) root.findViewById(R.id.welcome)).setText(getString(R.string.welcome_back, firstName));

        buildDayStrip(today);

        // today's mood face + speech bubbles
        Mood mood = store.getMood(today);
        ((ImageView) root.findViewById(R.id.home_face)).setImageResource(mood != null ? mood.face : R.drawable.neutral);
        ((TextView) root.findViewById(R.id.bubble_mood)).setText(
                mood != null ? getString(R.string.i_feel, mood.label) : "No mood yet");
        View.OnClickListener openToday = v -> openDay(today);
        root.findViewById(R.id.home_face).setOnClickListener(openToday);
        root.findViewById(R.id.bubble_mood).setOnClickListener(openToday);
        root.findViewById(R.id.bubble_tell).setOnClickListener(openToday);

        SummaryBinder.bind(root.findViewById(R.id.summary), store);
    }

    // one chip per day of the current month, scrolled so today sits in the middle.
    private void buildDayStrip(String todayKey) {
        LinearLayout strip = root.findViewById(R.id.day_strip);
        HorizontalScrollView scroll = root.findViewById(R.id.day_scroll);
        strip.removeAllViews();

        Calendar c = Calendar.getInstance();
        int days = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        SimpleDateFormat monthFmt = new SimpleDateFormat("MMM", Locale.getDefault());
        View todayChip = null;

        for (int d = 1; d <= days; d++) {
            c.set(Calendar.DAY_OF_MONTH, d);
            String key = MoodStore.key(c);
            Mood m = store.getMood(key);
            boolean isToday = key.equals(todayKey);

            View chip = LayoutInflater.from(requireContext()).inflate(R.layout.item_day_chip, strip, false);
            ((ImageView) chip.findViewById(R.id.chip_bg)).setImageResource(m != null ? m.tile : R.drawable.calendar_extra);
            TextView label = chip.findViewById(R.id.chip_date);
            label.setText(monthFmt.format(c.getTime()).toUpperCase(Locale.getDefault()) + "\n" + d);
            label.setTypeface(null, isToday ? Typeface.BOLD : Typeface.NORMAL);
            chip.setOnClickListener(v -> openDay(key));
            strip.addView(chip);
            if (isToday) todayChip = chip;
        }

        final View target = todayChip;
        if (target != null) {
            scroll.post(() -> scroll.scrollTo(target.getLeft() - (scroll.getWidth() - target.getWidth()) / 2, 0));
        }
    }

    private void openDay(String key) {
        Intent i = new Intent(requireContext(), DayActivity.class);
        i.putExtra(DayActivity.EXTRA_DATE, key);
        startActivity(i);
    }
}
