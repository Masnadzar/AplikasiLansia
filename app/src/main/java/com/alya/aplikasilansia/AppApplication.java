package com.alya.aplikasilansia;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class AppApplication extends Application {

    public static final String CHANNEL_ID = "default";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
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