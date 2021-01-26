package com.mrmi.groceryhelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            if (context.getSharedPreferences("Shared preferences", Context.MODE_PRIVATE).getBoolean("SendingDailyNotifications", true)) {
                NotificationManager notificationManager = new NotificationManager(context);
                notificationManager.enableDailyNotifications();
            }
        }
    }
}
