package com.example.minimalphone;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class BootReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "minimalphone_boot_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootReceiver", "Device booted, posting permission notification");

            // Create a notification channel on Android O+
            try {
                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CharSequence name = "MinimalPhone";
                    String description = "Notifications to prompt user to open app and grant permissions";
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                    channel.setDescription(description);
                    if (nm != null) nm.createNotificationChannel(channel);
                }

                // Build an intent that opens the app's MainActivity when the user taps the notification
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                if (launchIntent == null) {
                    // Fallback: explicitly create an intent to MainActivity
                    launchIntent = new Intent(context, MainActivity.class);
                }
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    flags |= PendingIntent.FLAG_IMMUTABLE;
                }

                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, flags);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("MinimalPhone — Permissions needed")
                        .setContentText("Open the app to grant Usage Access, Display over apps, and Do Not Disturb permissions.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

                if (nm != null) {
                    nm.notify(NOTIFICATION_ID, builder.build());
                }

            } catch (Exception e) {
                Log.w("BootReceiver", "Failed to show boot notification", e);
            }
        }
    }
}
