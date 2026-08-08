package com.alya.aplikasilansia.ui.reminder;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    public CustomSpinnerAdapter(@NonNull Context context, int resource, @NonNull String[] objects) {
        super(context, resource, objects);
    }

    @Override
    public boolean isEnabled(int position) {
        // Disable the first item from Spinner
        // First item will be used for hint
        return position != 0;
    }

    @NonNull
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView textView = (TextView) view;
        if (position == 0) {
            // Set the hint text color to gray
            textView.setTextColor(getContext().getResources().getColor(android.R.color.darker_gray));
        } else {
            textView.setTextColor(getContext().getResources().getColor(android.R.color.black));
        }
        return view;
    }
}
