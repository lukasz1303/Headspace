package com.example.headspace;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import java.util.Objects;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent repeating_intent = new Intent(context, MainActivity.class);
        repeating_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, repeating_intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Channel Reminder")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.launcher_circle)
                .setContentTitle("Reminder")
                .setAutoCancel(true)
                .setContentText("Time to get some Headspace.");

        if (Objects.equals(intent.getAction(), "Reminder")) {
            notificationManager.notify(2,builder.build());
        }
    }
}