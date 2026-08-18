package com.alya.aplikasilansia.messaging;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.alya.aplikasilansia.AppApplication;
import com.alya.aplikasilansia.R;
import com.alya.aplikasilansia.data.ReminderRepository;
import com.alya.aplikasilansia.ui.reminder.ReminderActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String ACTION_REMINDER = "com.alya.aplikasilansia.ACTION_REMINDER";
    // BARU: dipakai MainActivity untuk tahu bahwa notifikasi ini di-tap khusus
    // untuk reminder, supaya bisa langsung diarahkan ke ReminderActivity.
    public static final String EXTRA_OPEN_REMINDER = "open_reminder";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && ACTION_REMINDER.equals(intent.getAction())) {
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("desc");
            String reminderId = intent.getStringExtra("reminderId");
            String userId = intent.getStringExtra("userId");
            int icon = intent.getIntExtra("icon", 0);

            if (title != null && description != null) { // Check for nullability
                // DIUBAH: notifikasi diklik sekarang membuka ReminderActivity langsung
                // (bukan cuma MainActivity/halaman home), supaya user begitu tap notifikasi
                // langsung diarahkan ke fitur Pengingat -- sesuai alur yang diminta:
                // notif diklik -> masuk app -> buka fitur pengingat -> lihat riwayat.
                Intent notificationIntent = new Intent(context, ReminderActivity.class);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                notificationIntent.putExtra(EXTRA_OPEN_REMINDER, true);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppApplication.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(description)
                        .setAutoCancel(true) // notifikasi otomatis hilang sekali di-tap, tidak nongkrong terus
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(0, builder.build());

                // BARU: catat riwayat pengingat setiap kali notifikasi benar-benar muncul ke user.
                // Ini yang menjadi sumber data untuk halaman "Riwayat Pengingat".
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String triggeredAt = sdf.format(new Date());
                new ReminderRepository().addReminderHistory(userId, reminderId, title, description, icon, triggeredAt);

                // DIHAPUS: sendNotificationInApp() -- method ini menampilkan Toast custom
                // BERULANG 5 KALI selama 10 detik (repetitions = 10000ms / 2000ms = 5).
                // Toast TIDAK butuh Activity untuk tampil, jadi dia muncul MENIMPA layar
                // apapun yang lagi kebuka (home screen, dsb) walau app sudah ditutup total.
                // Inilah penyebab "notifikasi terasa repeat" dan "ada teks nongol di luar app" --
                // itu bukan reminder baru, itu 1 reminder yang SAMA nongol 5x berturut-turut.
                // Notifikasi sistem (NotificationCompat di atas) sudah cukup dan sudah terbukti
                // berhasil (muncul + bunyi), jadi toast tambahan ini dihapus total.
            } else {
                Log.w("ReminderReceiver", "Null title or description received");
            }
        } else {
            // Log or handle the case where the action is not as expected
            Log.w("ReminderReceiver", "Unexpected or null intent action: " + (intent != null ? intent.getAction() : "null"));
        }
    }
}