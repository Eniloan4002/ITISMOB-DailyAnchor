package com.example.dailyanchor;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_profile, container, false);
        root.findViewById(R.id.btn_edit).setOnClickListener(v -> showEditDialog());
        bind();
        return root;
    }

    private void bind() {
        ((TextView) root.findViewById(R.id.profile_name)).setText(DemoSession.name);
        ((TextView) root.findViewById(R.id.profile_email)).setText(DemoSession.email);
        ((TextView) root.findViewById(R.id.profile_full_name)).setText(DemoSession.name);
        ((TextView) root.findViewById(R.id.profile_full_email)).setText(DemoSession.email);
        ((TextView) root.findViewById(R.id.profile_birthday)).setText(DemoSession.birthday);
    }

    private void showEditDialog() {
        int pad = Math.round(20 * getResources().getDisplayMetrics().density);
        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(pad, pad / 2, pad, 0);

        EditText name = new EditText(requireContext());
        name.setHint(R.string.full_name);
        name.setText(DemoSession.name);
        name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        form.addView(name);

        EditText email = new EditText(requireContext());
        email.setHint(R.string.email);
        email.setText(DemoSession.email);
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        form.addView(email);

        TextView birthday = new TextView(requireContext());
        birthday.setText(DemoSession.birthday + "  ▾");
        birthday.setTextSize(16f);
        birthday.setPadding(0, pad / 2, 0, pad / 2);
        form.addView(birthday);
        final String[] chosenBirthday = {DemoSession.birthday};
        birthday.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(requireContext(), (view, year, month, day) -> {
                Calendar date = Calendar.getInstance();
                date.set(year, month, day);
                chosenBirthday[0] = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(date.getTime());
                birthday.setText(chosenBirthday[0] + "  ▾");
            }, 2004, 0, 1);
            picker.getDatePicker().setMaxDate(System.currentTimeMillis());
            picker.show();
        });

        EditText password = new EditText(requireContext());
        password.setHint(R.string.password);
        password.setText(DemoSession.password);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        form.addView(password);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.edit_details)
                .setView(form)
                .setPositiveButton(R.string.save, (d, w) -> {
                    String n = name.getText().toString().trim();
                    String e = email.getText().toString().trim();
                    if (!n.isEmpty()) {
                        DemoSession.name = n;
                        DemoSession.email = e;
                        DemoSession.birthday = chosenBirthday[0];
                        DemoSession.password = password.getText().toString();
                        bind();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
