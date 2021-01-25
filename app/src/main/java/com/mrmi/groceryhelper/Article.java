package com.mrmi.groceryhelper;

import android.content.Context;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Article implements Serializable
{
    private String articleName, articleExpirationDate;

    //Constructor
    public Article(String articleNameArg, String articleExpirationDateArg)
    {
        articleName = articleNameArg;
        articleExpirationDate = articleExpirationDateArg;
    }

    //Returns the article name
    public String GetArticleName()
    {
        return articleName;
    }

    //Returns the article expiration date
    public String GetArticleExpirationDate()
    {
        return articleExpirationDate;
    }

    public void SetArticleExpirationDate(String str)
    {
        articleExpirationDate = str;
    }

    //Compares this object's date and with the given date String argument, returns true if date is later than argument otherwise false
    public boolean CompareDate(String article2ExpirationDate)
    {
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            //Set this object's date and given object's date
            Calendar calendar1 = Calendar.getInstance();
            Calendar calendar2 = Calendar.getInstance();

            Date date1 = sdf.parse(this.GetArticleExpirationDate());
            Date date2 = sdf.parse(article2ExpirationDate);

            calendar1.setTime(date1);
            calendar2.setTime(date2);

            //compareTo returns 0 if two dates are the same, -1 if calendar2 is of greater value and 1 if calendar 1 is of greater value
            return (calendar1.compareTo(calendar2)==1);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public String GetExpirationText(Context context)
    {
        String expirationCounterText;
        long daysLeft = CalculateExpirationCounter(context);

        if(daysLeft<0)
        {
            expirationCounterText = "Expired " + -1*daysLeft + " days ago";
        }
        else if(daysLeft==0)
        {
            expirationCounterText = "Expires today!";
        }
        else
        {
            expirationCounterText = "Expires in " + daysLeft + " days";
        }

        return expirationCounterText;
    }

    //Returns the number of days between an article's expiration date and today's date
    public long CalculateExpirationCounter(Context context)
    {
        try
        {
            Calendar expirationDay = Calendar.getInstance();

            ArticleList articleListClass = new ArticleList(context);
            articleListClass.LoadDatePattern();
            String datePattern = articleListClass.GetDatePattern();

            SimpleDateFormat sdf;   //Enable reading of "20/03/2021" strings
            //Parse that from the given expirationDate and set that as the time of the expirationDay calendar
            if(datePattern.equals("dd/MM"))
            {
                sdf = new SimpleDateFormat("dd/MM/yyyy");

            }
            else
            {
                sdf = new SimpleDateFormat("MM/dd/yyyy");

            }
            expirationDay.setTime(sdf.parse(this.GetArticleExpirationDate())); //Parse that from the given expirationDate and set that as the time of the expirationDay calendar

            //Get the current day using a calendar
            Calendar currentDay = Calendar.getInstance();
            currentDay.setTimeInMillis(System.currentTimeMillis());
            //Calculate the difference in days from the expiration date and current date
            return ((expirationDay.getTimeInMillis() - currentDay.getTimeInMillis())/86400000);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }
}
