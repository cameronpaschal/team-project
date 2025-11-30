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
            try {
                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                CharSequence name = "MinimalPhone";
                String description = "Notifications to prompt user to open app and grant permissions";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                if (nm != null) nm.createNotificationChannel(channel);
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                if (launchIntent == null) {
                    launchIntent = new Intent(context, MainActivity.class);
                }
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, flags);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("MinimalPhone — Permissions needed")
                        .setContentText("Open the app to grant Usage Access, Display over apps, and Do Not Disturb permissions.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);
                // Check for POST_NOTIFICATIONS permission on Android 13+
                if (android.os.Build.VERSION.SDK_INT < 33 || context.checkSelfPermission("android.permission.POST_NOTIFICATIONS") == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    if (nm != null) {
                        nm.notify(NOTIFICATION_ID, builder.build());
                    }
                } else {
                    Log.w("BootReceiver", "POST_NOTIFICATIONS permission not granted; notification not posted.");
                }
            } catch (Exception e) {
                Log.w("BootReceiver", "Failed to show boot notification", e);
            }
        }
    }
}
