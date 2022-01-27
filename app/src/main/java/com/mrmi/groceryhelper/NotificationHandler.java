package com.mrmi.groceryhelper;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationHandler extends Worker {

    private final String CHANNEL_ID = "GroceryHelper";
    private final Context context;

    public NotificationHandler(@NonNull Context cont, @NonNull WorkerParameters workerParams) {
        super(cont, workerParams);
        context = cont;
        Settings.loadLocale(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        scheduleNotification(context);
        createNotificationChannel();
        displayNotification(getNotificationMessage());

        //Indicate whether work was successful
        return Result.success();
    }

    //Creates a notification channel
    private void createNotificationChannel() {
        //Used in Android versions starting from Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Specify the channel's properties
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Grocery Helper", android.app.NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Grocery helper notification channel");

            //Create the actual channel
            android.app.NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    //Returns the notification message: how many articles have already expired and how many are expiring today
    private String getNotificationMessage() {
        int haveExpired = 0, expiringToday = 0;
        String expiredTodayText = context.getString(R.string.notification_expired_today), expiredText = context.getString(R.string.notification_expired);

        ArticleList articleListClass = new ArticleList(context);
        ArrayList<Article> articleList = articleListClass.getArticleList();

        //Loop through all articles and count how many articles expire today and how many articles have already expired
        long currentArticleDaysLeft;
        for (Article article : articleList) {
            currentArticleDaysLeft = article.getHoursUntilExpiration(context)/24;

            System.out.println("[MRMI]: Article: " + article.getName() + " days left: " + currentArticleDaysLeft);

            if (currentArticleDaysLeft == 0) {
                ++expiringToday;
            } else if (currentArticleDaysLeft < 0) {
                ++haveExpired;
            }
        }

        expiredTodayText += " " + expiringToday;
        expiredText += " " + haveExpired;

        return expiredTodayText + "\n" + expiredText;
    }

    //Display a notification with the given notification message
    public void displayNotification(String notificationMessage) {
        if (notificationMessage.equals(""))
            return;

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        //Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationMessage))
                .setContentText(notificationMessage)
                .setContentIntent(pendingIntent) //Launch Main Activity when the user taps the notification
                .setAutoCancel(true) //Delete the notification once the user taps it
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) //Make it visible even on locked screens
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        //Show the notification
        notificationManager.notify(2501, builder.build());
    }

    //Schedules a notification which will show up at the time specified in Settings saved in local storage using Shared preferences. (notificationHour, notificationMinute)
    public static void scheduleNotification(Context context) {
        killAllScheduledNotifications(context);
        int hourOfTheDay = context.getSharedPreferences("Shared preferences", Context.MODE_PRIVATE).getInt("notificationHour", 9), minuteOfTheDay = context.getSharedPreferences("Shared preferences", Context.MODE_PRIVATE).getInt("notificationMinute", 0);

        //Get current day and notification due date (notification time specified in shared prefs + 24 hours)
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();

        dueDate.set(Calendar.HOUR_OF_DAY, hourOfTheDay);
        dueDate.set(Calendar.MINUTE, minuteOfTheDay);
        dueDate.set(Calendar.SECOND, 0);

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24);
        }
        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
        //Fire a OneTimeWorkRequest to display a notification in timeDiff milliseconds
        OneTimeWorkRequest dailyWorkRequest = new OneTimeWorkRequest.Builder(NotificationHandler.class).addTag("Grocery tracker notification").setInitialDelay(timeDiff, TimeUnit.MILLISECONDS).build();
        WorkManager.getInstance(context).enqueue(dailyWorkRequest);

        System.out.println("[Mrmi]: Enqueued notification work request. User will be notified in " + timeDiff + " milliseconds.");
    }

    //Kill (disable) all scheduled notifications
    public static void killAllScheduledNotifications(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("Grocery tracker notification");
    }
}