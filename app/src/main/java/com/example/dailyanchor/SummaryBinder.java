package com.example.dailyanchor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

// Fills the "This Week's Mood Summary":D 
public final class SummaryBinder {

    private SummaryBinder() {
    }

    public static void bind(View summaryRoot, MoodStore store) {
        Context ctx = summaryRoot.getContext();
        LinearLayout container = summaryRoot.findViewById(R.id.summary_container);
        container.removeAllViews();
        int[] pct = DemoSession.weekPercentages(store);
        float density = ctx.getResources().getDisplayMetrics().density;

        for (Mood mood : Mood.values()) {
            View item = LayoutInflater.from(ctx).inflate(R.layout.item_summary_bar, container, false);
            int p = pct[mood.ordinal()];

            View bar = item.findViewById(R.id.bar);
            ViewGroup.LayoutParams lp = bar.getLayoutParams();
            lp.height = p == 0 ? 0 : Math.round((8 + p * 1.9f) * density);
            bar.setLayoutParams(lp);
            bar.setBackground(roundedBar(ctx, ContextCompat.getColor(ctx, mood.color), density));

            ((TextView) item.findViewById(R.id.percent)).setText(p + "%");
            ((TextView) item.findViewById(R.id.label)).setText(mood.label);
            container.addView(item);
        }
    }

    private static android.graphics.drawable.GradientDrawable roundedBar(Context ctx, int color, float density) {
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(20 * density);
        return d;
    }
}
