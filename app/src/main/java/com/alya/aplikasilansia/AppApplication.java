package com.alya.aplikasilansia;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class AppApplication extends Application {

    public static final String CHANNEL_ID = "default";

    // BARU: konfigurasi Cloudinary dipusatkan di sini (single source of truth),
    // supaya UserRepository & file lain tinggal REFERENSI konstanta ini,
    // bukan nulis ulang string yang sama di banyak tempat (rawan typo/beda nilai).
    // GANTI dengan nilai asli dari dashboard Cloudinary (cloudinary.com/console):
    public static final String CLOUDINARY_CLOUD_NAME = "dq59p6llb";
    public static final String CLOUDINARY_UPLOAD_PRESET = "profile_upload";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        initCloudinary();
    }

    // DIUBAH: "your_cloud_name" hardcode -> pakai konstanta CLOUDINARY_CLOUD_NAME di atas
    private void initCloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", CLOUDINARY_CLOUD_NAME);
        MediaManager.init(this, config);
        // BARU: konfirmasi di Logcat bahwa init() benar-benar jalan dengan nilai yang benar.
        // Kalau baris ini TIDAK MUNCUL sama sekali di Logcat saat app dibuka, berarti
        // AppApplication.onCreate() tidak terpanggil (masalah registrasi di Manifest).
        // Kalau MUNCUL tapi upload tetap error "Must supply cloud_name", itu tanda
        // ada config LAMA yang ke-cache di SharedPreferences HP -- solusinya uninstall
        // total app dari HP (bukan cuma install ulang APK), baru install lagi dari awal.
        android.util.Log.d("AppApplication", "Cloudinary initialized with cloud_name=" + CLOUDINARY_CLOUD_NAME);
    }

    /**
     * Dipindah dari MyFirebaseMessagingService.onCreate() ke sini.
     * Alasan: MyFirebaseMessagingService TIDAK DIJAMIN jalan setiap kali app dibuka
     * (cuma diinstansiasi sistem saat dibutuhkan, misal saat terima push FCM).
     * Kalau channel belum sempat dibuat, semua notifikasi (termasuk reminder)
     * GAGAL MUNCUL tanpa error apapun (silent fail dari sistem Android).
     * Application.onCreate() DIJAMIN jalan setiap kali proses app dimulai,
     * jadi channel pasti sudah ada sebelum ReminderReceiver butuh menampilkan notifikasi.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null && notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Pengingat & Notifikasi Umum",
                        NotificationManager.IMPORTANCE_HIGH // HIGH supaya muncul sebagai heads-up notification
                );
                channel.setDescription("Notifikasi pengingat obat/jadwal dan pemberitahuan umum aplikasi");
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}