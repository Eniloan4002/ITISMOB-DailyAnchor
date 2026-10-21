package com.example.dailyanchor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DemoSession {

    public static String name = "John Doe";
    public static String email = "johndoe@mail.com";
    public static String birthday = "January 1, 2004";
    public static String password = "password";

    private static final Map<String, Entry> entries = new HashMap<>();

    static {
        Calendar day = Calendar.getInstance();
        Mood[] sampleMoods = {Mood.HAPPY, Mood.NEUTRAL, Mood.HAPPY};
        for (int i = 1; i <= 3; i++) {
            day.setTimeInMillis(System.currentTimeMillis());
            day.add(Calendar.DAY_OF_MONTH, -i);
            entries.put(MoodStore.key(day), new Entry(sampleMoods[i - 1], "", null));
        }
    }

    private DemoSession() {
    }

    public static final class Entry {
        public final Mood mood;
        public final String text;
        public final String photo;

        public Entry(Mood mood, String text, String photo) {
            this.mood = mood;
            this.text = text;
            this.photo = photo;
        }
    }

    public static Entry getEntry(String day) {
        return entries.get(day);
    }

    public static Mood getMood(String day, MoodStore store) {
        Entry entry = getEntry(day);
        return entry != null ? entry.mood : store.getMood(day);
    }

    public static void saveEntry(String day, Mood mood, String text, String photo) {
        entries.put(day, new Entry(mood, text, photo));
    }

    public static int[] moodCounts(Calendar first, int days, MoodStore store) {
        Calendar date = (Calendar) first.clone();
        int[] counts = new int[Mood.values().length];
        for (int i = 0; i < days; i++) {
            Mood mood = getMood(MoodStore.key(date), store);
            if (mood != null) counts[mood.ordinal()]++;
            date.add(Calendar.DAY_OF_MONTH, 1);
        }
        return counts;
    }

    public static int[] weekPercentages(MoodStore store) {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        int[] counts = moodCounts(start, 7, store);
        int total = 0;
        for (int count : counts) total += count;
        int[] percentages = new int[counts.length];
        if (total > 0) {
            for (int i = 0; i < counts.length; i++) {
                percentages[i] = Math.round(counts[i] * 100f / total);
            }
        }
        return percentages;
    }

    public static List<String> photoDays() {
        List<String> dates = new ArrayList<>();
        for (Map.Entry<String, Entry> entry : entries.entrySet()) {
            if (entry.getValue().photo != null) dates.add(entry.getKey());
        }
        Collections.sort(dates, Collections.reverseOrder());
        return dates;
    }
}
