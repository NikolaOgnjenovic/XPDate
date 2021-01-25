package com.mrmi.groceryhelper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;

public class AlarmNotificationReceiver extends BroadcastReceiver {
    private final String CHANNEL_ID = "GroceryHelper";
    private Context context = null;

    @Override
    public void onReceive(Context contextArg, Intent intent) {
        context = contextArg;
        createNotificationChannel();
        showNotification(getNotificationMessage());
    }

    private String getNotificationMessage() {

        int haveExpired = 0, expiringToday = 0;
        String alarmMessage = "";

        ArticleList articleListClass = new ArticleList(context);
        articleListClass.LoadArticles();
        ArrayList<Article> articleList = articleListClass.GetArticleList();

        try {
            for (Article article : articleList) {
                long currentArticleDaysLeft = article.CalculateExpirationCounter(context);

                System.out.println("[MRMI]: Article: " + article.GetArticleName() + " days left: " + currentArticleDaysLeft);

                if (currentArticleDaysLeft == 0) {
                    ++expiringToday;
                } else if (currentArticleDaysLeft < 0) {
                    ++haveExpired;
                }
            }
            if (expiringToday > 0) {
                if (expiringToday == 1) alarmMessage += "1 article expires today.\n";
                else alarmMessage += expiringToday + " articles are expiring today.\n";
            }
            if (haveExpired > 0) {
                if (haveExpired == 1) alarmMessage += "1 article has expired.";
                else alarmMessage += haveExpired + " articles have expired.";
            }

            return alarmMessage;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Error";
    }

    public void showNotification(String notificationMessage) {
        System.out.println("[MRMI]: Setting notification with text " + notificationMessage);

        //Отвори MainActivity кад корисник притисне нотификацију
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        //Подешавања нотификације
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle("Grocery helper")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationMessage))
                .setContentText(notificationMessage)
                .setContentIntent(pendingIntent) //Шта се догоди кад корисник притисне нотификацију
                .setAutoCancel(true) //Избриши нотификацију кад је корисник притисне
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(310, builder.build());
    }

    //Направи канал за нотификације - коришћен у Андроид верзијама >=8
    private void createNotificationChannel() {
        //У верзијама после Ореа (Андроид 8), направи канал за нотификације
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Grocery Helper"; //Име канала
            String description = "Grocery helper notification channel"; //Опис канала
            int importance = NotificationManager.IMPORTANCE_DEFAULT; //Важност канала (приоритет у односу на друге нотификације)
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            //Региструј канал у уређају
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
