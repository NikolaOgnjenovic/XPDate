package com.mrmi.groceryhelper;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MyService extends Service {

    public MyService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ScheduleNotifications();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    private void ScheduleNotifications() {
        ArticleList articleList = new ArticleList(this);
        //articleList.SetNotifications();
    }
}
