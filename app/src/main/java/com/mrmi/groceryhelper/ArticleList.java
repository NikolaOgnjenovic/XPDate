package com.mrmi.groceryhelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;

public class ArticleList {
    private final Context context;
    private static ArrayList<Article> articleList;
    private static String datePattern;

    public ArticleList(Context givenContext) {
        context = givenContext;
    }

    public ArrayList<Article> getArticleList() {
        loadArticleList();
        return articleList;
    }

    private void loadArticleList() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("Articles", null);
        Type type = new TypeToken<ArrayList<Article>>() {}.getType();
        articleList = gson.fromJson(json, type);

        if (articleList == null) {
            articleList = new ArrayList<>();
        }
    }

    //Saves the Articles array list as a json string in shared preferences using the gson library
    public void saveArticles() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(articleList);

        editor.putString("Articles", json);
        editor.apply();
    }

    //Adds an article to the list and sorts the article list array by expiration date
    public void addArticleToList(Article article) {
        articleList.add(article);
        sortArticleList();
        saveArticles();
    }

    //Sorts the article list by soonest expiring articles first
    public void sortArticleList() {
        Collections.sort(articleList, (o1, o2) -> {
            if (o1.getFormattedDate(datePattern) == null || o2.getFormattedDate(datePattern) == null)
                return 0;
            return o1.getFormattedDate(datePattern).compareTo(o2.getFormattedDate(datePattern));
        });
    }

    public void sortList(ArrayList<String> list) {
        Pattern datePattern = Pattern.compile("^([0-2][0-9]|(3)[0-1])(\\/)(((0)[0-9])|((1)[0-2]))(\\/)\\d{4}$");
        Collections.sort(list, (o1, o2) -> {
            Matcher matcher1 = datePattern.matcher(o1);
            Matcher matcher2 = datePattern.matcher(o2);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(datePattern+"/yyyy");
            try {
                return Objects.requireNonNull(sdf.parse(matcher1.group())).compareTo(sdf.parse(matcher2.group()));
            }
            catch (Exception e) {
                return 0;
            }
        });
    }

    //Loads the saved date pattern or creates a default "dd/MM" one if one is not found
    private void loadDatePattern() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Shared preferences", MODE_PRIVATE);
        datePattern = sharedPreferences.getString("DatePattern", "dd/MM");
    }

    //Saves the date pattern using Shared Preferences
    private void saveDatePattern() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Shared preferences", MODE_PRIVATE);
        sharedPreferences.edit().putString("DatePattern", datePattern).apply();
        System.out.println("[MRMI]: Saved date pattern: " + datePattern);
    }

    public void setDatePattern(String dateArg) {
        datePattern = dateArg;
        saveDatePattern();

        //Loop through all articles
        for (Article article : articleList) {
            //Change the month and day parts of the current article input
            String str = article.getExpirationDate();

            //Split the date into 3 parts (month, day, year)
            String[] splitDate = str.split("/");
            //Reverse the month and day part of the article
            str = splitDate[1] + "/" + splitDate[0] + "/" + splitDate[2];

            //Change the expiration date of the current article
            article.setExpirationDate(str);
        }

        //Save the changed articles
        saveArticles();
    }

    public String getDatePattern() {
        loadDatePattern();
        return datePattern;
    }
}
