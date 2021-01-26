package com.mrmi.groceryhelper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.widget.Toast;

import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

public class NotificationManager {

    private final Context context;

    private final SharedPreferences sharedPreferences;

    public NotificationManager(Context contextArg) {
        context = contextArg;
        sharedPreferences = context.getSharedPreferences("Shared preferences", MODE_PRIVATE);
    }

    public void enableDailyNotifications() {
        //Enable the boot receiver which calls this function when the device boots
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        //Schedule the repeating alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //Set the alarm to start at approximately 2:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, sharedPreferences.getInt("notificationHour", 9));
        calendar.set(Calendar.MINUTE, sharedPreferences.getInt("notificationMinute", 0));

        Intent intent = new Intent(context, AlarmNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 2501, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        Toast.makeText(context, "Enabled notifications", Toast.LENGTH_SHORT).show();
    }

    public void disableDailyNotifications() {
        //Disable the boot receiver
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        //Disable the repeating alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 2501, intent, PendingIntent.FLAG_NO_CREATE);

        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        Toast.makeText(context, "Disabled notifications", Toast.LENGTH_SHORT).show();
    }
}
