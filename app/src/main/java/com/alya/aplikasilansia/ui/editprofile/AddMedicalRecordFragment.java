package com.alya.aplikasilansia.ui.editprofile;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.alya.aplikasilansia.R;
import com.alya.aplikasilansia.data.inputMedHistory;

public class AddMedicalRecordFragment extends DialogFragment {
    private Button btnsSave, btnCancel;
    private EditText  etPenyakit, etTahun, etBulan;
    private OnMedicalRecordAddedListener mListener;

    public interface OnMedicalRecordAddedListener {
        void onMedicalRecordAdded(inputMedHistory newMedHistory);
    }

    public void setOnMedicalRecordAddedListener(OnMedicalRecordAddedListener listener) {
        mListener = listener;
    }
    public AddMedicalRecordFragment() {
        // Required empty public constructor
    }
    public static AddMedicalRecordFragment newInstance(String param1, String param2) {
        AddMedicalRecordFragment fragment = new AddMedicalRecordFragment();
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_corner_rounded);

            dialog.getWindow().setLayout((int) (width * 0.85), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_medical_record, container, false);

        etPenyakit = view.findViewById(R.id.et_penyakit_profile);
        etTahun = view.findViewById(R.id.et_med_years_profile);
        etBulan = view.findViewById(R.id.et_med_months_profile);

        btnsSave = view.findViewById(R.id.btn_save_med_profile);
        btnCancel = view.findViewById(R.id.btn_cancel_med_profile);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnsSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etPenyakit != null && etTahun != null && etBulan != null) {
                    String newPenyakit = etPenyakit.getText().toString().trim();
                    String newTahun = etTahun.getText().toString().trim();
                    String newBulan = etBulan.getText().toString().trim();

                    if (mListener != null) {
                        inputMedHistory newMedHistory = new inputMedHistory(newPenyakit, newTahun, newBulan);
                        mListener.onMedicalRecordAdded(newMedHistory);
                        dismiss();
                    }
                } else {
                    Toast.makeText(getActivity(), "Mohon lengkapi", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }
}