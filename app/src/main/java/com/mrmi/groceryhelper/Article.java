package com.mrmi.groceryhelper;

import android.annotation.SuppressLint;
import android.content.Context;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Article implements Serializable {
    private final String articleName;
    private String articleExpirationDate;
    private final String articleCategory;

    //Constructor
    public Article(String articleNameArg, String articleExpirationDateArg, String articleCategoryArg) {
        articleName = articleNameArg;
        articleExpirationDate = articleExpirationDateArg;
        articleCategory = articleCategoryArg;
    }

    //Returns the article name
    public String getName() {
        return articleName;
    }

    //Returns the article expiration date
    public String getExpirationDate() {
        return articleExpirationDate;
    }

    //Returns the article category
    public String getCategory() {
        return articleCategory;
    }

    public void setExpirationDate(String str) {
        articleExpirationDate = str;
    }

    /**
     * @param datePattern: saved date pattern on the device for the formatter to use
     * @return String which holds a formatted date using sdf.parse(datePattern+"/yyyy") or null if an exception is caught
     */
    public Date getFormattedDate(String datePattern) {
        datePattern += "/yyyy";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        try {
            return sdf.parse(articleExpirationDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param context - context used for getting the datePattern from the ArticleList
     * @return String which holds when the article expires/has expired or if it's expiring today
     */
    public String getExpirationText(Context context) {
        String expirationCounterText;

        int daysLeft = getHoursUntilExpiration(context) / 24, hoursMod = getHoursUntilExpiration(context) % 24;

        if (daysLeft < -1) {
            expirationCounterText = context.getString(R.string.expired) + " " + -1 * daysLeft + " " + context.getString(R.string.days_ago);
        } else if (daysLeft == 0 && hoursMod < 0) {
            expirationCounterText = context.getString(R.string.expired_today);
        } else if (daysLeft < 1 && hoursMod < 0) {
            expirationCounterText = context.getString(R.string.expired_yesterday);
        } else if (daysLeft <= 2) {
            expirationCounterText = context.getString(R.string.expires_in) + " " + hoursMod + " " + context.getString(R.string.hours);
        } else {
            expirationCounterText = context.getString(R.string.expires_in) + " " + (++daysLeft) + " " + context.getString(R.string.days);
        }

        return expirationCounterText;
    }

    /**
     * @param context context
     * @return milliseconds left until the article expires
     */
    //Returns the number of days between an article's expiration date and today's date
    @SuppressLint("SimpleDateFormat")
    public long getMillisUntilExpiration(Context context) {
        try {
            Calendar expirationDay = Calendar.getInstance();

            ArticleList articleListClass = new ArticleList(context);
            String datePattern = articleListClass.getDatePattern();

            SimpleDateFormat sdf;
            if (datePattern.equals("dd/MM")) {
                sdf = new SimpleDateFormat("dd/MM/yyyy");
            } else {
                sdf = new SimpleDateFormat("MM/dd/yyyy");
            }

            //Parse that from the given expirationDate and set that as the time of the expirationDay calendar
            Date expirationDate = sdf.parse(articleExpirationDate);
            if (expirationDate != null) {
                expirationDay.setTime(expirationDate);
            }

            //Get the current day using a calendar
            Calendar currentDay = Calendar.getInstance();
            currentDay.setTimeInMillis(System.currentTimeMillis());

            //Calculate the difference in days from the expiration date and current date
            return expirationDay.getTimeInMillis() - currentDay.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * @param context context
     * @return hours left until the article expires
     */
    public int getHoursUntilExpiration(Context context) {
        return (int) getMillisUntilExpiration(context) / 3600000;
    }
}
