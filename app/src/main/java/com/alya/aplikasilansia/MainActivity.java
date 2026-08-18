package com.alya.aplikasilansia;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.alya.aplikasilansia.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // Launcher untuk minta izin POST_NOTIFICATIONS (wajib Android 13+/API 33+).
    // Tanpa izin ini, NotificationManager.notify() akan GAGAL DIAM-DIAM
    // (tidak crash, tidak ada log error, notifikasi cuma tidak pernah muncul).
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // Tidak perlu aksi khusus di sini: kalau ditolak, reminder tetap tersimpan
                // di Firestore dan tetap muncul di dalam app, hanya saja notifikasi
                // di luar app tidak akan tampil sampai izin diberikan lewat Settings.
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.GONE);

        BottomNavigationView navView = findViewById(R.id.mobile_navigation);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_check, R.id.navigation_news, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        askNotificationPermission();

//        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
//            @Override
//            public void onComplete(@NonNull Task<String> task) {
//                if (!task.isSuccessful()) {
//                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
//                    return;
//                }
//
//                String token = task.getResult();
//                Log.d(TAG, "FCM Token: " + token);
//                // Save the token to Firebase Database under the user ID
//                saveTokenToServer(token);
//            }
//        });
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        // Di bawah Android 13, izin notifikasi otomatis diberikan saat install, tidak perlu diminta manual.
    }

    //    private void saveTokenToServer(String token) {
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
//        database.child("userTokens").child(userId).setValue(token);
//    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}