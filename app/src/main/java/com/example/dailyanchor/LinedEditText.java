package com.example.dailyanchor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

// ALLOWS LINES (LIKE A NOTEBOOK) FOR EDIT TEXT IN DayActivity
public class LinedEditText extends AppCompatEditText {

    private final Paint linePaint = new Paint();

    public LinedEditText(Context context) {
        this(context, null);
    }

    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        linePaint.setColor(0xFF1A1A1A);
        linePaint.setStrokeWidth(getResources().getDisplayMetrics().density);
        setLineSpacing(0f, 1.8f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int count = Math.max(getLineCount(), getMinLines());
        int height = getHeight();
        int lineHeight = getLineHeight();
        int top = getPaddingTop();
        // draw a line at the bottom of each text row, and keep filling the empty space
        int y = top + lineHeight;
        int drawn = 0;
        while (drawn < count || y < height) {
            canvas.drawLine(getPaddingLeft(), y, getWidth() - getPaddingRight(), y, linePaint);
            y += lineHeight;
            drawn++;
            if (y > height + lineHeight) break;
        }
        super.onDraw(canvas);
    }
}
