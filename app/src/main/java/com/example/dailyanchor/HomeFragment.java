package com.example.dailyanchor;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
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
import java.util.List;
import java.util.Random;

// HOME CONTENTS (THE CALENDAR PART)
public class HomeFragment extends Fragment {

    private MoodStore store;
    private View root;
    private int period;
    private final Random random = new Random();
    private static final String[] QUOTES = {
            "Little steps still move you forward. 🌱",
            "You don't have to have it all figured out today.",
            "Your feelings matter. Give yourself a little grace.",
            "A fresh moment can begin whenever you need one.",
            "You've made it this far. That's worth celebrating!"
    };

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

        String firstName = DemoSession.name.trim().split("\\s+")[0];
        ((TextView) root.findViewById(R.id.welcome)).setText(getString(R.string.welcome_back, firstName));

        bindPeriodToggle();
        buildDayStrip(today);

        Mood mood = DemoSession.getMood(today, store);
        ((ImageView) root.findViewById(R.id.home_face)).setImageResource(mood != null ? mood.face : R.drawable.neutral);
        ((TextView) root.findViewById(R.id.bubble_mood)).setText(
                mood != null ? getString(R.string.i_feel, mood.label) : "No check-in yet!");
        View.OnClickListener openToday = v -> openDay(today);
        root.findViewById(R.id.home_face).setOnClickListener(openToday);
        root.findViewById(R.id.bubble_mood).setOnClickListener(openToday);
        root.findViewById(R.id.bubble_tell).setOnClickListener(openToday);

