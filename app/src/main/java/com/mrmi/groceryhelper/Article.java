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

    //Constructor
    public Article(String articleNameArg, String articleExpirationDateArg) {
        articleName = articleNameArg;
        articleExpirationDate = articleExpirationDateArg;
    }

    //Returns the article name
    public String getName() {
        return articleName;
    }

    //Returns the article expiration date
    public String getExpirationDate() {
        return articleExpirationDate;
    }

    public void setExpirationDate(String str) {
        articleExpirationDate = str;
    }

    /**
     * @param datePattern: saved date pattern on the device for the formatter to use
     * @return formatted date using sdf.parse(datePattern+"/yyyy") or null if an exception is caught
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
     * @return returns text which says when the article expires/has expired or if it's expiring today
     */
    public String getExpirationText(Context context) {
        String expirationCounterText;
        int daysLeft = getDaysUntilExpiration(context);

        if (daysLeft < -1) {
            expirationCounterText = "Expired " + -1 * daysLeft + " days ago.";
        } else if (daysLeft == -1) {
            expirationCounterText = "Expired 1 day ago.";
        } else if (daysLeft == 0) {
            expirationCounterText = "Expires today.";
        } else if (daysLeft == 1) {
            expirationCounterText = "Expires tomorrow.";
        } else {
            expirationCounterText = "Expires in " + (++daysLeft) + " days.";
        }

        return expirationCounterText;
    }

    /**
     * @param context context
     * @return days left until the article expires
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

    public int getDaysUntilExpiration(Context context) {
        long millisLeft = getMillisUntilExpiration(context);
        long daysLeft = millisLeft / 86400000;

        Calendar currentDay = Calendar.getInstance();
        int day = currentDay.get(Calendar.DAY_OF_MONTH);

        currentDay.setTimeInMillis(currentDay.getTimeInMillis() + millisLeft);

        if (currentDay.get(Calendar.DAY_OF_MONTH) == day + 1) {
            ++daysLeft;
        }

        return (int) daysLeft;
    }
}
