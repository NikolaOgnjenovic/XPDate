package com.mrmi.groceryhelper;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class ArticleList {
    Context context;
    ArrayList<Article> articles;
    String datePattern;

    public ArticleList(Context givenContext) {
        context = givenContext;
    }

    //Returns the article list
    public ArrayList<Article> GetArticleList() {
        return articles;
    }

    public String GetDatePattern() {
        return datePattern;
    }

    //Loads the Articles ArrayList from shared preferences as a decoded json string using gson or creates a new one if it's loaded as null
    public void LoadArticles(Context context1) {
        SharedPreferences sharedPreferences = context1.getSharedPreferences("Shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("Articles", null);
        Type type = new TypeToken<ArrayList<Article>>() {
        }.getType();
        articles = gson.fromJson(json, type);

        if (articles == null) {
            articles = new ArrayList<>();
        }
    }

    public void LoadArticles() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("Articles", null);
        Type type = new TypeToken<ArrayList<Article>>() {
        }.getType();
        articles = gson.fromJson(json, type);

        if (articles == null) {
            articles = new ArrayList<>();
        }

        sortArticleList();
    }

    //Saves the Articles array list as a json string in shared preferences using the gson library
    public void SaveArticles() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(articles);

        editor.putString("Articles", json);
        editor.apply();
    }

    //Adds an article to the list and sorts the article list array by expiration date
    public void AddArticleToList(Article article) {
        articles.add(article);
        sortArticleList();
        SaveArticles();
    }

    public void sortArticleList() {
         /*Loop through the whole array list while
        1) the expriation date of the article currently being inserted is smaller than the currently iterated article's expiration date
        2) i>=0: the index hasn't been decreased to less than 0 meaning this article should be put in first place
        */
        /*int i=articles.size()-2;
        while (i >= 0 && articles.get(i).CompareDate(article.GetArticleExpirationDate()))
        {
            articles.set(i+1, articles.get(i));
            i--;
        }
        articles.set(i+1, article);
        */
        Collections.sort(articles, new Comparator<Article>() {
            @Override
            public int compare(Article o1, Article o2) {
                try{
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy");
                    Date articleOne = sdf.parse(o1.GetArticleExpirationDate()), articleTwo = sdf.parse(o2.GetArticleExpirationDate());
                    return (articleTwo.after(articleOne)) ? 1 : 0;
                } catch(Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
            //return o1.GetArticleExpirationDate().compareTo(o2.GetArticleExpirationDate());
        });
    }

    //Loads or creates a new date pattern if one can't be loaded
    public void LoadDatePattern() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Shared preferences", MODE_PRIVATE);
        datePattern = sharedPreferences.getString("DatePattern", null);

        // "dd/MM" || "MM/dd"
        if (datePattern == null) {
            System.out.println("[MRMI]: No date pattern found locally, setting default pattern: dd/MM");
            datePattern = "dd/MM";
        }
    }

    //Saves the current date pattern
    public void SaveDatePattern(String selectedDatePattern) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("DatePattern", selectedDatePattern).apply();
        System.out.println("[MRMI]: Saved date pattern: " + selectedDatePattern);
    }
}
