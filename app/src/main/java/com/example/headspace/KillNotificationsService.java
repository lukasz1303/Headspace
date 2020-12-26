package com.example.headspace;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class KillNotificationsService extends Service {

    static class KillBinder extends Binder {
        final Service service;

        KillBinder(Service service) {
            this.service = service;
        }

    }

    public static int NOTIFICATION_ID = 666;
    private final IBinder mBinder = new KillBinder(this);

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }
    @Override
    public void onCreate() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
    }
}