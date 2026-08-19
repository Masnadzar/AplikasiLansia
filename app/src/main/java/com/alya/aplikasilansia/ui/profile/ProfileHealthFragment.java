package com.alya.aplikasilansia.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.alya.aplikasilansia.LoginActivity;
import com.alya.aplikasilansia.R;
import com.alya.aplikasilansia.data.BloodPresRepository;
import com.alya.aplikasilansia.data.BloodPressure;
import com.alya.aplikasilansia.data.QuizHistoryItem;
import com.alya.aplikasilansia.data.QuizRepository;
import com.alya.aplikasilansia.data.User;
import com.alya.aplikasilansia.data.inputMedHistory;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Objects;


public class ProfileHealthFragment extends Fragment {
    private LinearLayout profileMedHistory;
    private TextView tvCaregiver, tvMaritalStatus;
    // BARU: dipakai untuk menampilkan hasil screening & tekanan darah terakhir
    private TextView tvQuizScore, tvQuizDate, tvBpValue, tvBpDate;
    private ProfileViewModel profileViewModel;
    // BARU: repository terpisah, dipakai langsung (bukan lewat ViewModel Fragment ini)
    // supaya tidak perlu bikin ViewModel baru cuma untuk 2 field ringkasan ini
    private QuizRepository quizRepository;
    private BloodPresRepository bloodPresRepository;
    private Button signOut;

    public ProfileHealthFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_health, container, false);

        profileMedHistory = view.findViewById(R.id.profile_medhistory);
        tvCaregiver = view.findViewById(R.id.tv_caregiver);
        tvMaritalStatus = view.findViewById(R.id.tv_marital_stat);
        // BARU: bind 4 TextView hasil screening & tensi -- sebelumnya tidak pernah
        // di-findViewById() sama sekali, makanya box-nya cuma nampilin placeholder
        // "Belum ada data tes" / "Belum ada data tensi" terus-menerus walau datanya ada.
        tvQuizScore = view.findViewById(R.id.tv_quiz_score);
        tvQuizDate = view.findViewById(R.id.tv_quiz_date);
        tvBpValue = view.findViewById(R.id.tv_bp_value);
        tvBpDate = view.findViewById(R.id.tv_bp_date);
        signOut = view.findViewById(R.id.btn_sign_out_2);

        quizRepository = new QuizRepository();
        bloodPresRepository = new BloodPresRepository();

        signOut.setOnClickListener(v -> {
            showLogoutDialog();
        });

        getData();
        getLatestQuizScore(); // BARU
        getLatestBloodPressure(); // BARU
        return view;

    }

    // BARU: ambil 1 hasil tes GDS (skrining depresi) yang paling baru, lalu tampilkan
    // ke tv_quiz_score & tv_quiz_date. Kalau belum pernah tes sama sekali, placeholder
    // bawaan di layout XML ("Belum ada data tes") otomatis tetap yang tampil.
    private void getLatestQuizScore() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            return;
        }
        quizRepository.fetchQuizHistory(userId);
        quizRepository.getQuizHistoryLiveData().observe(getViewLifecycleOwner(), new Observer<List<QuizHistoryItem>>() {
            @Override
            public void onChanged(List<QuizHistoryItem> quizHistoryItems) {
                if (quizHistoryItems != null && !quizHistoryItems.isEmpty()) {
                    // Sudah diurutkan terbaru-dulu di QuizRepository, jadi index 0 = paling baru
                    QuizHistoryItem latest = quizHistoryItems.get(0);
                    tvQuizScore.setText(latest.getTotalScore() + " - " + latest.getClassifiedScore());
                    tvQuizDate.setText(latest.getDate());
                }
            }
        });
    }

    // BARU: ambil 1 data tensi darah yang paling baru, lalu tampilkan
    // ke tv_bp_value & tv_bp_date.
    private void getLatestBloodPressure() {
        bloodPresRepository.getLatestBloodPressure().observe(getViewLifecycleOwner(), new Observer<BloodPressure>() {
            @Override
            public void onChanged(BloodPressure bloodPressure) {
                if (bloodPressure != null) {
                    tvBpValue.setText(bloodPressure.getBloodPressure() + " mmHg, Nadi " + bloodPressure.getPulse() + " bpm");
                    tvBpDate.setText(bloodPressure.getBpDate());
                }
            }
        });
    }

    public void showLogoutDialog() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.confirm_logout_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        dialog.show();

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.custom_corner_rounded);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);

        Button buttonConfirm = dialogView.findViewById(R.id.btn_logout_confirmed);
        Button buttonCancel = dialogView.findViewById(R.id.btn_cancel_logout);

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileViewModel.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void getData(){
        profileViewModel.getUserLiveData().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null) {
                    Log.d("ProfileHealthFragment", "User data received: " + user.toString());
                    tvCaregiver.setText(user.getCaregiver());
                    tvMaritalStatus.setText(user.getMaritalStatus());
                    profileMedHistory(user.getMedHistory());
                } else {
                    Log.d("ProfileHealthFragment", "User data is null");
                }
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        profileViewModel.fetchUser();
        getData();
        getLatestQuizScore(); // BARU: refresh setiap kali fragment ini dibuka lagi
        getLatestBloodPressure(); // BARU
        if (profileMedHistory != null) {
            profileMedHistory.post(() -> {
                profileMedHistory.scrollTo(0, 0);
            });
        }
    }

    private void profileMedHistory(List<inputMedHistory> medHistory){
        if (medHistory == null || medHistory.isEmpty()) {
            Log.d("ProfileHealthFragment", "Medical history is null or empty");
            return;
        }

        profileMedHistory.removeAllViews();

        for (inputMedHistory history : medHistory) {
            Log.d("ProfileHealthFragment", "Med history: " + history.toString());
            View itemView = getLayoutInflater().inflate(R.layout.profile_view_medhistory, profileMedHistory, false);

            TextView tvPenyakit = itemView.findViewById(R.id.tv_profile_penyakit);
            TextView tvMedYears = itemView.findViewById(R.id.tv_profile_tahun);
            TextView tvMedMonths = itemView.findViewById(R.id.tv_profile_bulan);

            tvPenyakit.setText(history.getPenyakit());
            tvMedYears.setText(history.getLamanya());
            tvMedMonths.setText(history.getLamanyaBulan());

            profileMedHistory.addView(itemView);
        }
    }
}