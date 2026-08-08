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

    public static void scheduleReminder(Context context, String title, String desc, String timestamp) {
        Log.d(TAG, "scheduleReminder called");

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
                intent.setAction(ACTION_REMINDER); // Set the action here
                intent.putExtra("title", title);
                intent.putExtra("desc", desc);

                int requestCode = 0;
//                int requestCode = generateUniqueRequestCode();
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                Log.d(TAG, "PendingIntent created: " + (pendingIntent != null));

                if (canScheduleExactAlarms(context, alarmManager)) {
                    Log.d(TAG, "Can schedule exact alarms");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent);
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent);
                    }
                    Log.d(TAG, "Scheduled reminder at: " + reminderTimeMillis);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Intent intentSettings = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        intentSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intentSettings);
                    }
                    Log.e(TAG, "Cannot schedule exact alarms");
                }
            }
        } catch (ParseException e) {
            Log.e(TAG, "ParseException: " + e.getMessage());
        }
    }

//    private static int generateUniqueRequestCode() {
//        return (int) System.currentTimeMillis();
//    }

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
