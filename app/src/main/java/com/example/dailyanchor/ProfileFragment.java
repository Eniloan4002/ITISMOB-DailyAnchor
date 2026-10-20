package com.example.dailyanchor;

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

// PROFILE
public class ProfileFragment extends Fragment {

    private MoodStore store;
    private View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_profile, container, false);
        store = new MoodStore(requireContext());
        // TODO: make Edit Details work: root.findViewById(R.id.btn_edit).setOnClickListener(v -> showEditDialog());
        bind();
        return root;
    }

    private void bind() {
        ((TextView) root.findViewById(R.id.profile_name)).setText(store.getName());
        ((TextView) root.findViewById(R.id.profile_email)).setText(store.getEmail());
    }

    private void showEditDialog() {
        int pad = Math.round(20 * getResources().getDisplayMetrics().density);
        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(pad, pad / 2, pad, 0);

        EditText name = new EditText(requireContext());
        name.setHint(R.string.name);
        name.setText(store.getName());
        name.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        form.addView(name);

        EditText email = new EditText(requireContext());
        email.setHint(R.string.email);
        email.setText(store.getEmail());
        email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        form.addView(email);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.edit_details)
                .setView(form)
                .setPositiveButton(R.string.save, (d, w) -> {
                    String n = name.getText().toString().trim();
                    String e = email.getText().toString().trim();
                    if (!n.isEmpty()) {
                        store.setProfile(n, e);
                        bind();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
