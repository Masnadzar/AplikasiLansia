package com.alya.aplikasilansia.messaging;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.alya.aplikasilansia.AppApplication;
import com.alya.aplikasilansia.MainActivity;
import com.alya.aplikasilansia.R;
public class ReminderReceiver extends BroadcastReceiver {
    public static final String ACTION_REMINDER = "com.alya.aplikasilansia.ACTION_REMINDER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && ACTION_REMINDER.equals(intent.getAction())) {
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("desc");

            if (title != null && description != null) { // Check for nullability
                Intent notificationIntent = new Intent(context, MainActivity.class);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppApplication.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(description)
                        .setAutoCancel(true) // notifikasi otomatis hilang sekali di-tap, tidak nongkrong terus
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(0, builder.build());
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