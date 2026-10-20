package com.example.dailyanchor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final int TAB_HOME = 0, TAB_CALENDAR = 1, TAB_PROFILE = 2;
    private ImageView navHome, navCalendar, navProfile;
    private int currentTab = TAB_HOME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        navHome = findViewById(R.id.nav_home);
        navCalendar = findViewById(R.id.nav_calendar);
        navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> showTab(TAB_HOME));
        navCalendar.setOnClickListener(v -> showTab(TAB_CALENDAR));
        navProfile.setOnClickListener(v -> showTab(TAB_PROFILE));

        // entry button
        findViewById(R.id.fab_write).setOnClickListener(v ->
                openDay(MoodStore.key(Calendar.getInstance())));

        if (savedInstanceState == null) {
            showTab(TAB_HOME);
        } else {
            currentTab = savedInstanceState.getInt("tab", TAB_HOME);
            updateNavIcons();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", currentTab);
    }

    private void showTab(int tab) {
        currentTab = tab;
        Fragment f;
        switch (tab) {
            case TAB_CALENDAR:
                f = new CalendarFragment();
                break;
            case TAB_PROFILE:
                f = new ProfileFragment();
                break;
            default:
                f = new HomeFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, f)
                .commit();
        updateNavIcons();
    }

    private void updateNavIcons() {
        navHome.setImageResource(currentTab == TAB_HOME ? R.drawable.nav_home_on : R.drawable.nav_home_off);
        navCalendar.setImageResource(currentTab == TAB_CALENDAR ? R.drawable.nav_calendar_on : R.drawable.nav_calendar_off);
        navProfile.setImageResource(currentTab == TAB_PROFILE ? R.drawable.nav_profile_on : R.drawable.nav_profile_off);
    }

    private void openDay(String dayKey) {
        Intent i = new Intent(this, DayActivity.class);
        i.putExtra(DayActivity.EXTRA_DATE, dayKey);
        startActivity(i);
    }
}
