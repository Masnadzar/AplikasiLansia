package com.alya.aplikasilansia.ui.editprofile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alya.aplikasilansia.R;
import com.alya.aplikasilansia.data.inputMedHistory;
import com.alya.aplikasilansia.ui.reminder.CustomSpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

public class EditHealthFragment extends Fragment implements AddMedicalRecordFragment.OnMedicalRecordAddedListener {
    private Spinner caregiverSpinner, maritalStatSpinner;
    private LinearLayout medHistoryContainer;
    private EditProfileViewModel editProfileViewModel;
    private Button dialogAddMedButton;
    private List<inputMedHistory> medHistoryList = new ArrayList<>();
    private OnSaveEditListener onSaveEditListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSaveEditListener) {
            onSaveEditListener = (OnSaveEditListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnSaveEditListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSaveEditListener = null;
    }

    public EditHealthFragment() {
        // Required empty public constructor
    }
    public static EditHealthFragment newInstance(String param1, String param2) {
        EditHealthFragment fragment = new EditHealthFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editProfileViewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_health, container, false);

        medHistoryContainer = view.findViewById(R.id.edit_input_medhistory);
        dialogAddMedButton = view.findViewById(R.id.btn_medhistory_dialog);
        addNewMedicalRecordDialog(dialogAddMedButton);

        caregiverSpinner = view.findViewById(R.id.edit_care_giver);
        maritalStatSpinner = view.findViewById(R.id.edit_marital);

        view.findViewById(R.id.btn_save_health).setOnClickListener(v -> {saveProfileChanges();});

        view.findViewById(R.id.btn_health_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        editProfileViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                medHistoryList.addAll(user.getMedHistory());
                medicalRecord(medHistoryList);
                setSpinnerCaregiver(user.getCaregiver());
                setSpinnerMarStat(user.getMaritalStatus());
            }
        });

        editProfileViewModel.getUpdateResultLiveData().observe(getViewLifecycleOwner(), updateResult -> {

        });

        return view;
    }

    private void addNewMedicalRecordDialog(Button dialogAddMedButton){
        dialogAddMedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddMedicalRecordFragment addMedicalRecordFragment = new AddMedicalRecordFragment();
                addMedicalRecordFragment.setOnMedicalRecordAddedListener(EditHealthFragment.this);
                addMedicalRecordFragment.show(getActivity().getSupportFragmentManager(), "AddMedicalRecordDialog");
            }
        });

    }

    private void saveProfileChanges(){
        String newCaregiver = caregiverSpinner.getSelectedItem().toString();
        String newStatus = maritalStatSpinner.getSelectedItem().toString();

        if (onSaveEditListener != null) {
            onSaveEditListener.onSaveHealthData(newCaregiver, newStatus, medHistoryList);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("FRAGMENT_TYPE", "health");
            requireActivity().setResult(FragmentActivity.RESULT_OK, resultIntent);
            requireActivity().finish();
        } else {
            Log.e("EditHealthFragment", "onSaveEditListener is not attached");
        }
    }

    public void onMedicalRecordAdded(inputMedHistory newMedHistory) {
        medHistoryList.add(newMedHistory);
        medicalRecord(medHistoryList);
    }

    private void medicalRecord(List<inputMedHistory> medHistory){
        if (medHistory == null || medHistory.isEmpty()) {
            Log.d("ProfileHealthFragment", "Medical history is null or empty");
            return;
        }

        medHistoryContainer.removeAllViews();

        for (int i = 0; i < medHistory.size(); i++) {
            inputMedHistory history = medHistory.get(i);
            View itemView = getLayoutInflater().inflate(R.layout.register_input_medhistory, medHistoryContainer, false);

            EditText penyakitEditText = itemView.findViewById(R.id.et_penyakit);
            EditText tahunEditText = itemView.findViewById(R.id.et_med_years);
            EditText bulanEditText = itemView.findViewById(R.id.et_med_months);

            penyakitEditText.setText(history.getPenyakit());
            tahunEditText.setText(history.getLamanya());
            bulanEditText.setText(history.getLamanyaBulan());

            int finalI = i;
            penyakitEditText.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    medHistoryList.get(finalI).setPenyakit(s.toString());
                }
            });

            tahunEditText.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    medHistoryList.get(finalI).setLamanya(s.toString());
                }
            });

            bulanEditText.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    medHistoryList.get(finalI).setLamanyaBulan(s.toString());
                }
            });

            medHistoryContainer.addView(itemView);
        }
    }

    public abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private void setSpinnerCaregiver(String selectedCaregiver) {
        CustomSpinnerAdapter careGiverAdapter = new CustomSpinnerAdapter(
                getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.care_giver)
        );
        careGiverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        caregiverSpinner.setAdapter(careGiverAdapter);

        if (selectedCaregiver != null) {
            int position = careGiverAdapter.getPosition(selectedCaregiver);
            caregiverSpinner.setSelection(position);
        }
    }

    private void setSpinnerMarStat(String selectedMaritalStatus) {
        CustomSpinnerAdapter maritalStatusAdapter = new CustomSpinnerAdapter(
                getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.marital_status)
        );
        maritalStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        maritalStatSpinner.setAdapter(maritalStatusAdapter);

        if (selectedMaritalStatus != null) {
            int position = maritalStatusAdapter.getPosition(selectedMaritalStatus);
            maritalStatSpinner.setSelection(position);
        }
    }
}