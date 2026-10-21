package com.example.dailyanchor;

import android.content.Intent;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
    private ImageView photoSelected;
    private View photoPlaceholder;
    private LinearLayout moodRow;
    private String currentPhoto;

    private final Random random = new Random();
    private int lastPrompt = 0;

    private final ActivityResultLauncher<String[]> pickPhoto =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {
                }
                currentPhoto = uri.toString();
                showPhoto(currentPhoto);
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
        DemoSession.Entry previous = DemoSession.getEntry(dayKey);
        if (previous != null) diary.setText(previous.text);
        ScrollView scroll = findViewById(R.id.day_root);
        diary.setOnFocusChangeListener((view, focused) -> {
            if (focused) diary.post(() -> diary.requestRectangleOnScreen(
                    new android.graphics.Rect(0, 0, diary.getWidth(), diary.getHeight()), false));
        });
        diary.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                scroll.post(() -> diary.requestRectangleOnScreen(
                        new android.graphics.Rect(0, Math.max(0, diary.getHeight() - 80),
                                diary.getWidth(), diary.getHeight()), false));
            }
        });

        promptText = findViewById(R.id.prompt_text);
        findViewById(R.id.btn_random_prompt).setOnClickListener(v -> {
            int next;
            do {
                next = random.nextInt(PROMPTS.length);
            } while (next == lastPrompt);
            lastPrompt = next;
            promptText.setText(PROMPTS[next]);
        });

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
        currentPhoto = previous != null ? previous.photo : null;
        if (currentPhoto != null) showPhoto(currentPhoto);

        moodRow = findViewById(R.id.mood_row);
        selected = DemoSession.getMood(dayKey, store);
        buildMoodRow();
        HorizontalScrollView moodScroll = findViewById(R.id.mood_scroll);
        int distance = Math.round(150 * getResources().getDisplayMetrics().density);
        findViewById(R.id.mood_previous).setOnClickListener(v -> moodScroll.smoothScrollBy(-distance, 0));
        findViewById(R.id.mood_next).setOnClickListener(v -> moodScroll.smoothScrollBy(distance, 0));
        findViewById(R.id.btn_save_entry).setOnClickListener(v -> {
            if (selected == null && diary.getText().toString().trim().isEmpty() && currentPhoto == null) {
                Toast.makeText(this, "Add a mood or a little thought first!", Toast.LENGTH_SHORT).show();
                return;
            }
            DemoSession.saveEntry(dayKey, selected, diary.getText().toString(), currentPhoto);
            Toast.makeText(this, "Saved for this demo session ♡", Toast.LENGTH_SHORT).show();
            finish();
        });
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
            face.setLayoutParams(new LinearLayout.LayoutParams(Math.round(104 * d), Math.round(104 * d)));
            col.addView(face);

            TextView label = new TextView(this);
            label.setText(m.label);
            label.setTextSize(14);
            label.setTextColor(getColor(R.color.text_dark));
            label.setPadding(0, Math.round(4 * d), 0, 0);
            col.addView(label);

            col.setTag(m);
            col.setOnClickListener(v -> {
                selected = (selected == m) ? null : m;
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

}
