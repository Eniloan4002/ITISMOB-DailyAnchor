package com.example.dailyanchor;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

// MOOD CLASS
public enum Mood {
    ANGRY("Angry", R.drawable.angry, R.drawable.calendar_angry, R.color.mood_angry),
    SAD("Sad", R.drawable.sad, R.drawable.calendar_sad, R.color.mood_sad),
    NEUTRAL("Neutral", R.drawable.neutral, R.drawable.calendar_neutral, R.color.mood_neutral),
    HAPPY("Happy", R.drawable.happy, R.drawable.calendar_happy, R.color.mood_happy),
    ANXIOUS("Anxious", R.drawable.anxious, R.drawable.calendar_anxious, R.color.mood_anxious);

    /** Order shown in the "Today's Mood" picker on the Day screen. */
    public static final Mood[] PICKER_ORDER = {ANXIOUS, NEUTRAL, HAPPY, SAD, ANGRY};

    public final String label;
    @DrawableRes public final int face;
    @DrawableRes public final int tile;
    @ColorRes public final int color;

    Mood(String label, int face, int tile, int color) {
        this.label = label;
        this.face = face;
        this.tile = tile;
        this.color = color;
    }

    public static Mood fromName(String name) {
        if (name == null) return null;
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
