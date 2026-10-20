package com.example.dailyanchor;

import android.content.Intent;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

// SPECIFIC DAY ENTRY
public class DayActivity extends AppCompatActivity {

    public static final String EXTRA_DATE = "date";

    private static final String[] PROMPTS = {
            "Tell me about it ...",
            "What made you smile today?",
            "What is weighing on your mind right now?",
            "Describe one small thing you're grateful for.",
            "What was the hardest part of your day, and how did you handle it?",
            "Who did you talk to today, and how did it feel?",
            "What are you looking forward to tomorrow?",
            "If today had a color, what would it be and why?",
            "What did you learn about yourself today?",
            "Write a kind note to your future self."
    };

    private MoodStore store;
    private String dayKey;
    private Mood selected;

    private EditText diary;
    private TextView promptText;
    private ImageView photoSelected, photoPlaceholder;
    private LinearLayout moodRow;

    private final Random random = new Random();
    private int lastPrompt = 0;

    private final ActivityResultLauncher<String[]> pickPhoto =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {
                }
                // TODO: save the photo: store.setPhoto(dayKey, uri.toString());
                showPhoto(uri.toString());
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_day);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.day_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        store = new MoodStore(this);
        dayKey = getIntent().getStringExtra(EXTRA_DATE);
        if (dayKey == null) dayKey = MoodStore.key(Calendar.getInstance());

        ((TextView) findViewById(R.id.day_title)).setText(
                new SimpleDateFormat("MMMM d", Locale.getDefault()).format(MoodStore.parse(dayKey).getTime()));
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        diary = findViewById(R.id.diary_text);
        // TODO: load saved diary text: diary.setText(store.getText(dayKey));

        promptText = findViewById(R.id.prompt_text);
        findViewById(R.id.btn_random_prompt).setOnClickListener(v -> {
            int next;
            do {
                next = random.nextInt(PROMPTS.length);
            } while (next == lastPrompt);
            lastPrompt = next;
            promptText.setText(PROMPTS[next]);
        });

        // photo
        photoSelected = findViewById(R.id.photo_selected);
        photoPlaceholder = findViewById(R.id.photo_placeholder);
        final float radius = 16 * getResources().getDisplayMetrics().density;
        photoSelected.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        photoSelected.setClipToOutline(true);
        findViewById(R.id.photo_box).setOnClickListener(v -> pickPhoto.launch(new String[]{"image/*"}));
        String photo = null; // TODO: store.getPhoto(dayKey)
        if (photo != null) showPhoto(photo);

        // mood picker
        moodRow = findViewById(R.id.mood_row);
        selected = store.getMood(dayKey);
        buildMoodRow();
    }

    private void showPhoto(String uri) {
        try {
            photoSelected.setImageURI(Uri.parse(uri));
            photoSelected.setVisibility(View.VISIBLE);
            photoPlaceholder.setVisibility(View.INVISIBLE); // keeps the box size
        } catch (Exception e) {
            photoSelected.setVisibility(View.GONE);
            photoPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private void buildMoodRow() {
        moodRow.removeAllViews();
        float d = getResources().getDisplayMetrics().density;
        for (Mood m : Mood.PICKER_ORDER) {
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cp.setMarginEnd(Math.round(16 * d));
            col.setLayoutParams(cp);

            ImageView face = new ImageView(this);
            face.setImageResource(m.face);
            face.setScaleType(ImageView.ScaleType.FIT_CENTER);
            face.setLayoutParams(new LinearLayout.LayoutParams(Math.round(80 * d), Math.round(80 * d)));
            col.addView(face);

            TextView label = new TextView(this);
            label.setText(m.label);
            label.setTextSize(12);
            label.setTextColor(getColor(R.color.text_dark));
            label.setPadding(0, Math.round(4 * d), 0, 0);
            col.addView(label);

            col.setTag(m);
            col.setOnClickListener(v -> {
                selected = (selected == m) ? null : m; // tap again to clear
                // TODO: save the mood so Home/Calendar update: store.setMood(dayKey, selected);
                updateMoodHighlight();
            });
            moodRow.addView(col);
        }
        updateMoodHighlight();
    }

    private void updateMoodHighlight() {
        for (int i = 0; i < moodRow.getChildCount(); i++) {
            View col = moodRow.getChildAt(i);
            col.setAlpha(selected == null || col.getTag() == selected ? 1f : 0.4f);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // TODO: auto-save diary text: store.setText(dayKey, diary.getText().toString());
    }
}
