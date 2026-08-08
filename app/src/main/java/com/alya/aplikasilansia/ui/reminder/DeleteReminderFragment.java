package com.alya.aplikasilansia.ui.reminder;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.alya.aplikasilansia.R;

public class DeleteReminderFragment extends DialogFragment {
    private static final String ARG_REMINDER_ID = "reminder_id";
    private String reminderId;
    private Button deleteBtn, cancelBtn;
    ReminderViewModel reminderViewModel;
    private OnReminderDeletedListener listener;

    public interface OnReminderDeletedListener {
        void onReminderDeleted(String reminderId);
    }

    public DeleteReminderFragment() {
        // Required empty public constructor
    }

    public static DeleteReminderFragment newInstance(String reminderId) {
        DeleteReminderFragment fragment = new DeleteReminderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REMINDER_ID, reminderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reminderId = getArguments().getString(ARG_REMINDER_ID);
        }
        reminderViewModel = new ViewModelProvider(this).get(ReminderViewModel.class);
        try {
            listener = (OnReminderDeletedListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement OnReminderDeletedListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delete_reminder, container, false);

        cancelBtn = view.findViewById(R.id.btn_cancel_delete);
        deleteBtn = view.findViewById(R.id.btn_delete_confirmed);

        cancelBtn.setOnClickListener(v -> dismiss());

        deleteBtn.setOnClickListener(v -> {
            if (reminderId != null && listener != null) {
                Log.d("DeleteReminderFragment", "Calling listener.onReminderDeleted with reminderId: " + reminderId);
                listener.onReminderDeleted(reminderId);
            } else {
                Log.d("DeleteReminderFragment", "Listener is null");
            }
            Log.d("DeleteReminderFragment", "Dismissing dialog");
            dismiss();
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnReminderDeletedListener) {
            listener = (OnReminderDeletedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnReminderDeletedListener");
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            // Get the screen width
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_corner_rounded);

            // Set the dialog's width to match the screen width
            dialog.getWindow().setLayout((int) (width * 0.9), ViewGroup.LayoutParams.WRAP_CONTENT);

//            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}