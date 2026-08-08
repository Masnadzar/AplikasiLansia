package com.alya.aplikasilansia.ui.editprofile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alya.aplikasilansia.R;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;


public class EditPersonalFragment extends Fragment {
    private static final String TAG = "EditPersonalFragment";
    private EditProfileViewModel editProfileViewModel;
    private EditText editTextUserName;
    private TextView genderTextView, emailTextView, dateTextView, ageTextView;
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
    public EditPersonalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editProfileViewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_personal, container, false);

        editTextUserName = view.findViewById(R.id.et_username_profile);
        emailTextView = view.findViewById(R.id.tv_email_edit);
        genderTextView = view.findViewById(R.id.tv_gender);
        ageTextView = view.findViewById(R.id.tv_edit_age);

        dateTextView = view.findViewById(R.id.tv_edit_birthdate);
        setupDatePicker(dateTextView);

        view.findViewById(R.id.btn_save_edit).setOnClickListener(v -> {
            saveProfileChanges();
            requireActivity().onBackPressed();
        });
        view.findViewById(R.id.btn_cancel_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        editProfileViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                editTextUserName.setText(user.getUserName());
                emailTextView.setText(user.getEmail());
                genderTextView.setText(user.getGender());
                dateTextView.setText(user.getBirthDate());
                setAge(user.getBirthDate());

            }
        });

        editProfileViewModel.getUpdateResultLiveData().observe(getViewLifecycleOwner(), updateResult -> {
            Toast.makeText(getActivity(), updateResult, Toast.LENGTH_SHORT).show();

        });

        return view;
    }

    private void saveProfileChanges() {
        String newUsername = editTextUserName.getText().toString().trim();
        String newBirthdate = dateTextView.getText().toString().trim();

        if (onSaveEditListener != null) {
            onSaveEditListener.onSavePersonalData(newUsername, newBirthdate);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("FRAGMENT_TYPE", "personal");
            requireActivity().setResult(FragmentActivity.RESULT_OK, resultIntent);
            requireActivity().finish();
        } else {
            Log.e(TAG, "onSaveEditListener is not attached");
        }
    }

    private void setAge(String birthDate){
        if (birthDate != null && !birthDate.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Calendar birthDateCalendar = Calendar.getInstance();
                birthDateCalendar.setTime(Objects.requireNonNull(sdf.parse(birthDate)));
                Calendar today = Calendar.getInstance();
                int age = today.get(Calendar.YEAR) - birthDateCalendar.get(Calendar.YEAR);
                if (today.get(Calendar.DAY_OF_YEAR) < birthDateCalendar.get(Calendar.DAY_OF_YEAR)) {
                    age--;
                }
                String ageText = getString(R.string.age_format, age);
                ageTextView.setText(ageText);

                // Log age text
                Log.d(TAG, "Age: " + ageText);
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e(TAG, "Error parsing date: " + e.getMessage());
            }
        } else {
            ageTextView.setText("N/A");
            Log.e(TAG, "Birthdate is empty or null");
        }
    }

    private void setupDatePicker(TextView dateTextView) {
        EditPersonalFragment.this.dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setOpenAt(calendar.getTimeInMillis());
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pilih Tanggal Lahir Anda")
                .setCalendarConstraints(constraintsBuilder.build())
                .build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = format.format(selection);
            dateTextView.setText(formattedDate);
            setAge(formattedDate);
        });
        datePicker.show(requireActivity().getSupportFragmentManager(), "DATE_PICKER");
    }

}