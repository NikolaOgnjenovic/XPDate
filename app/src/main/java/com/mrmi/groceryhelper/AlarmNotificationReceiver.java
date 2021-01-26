package com.mrmi.groceryhelper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;

public class AlarmNotificationReceiver extends BroadcastReceiver {
    private final String CHANNEL_ID = "GroceryHelper";
    private Context context;

    @Override
    public void onReceive(Context contextArg, Intent intent) {
        context = contextArg;
        createNotificationChannel();
        showNotification(getNotificationMessage());
    }

    //Creates a notification channel
    private void createNotificationChannel() {
        //Used in Android versions starting from Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Specify the channel's properties
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Grocery Helper", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Grocery helper notification channel");

            //Create the actual channel
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    //Creates the notification message: how many articles have already expired and how many are expiring today
    private String getNotificationMessage() {
        int haveExpired = 0, expiringToday = 0;
        String alarmMessage = "";

        ArticleList articleListClass = new ArticleList(context);
        ArrayList<Article> articleList = articleListClass.getArticleList();

        try {
            long currentArticleDaysLeft;
            for (Article article : articleList) {
                currentArticleDaysLeft = article.getExpirationDays(context);

                System.out.println("[MRMI]: Article: " + article.getName() + " days left: " + currentArticleDaysLeft);

                if (currentArticleDaysLeft == 0) {
                    ++expiringToday;
                } else if (currentArticleDaysLeft < 0) {
                    ++haveExpired;
                }
            }
            if (expiringToday > 0) {
                if (expiringToday == 1) alarmMessage += "1 article expires today.\n";
                else alarmMessage = expiringToday + " articles are expiring today.\n";
            }
            if (haveExpired > 0) {
                if (haveExpired == 1) alarmMessage += "1 article has expired.";
                else alarmMessage += haveExpired + " articles have expired.";
            }

            return alarmMessage;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public void showNotification(String notificationMessage) {
        if(notificationMessage.equals(""))
            return;

        System.out.println("[MRMI]: Setting notification with text " + notificationMessage);

        //Отвори MainActivity кад корисник притисне нотификацију
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        //Подешавања нотификације
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle("Grocery Helper")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationMessage))
                .setContentText(notificationMessage)
                .setContentIntent(pendingIntent) //Launch Main Activity when the user taps the notification
                .setAutoCancel(true) //Delete the notification once the user taps it
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) //Make it visible even on locked screens
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(2501, builder.build());
    }
}