        SummaryBinder.bind(root.findViewById(R.id.summary), store);
        buildMemories();
        TextView quote = root.findViewById(R.id.quote_text);
        View.OnClickListener shuffle = v -> quote.setText("“" + QUOTES[random.nextInt(QUOTES.length)] + "”");
        shuffle.onClick(quote);
        quote.setOnClickListener(shuffle);
    }

    private void bindPeriodToggle() {
        TextView[] options = {
                root.findViewById(R.id.period_day),
                root.findViewById(R.id.period_week),
                root.findViewById(R.id.period_month)
        };
        for (int i = 0; i < options.length; i++) {
            final int value = i;
            TextView option = options[i];
            boolean selected = period == i;
            option.setBackgroundResource(selected ? R.drawable.bg_pill_olive : android.R.color.transparent);
            option.setTextColor(selected ? Color.WHITE : Color.DKGRAY);
            option.setTypeface(null, Typeface.BOLD);
            option.setOnClickListener(v -> {
                period = value;
                bindPeriodToggle();
                buildDayStrip(MoodStore.key(Calendar.getInstance()));
            });
        }
    }

    private void buildDayStrip(String todayKey) {
        LinearLayout strip = root.findViewById(R.id.day_strip);
        HorizontalScrollView scroll = root.findViewById(R.id.day_scroll);
        strip.removeAllViews();

        Calendar c = Calendar.getInstance();
        int days = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        SimpleDateFormat monthFmt = new SimpleDateFormat("MMM", Locale.getDefault());
        View todayChip = null;

        if (period == 0) {
            for (int d = 1; d <= days; d++) {
                c.set(Calendar.DAY_OF_MONTH, d);
                String key = MoodStore.key(c);
                boolean isToday = key.equals(todayKey);
                View chip = makeChip(strip, monthFmt.format(c.getTime()).toUpperCase(Locale.getDefault()) + "\n" + d,
                        DemoSession.getMood(key, store), isToday, v -> openDay(key));
                if (isToday) todayChip = chip;
            }
        } else if (period == 1) {
            c.set(Calendar.DAY_OF_MONTH, 1);
            int week = 1;
            for (int start = 1; start <= days; ) {
                c.set(Calendar.DAY_OF_MONTH, start);
                int end = Math.min(days, start + 7 - c.get(Calendar.DAY_OF_WEEK));
                String title = "WEEK " + week + "\n" + start + "–" + end;
                Mood common = commonMood(c, end - start + 1);
                boolean active = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) >= start
                        && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) <= end;
                int year = c.get(Calendar.YEAR);
                int monthIndex = c.get(Calendar.MONTH);
                View chip = makeChip(strip, title, common, active, v ->
                        ((MainActivity) requireActivity()).openCalendarMonth(year, monthIndex));
                if (active) todayChip = chip;
                start = end + 1;
                week++;
            }
        } else {
            Calendar now = Calendar.getInstance();
            for (int back = 5; back >= 0; back--) {
                Calendar month = (Calendar) now.clone();
                month.add(Calendar.MONTH, -back);
                month.set(Calendar.DAY_OF_MONTH, 1);
                int year = month.get(Calendar.YEAR);
                int monthIndex = month.get(Calendar.MONTH);
                Mood common = commonMood(month, month.getActualMaximum(Calendar.DAY_OF_MONTH));
                boolean active = back == 0;
                View chip = makeChip(strip, new SimpleDateFormat("MMM\nyyyy", Locale.getDefault()).format(month.getTime()),
                        common, active, v -> ((MainActivity) requireActivity()).openCalendarMonth(year, monthIndex));
                if (active) todayChip = chip;
            }
        }

        final View target = todayChip;
        if (target != null) {
            scroll.post(() -> scroll.scrollTo(target.getLeft() - (scroll.getWidth() - target.getWidth()) / 2, 0));
        }
    }

    private View makeChip(LinearLayout strip, String text, Mood mood, boolean active, View.OnClickListener click) {
        View chip = LayoutInflater.from(requireContext()).inflate(R.layout.item_day_chip, strip, false);
        ((ImageView) chip.findViewById(R.id.chip_bg)).setImageResource(
                mood != null ? mood.tile : R.drawable.calendar_extra);
        TextView label = chip.findViewById(R.id.chip_date);
        label.setText(text);
        label.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        chip.setOnClickListener(click);
        strip.addView(chip);
        return chip;
    }

    private Mood commonMood(Calendar first, int days) {
        int[] counts = DemoSession.moodCounts(first, days, store);
        int max = 0;
        Mood result = null;
        for (Mood mood : Mood.values()) {
            if (counts[mood.ordinal()] > max) {
                max = counts[mood.ordinal()];
                result = mood;
            }
        }
        return result;
    }

    private void buildMemories() {
        LinearLayout grid = root.findViewById(R.id.memories_grid);
        TextView empty = root.findViewById(R.id.memories_empty);
        grid.removeAllViews();
        List<String> dates = DemoSession.photoDays();
        empty.setVisibility(dates.isEmpty() ? View.VISIBLE : View.GONE);
        empty.setOnClickListener(v -> openDay(MoodStore.key(Calendar.getInstance())));
        int size = Math.round(122 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < dates.size(); i += 2) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rp.bottomMargin = Math.round(10 * getResources().getDisplayMetrics().density);
            grid.addView(row, rp);
            for (int j = i; j < Math.min(i + 2, dates.size()); j++) {
                String date = dates.get(j);
                DemoSession.Entry entry = DemoSession.getEntry(date);
                FrameLayout card = new FrameLayout(requireContext());
                card.setBackgroundResource(R.drawable.bg_card_white);
                card.setClipToOutline(true);
                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, size, 1f);
                cp.setMarginEnd(Math.round(8 * getResources().getDisplayMetrics().density));
                row.addView(card, cp);
                ImageView photo = new ImageView(requireContext());
                photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
                photo.setImageURI(Uri.parse(entry.photo));
                card.addView(photo, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                TextView label = new TextView(requireContext());
                label.setText(new SimpleDateFormat("MMM d", Locale.getDefault()).format(MoodStore.parse(date).getTime()));
                label.setTextColor(Color.WHITE);
                label.setBackgroundColor(0x99000000);
                label.setPadding(12, 6, 12, 6);
                card.addView(label, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, android.view.Gravity.BOTTOM));
                card.setOnClickListener(v -> openDay(date));
            }
            if (i + 1 == dates.size()) {
                View spacer = new View(requireContext());
                row.addView(spacer, new LinearLayout.LayoutParams(0, size, 1f));
            }
        }
    }

    private void openDay(String key) {
        Intent i = new Intent(requireContext(), DayActivity.class);
        i.putExtra(DayActivity.EXTRA_DATE, key);
        startActivity(i);
    }
}
