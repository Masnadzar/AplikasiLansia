package com.alya.aplikasilansia.messaging;

import static com.alya.aplikasilansia.messaging.ReminderReceiver.ACTION_REMINDER;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderScheduler {
    private static final String TAG = "ReminderScheduler";

    /**
     * DIUBAH: tambah parameter reminderId (document ID Firestore).
     * SEBELUMNYA requestCode di-hardcode 0 untuk SEMUA reminder -> ini BUG FATAL:
     * PendingIntent dengan requestCode yang sama akan SALING MENIMPA satu sama lain
     * di AlarmManager. Artinya kalau user punya 3 reminder, cuma reminder yang
     * TERAKHIR dijadwalkan yang benar-benar bakal bunyi -- 2 reminder sebelumnya
     * otomatis batal tanpa pemberitahuan apapun.
     * Sekarang requestCode dibuat unik per reminder dari hash reminderId,
     * supaya tiap reminder punya alarm sendiri-sendiri yang independen.
     */
    public static void scheduleReminder(Context context, String reminderId, String title, String desc, String timestamp) {
        Log.d(TAG, "scheduleReminder called for id=" + reminderId);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Log.d(TAG, "AlarmManager obtained: " + (alarmManager != null));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date reminderDate = sdf.parse(timestamp);
            Log.d(TAG, "Parsed date: " + reminderDate);

            if (reminderDate != null) {
                long reminderTimeMillis = reminderDate.getTime();
                Log.d(TAG, "Reminder time in millis: " + reminderTimeMillis);

                Intent intent = new Intent(context, ReminderReceiver.class);
                intent.setAction(ACTION_REMINDER);
                intent.putExtra("title", title);
                intent.putExtra("desc", desc);

                int requestCode = getRequestCode(reminderId); // DIUBAH: dari hardcode 0
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                Log.d(TAG, "PendingIntent created with requestCode=" + requestCode);

                if (canScheduleExactAlarms(context, alarmManager)) {
                    Log.d(TAG, "Can schedule exact alarms");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent);
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent);
                    }
                    Log.d(TAG, "Scheduled reminder at: " + reminderTimeMillis);
                } else {
                    // DIUBAH: SEBELUMNYA kalau izin exact alarm belum di-grant, kode cuma
                    // membuka halaman Settings lalu LANGSUNG BERHENTI -- alarm-nya TIDAK
                    // PERNAH terjadwal sama sekali (bukan cuma telat, tapi hilang total).
                    // Ini penyebab persis kenapa "reminder pertama gagal, reminder kedua berhasil":
                    // begitu user kasih izin lewat Settings SETELAH reminder pertama dibuat,
                    // reminder pertama itu SUDAH TERLANJUR gagal dan tidak pernah dicoba lagi.
                    // Sekarang ditambahkan fallback: tetap jadwalkan alarm biasa (tidak exact),
                    // supaya reminder TETAP BUNYI walau mungkin meleset beberapa menit dari
                    // waktu pastinya -- lebih baik telat daripada tidak bunyi sama sekali.
                    Log.e(TAG, "Cannot schedule exact alarms, falling back to inexact alarm");
                    alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Intent intentSettings = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        intentSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intentSettings);
                    }
                }
            }
        } catch (ParseException e) {
            Log.e(TAG, "ParseException: " + e.getMessage());
        }
    }

    /**
     * BARU: membatalkan alarm yang sudah terjadwal untuk 1 reminder spesifik.
     * WAJIB dipanggil sebelum reschedule saat edit (supaya alarm lama tidak nyangkut duplikat),
     * dan saat reminder dihapus (supaya tidak muncul notifikasi untuk reminder yang sudah dihapus).
     */
    public static void cancelReminder(Context context, String reminderId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ACTION_REMINDER);

        int requestCode = getRequestCode(reminderId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Cancelled reminder id=" + reminderId + " (requestCode=" + requestCode + ")");
        }
    }

    // BARU: requestCode unik & konsisten selama reminderId sama (dipakai untuk schedule & cancel)
    private static int getRequestCode(String reminderId) {
        if (reminderId == null) return 0;
        return reminderId.hashCode();
    }

    private static boolean canScheduleExactAlarms(Context context, AlarmManager alarmManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                return alarmManager.canScheduleExactAlarms();
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException: " + e.getMessage());
                return false;
            }
        } else {
            return true;
        }
    }
}