package com.alya.aplikasilansia.messaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.alya.aplikasilansia.MainActivity;
import com.alya.aplikasilansia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingServ";
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // token to your app server.
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
//        if (remoteMessage.getData().size() > 0) {
            // Extract reminder data from the message
            String reminderId = remoteMessage.getData().get("reminderId");
            String title = remoteMessage.getData().get("title");
            String description = remoteMessage.getData().get("desc");

            // Example: Check if this reminder is for the current user
            String userId = remoteMessage.getData().get("userId");
            String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (currentUserUid.equals(userId)) {
                sendNotification(title, description);
//            }
        }
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }

    private void createNotificationChannel() {
        Log.d(TAG, "createNotificationChannel CALLED");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                NotificationChannel existingChannel = notificationManager.getNotificationChannel("default");
                if (existingChannel == null) {
                    NotificationChannel channel = new NotificationChannel(
                            "default",
                            "Default Channel",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                    Log.d(TAG, "Notification channel 'default' created");
                } else {
                    Log.d(TAG, "Notification channel 'default' already exists");
                }
            }
        }
    }
}
