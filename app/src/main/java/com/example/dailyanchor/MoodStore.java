package com.example.dailyanchor;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// tiny SharedPreferences-backed storage for moods, diary text, photos, and profile
public class MoodStore {
    // demo data
    private static final boolean SEED_DEMO_DATA = true;

    private static final SimpleDateFormat KEY_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private final SharedPreferences prefs;

    public MoodStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences("dailyanchor", Context.MODE_PRIVATE);
        if (SEED_DEMO_DATA && !prefs.getBoolean("seeded", false)) {
            seed();
        }
    }

    public static String key(Calendar c) {
        return KEY_FORMAT.format(c.getTime());
    }

    public static Calendar parse(String key) {
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(KEY_FORMAT.parse(key));
        } catch (Exception ignored) {
        }
        return c;
    }

    // ENTRIES

    public Mood getMood(String day) {
        return Mood.fromName(prefs.getString("mood_" + day, null));
    }

    public void setMood(String day, Mood mood) {
        SharedPreferences.Editor e = prefs.edit();
        if (mood == null) e.remove("mood_" + day);
        else e.putString("mood_" + day, mood.name());
        e.apply();
    }

    public String getText(String day) {
        return prefs.getString("text_" + day, "");
    }

    public void setText(String day, String text) {
        prefs.edit().putString("text_" + day, text).apply();
    }

    public String getPhoto(String day) {
        return prefs.getString("photo_" + day, null);
    }

    public void setPhoto(String day, String uri) {
        prefs.edit().putString("photo_" + day, uri).apply();
    }

    // PROFILE

    public String getName() {
        return prefs.getString("name", "John Doe");
    }

    public String getEmail() {
        return prefs.getString("email", "johndoe@mail.com");
    }

    public void setProfile(String name, String email) {
        prefs.edit().putString("name", name).putString("email", email).apply();
    }

    // WEEKLY SUMMARY

    // PERCENTAGE STATS
    public int[] weekPercentages() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        int[] counts = new int[Mood.values().length];
        int total = 0;
        for (int i = 0; i < 7; i++) {
            Mood m = getMood(key(c));
            if (m != null) {
                counts[m.ordinal()]++;
                total++;
            }
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
        int[] pct = new int[counts.length];
        if (total > 0) {
            for (int i = 0; i < counts.length; i++) {
                pct[i] = Math.round(counts[i] * 100f / total);
            }
        }
        return pct;
    }

    // DUMMY DATA

    private void seed() {
        Mood[] oct = {
                Mood.NEUTRAL, Mood.ANGRY, Mood.HAPPY,            // 1-3
                Mood.HAPPY, Mood.ANXIOUS, Mood.HAPPY, Mood.HAPPY, // 4-7
                Mood.NEUTRAL, Mood.SAD, Mood.ANXIOUS,            // 8-10
                Mood.SAD, Mood.HAPPY, Mood.NEUTRAL               // 11-13
        };
        SharedPreferences.Editor e = prefs.edit();
        for (int i = 0; i < oct.length; i++) {
            e.putString(String.format(Locale.US, "mood_2026-10-%02d", i + 1), oct[i].name());
        }
        e.putBoolean("seeded", true).apply();
    }
}
