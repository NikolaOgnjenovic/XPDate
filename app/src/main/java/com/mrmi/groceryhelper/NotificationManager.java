package com.mrmi.groceryhelper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import java.util.Calendar;

public class NotificationManager {

    private Context context;

    public void setNotifications(Context contextArg, Calendar calendar) {

        context = contextArg;

        //Enable boot receiver
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        startAlarm(calendar);
    }

    private void startAlarm(Calendar calendar) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent notificationAlarmIntent;
            PendingIntent pendingIntent;

            //Set the notification time
            if (calendar == null) {
                calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, 14);
            }
            System.out.println("[MRMI]: Calendar: " + calendar);

            notificationAlarmIntent = new Intent(context, AlarmNotificationReceiver.class);

            pendingIntent = PendingIntent.getBroadcast(context, 0, notificationAlarmIntent, 0);
            if (alarmManager != null) {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
