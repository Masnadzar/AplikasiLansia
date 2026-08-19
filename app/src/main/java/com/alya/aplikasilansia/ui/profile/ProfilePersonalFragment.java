package com.alya.aplikasilansia.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.alya.aplikasilansia.LoginActivity;
import com.alya.aplikasilansia.R;
import com.alya.aplikasilansia.data.User;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ProfilePersonalFragment extends Fragment {
    private static final String TAG = "ProfilePersonalFragment";
    private FirebaseAuth mAuth;
    private Button signOut, deleteAccount;
    private ProfileViewModel profileViewModel;
    private TextView emailTextView, birthDateTextView, userNameTextView, ageTextView, genderTextView;

    public ProfilePersonalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_personal, container, false);

        emailTextView = view.findViewById(R.id.tv_email);
        birthDateTextView = view.findViewById(R.id.tv_date_profile);
        ageTextView = view.findViewById(R.id.tv_age_profile);
        genderTextView = view.findViewById(R.id.tv_gender);
        userNameTextView = view.findViewById(R.id.tv_username_profile);
        signOut = view.findViewById(R.id.btn_keluar);
        deleteAccount = view.findViewById(R.id.btn_hapus_akun);

        profileViewModel.getUserLiveData().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null) {
                    emailTextView.setText(user.getEmail());
                    birthDateTextView.setText(user.getBirthDate());
                    userNameTextView.setText(user.getUserName());
                    genderTextView.setText(user.getGender());
                    setAge(user.getBirthDate());
                }
            }
        });

        profileViewModel.getDeleteResultLiveData().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;

            if (result.equals("Account deleted successfully")) {
                Toast.makeText(requireContext(), "Akun Anda telah berhasil dihapus", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) getActivity().finish();

            } else if (result.equals("RECENT_LOGIN_REQUIRED")) {
                showReauthRequiredDialog();

            } else {
                Toast.makeText(requireContext(), result, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Delete account failed: " + result);
            }
        });

        signOut.setOnClickListener(v -> {
            showLogoutDialog();
        });

        deleteAccount.setOnClickListener(v -> {
            showDeleteAccountDialog();
        });

        return view;
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

    /**
     * Dialog konfirmasi Hapus Akun. Menggunakan layout confirm_delete_account_dialog.xml
     * (perlu dibuat, mengikuti pola confirm_logout_dialog.xml) dengan id tombol:
     * btn_delete_confirmed dan btn_cancel_delete.
     */
    public void showDeleteAccountDialog() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.confirm_delete_account_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.custom_corner_rounded);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);

        Button buttonConfirm = dialogView.findViewById(R.id.btn_delete_confirmed);
        Button buttonCancel = dialogView.findViewById(R.id.btn_cancel_delete);

        buttonConfirm.setOnClickListener(v -> {
            profileViewModel.deleteAccount();
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
    }

    /**
     * Ditampilkan kalau Firebase menolak penghapusan akun karena sesi login sudah lama
     * (FirebaseAuthRecentLoginRequiredException). Solusi paling aman & sederhana: arahkan
     * user logout lalu login ulang, baru ulangi proses hapus akun dari halaman Profil.
     *
     * TODO: kalau mau UX lebih mulus (tanpa logout manual), bisa diganti dengan re-auth
     * langsung di sini -- reauthenticate(EmailAuthProvider.getCredential(...)) untuk akun
     * email/password, atau re-trigger Google Sign-In silent untuk akun Google -- tapi itu
     * perlu akses ke LoginActivity/alur Google Sign-In yang belum saya lihat.
     */
    private void showReauthRequiredDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sesi Login Sudah Lama")
                .setMessage("Demi keamanan, Anda perlu masuk (login) ulang sebelum bisa menghapus akun. Silakan keluar dan masuk kembali, lalu ulangi proses hapus akun.")
                .setPositiveButton("Keluar Sekarang", (dialogInterface, which) -> {
                    profileViewModel.signOut();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    if (getActivity() != null) getActivity().finish();
                })
                .setNegativeButton("Batal", (dialogInterface, which) -> dialogInterface.dismiss())
                .show();
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

    @Override
    public void onResume() {
        super.onResume();
        profileViewModel.fetchUser();
    }

}