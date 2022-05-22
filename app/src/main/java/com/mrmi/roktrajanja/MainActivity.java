package com.mrmi.roktrajanja;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ArticleList articleList;

    private ExpandableListView expandableListView;
    private ExpandableListViewAdapter expandableListViewAdapter;
    private List<String> categoryNameList;
    private HashMap<String, List<String>> articleDataMap;
    private View settingsView, allArticlesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialiseViews();
        initialiseObjects();
        initialiseListeners();
        populateExpandableListView();
    }

    //Quit the app when the user presses the back button
    @Override
    public void onBackPressed() {
        Intent quitIntent = new Intent(Intent.ACTION_MAIN);
        quitIntent.addCategory(Intent.CATEGORY_HOME);
        quitIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(quitIntent);
    }

    private void initialiseViews() {
        settingsView = findViewById(R.id.settingsView);
        allArticlesView = findViewById(R.id.allArticlesView);
        expandableListView = findViewById(R.id.expandableListView);
    }

    private void initialiseObjects() {
        articleList = new ArticleList(MainActivity.this);

        //Schedule a notification
        if (this.getSharedPreferences("Shared preferences", MODE_PRIVATE).getBoolean("SendingDailyNotifications", true)) {
            NotificationHandler.scheduleNotification(this);
        }

        categoryNameList = new ArrayList<>();

        articleDataMap = new HashMap<>();

        expandableListViewAdapter = new ExpandableListViewAdapter(this, categoryNameList, articleDataMap, false);

        expandableListView.setAdapter(expandableListViewAdapter);
    }

    private void initialiseListeners() {
        settingsView.setOnClickListener(v -> {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        });
        allArticlesView.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllArticles.class);
            startActivity(intent);
        });
    }

    //Displays all articles categorised by expiration date in the ExpandableListView
    private void populateExpandableListView() {

        //Get all articles from the ArticleList class
        ArrayList<Article> articles = articleList.getArticleList();
        //Loop through all articles and add them to their according lists (expiring soon, later etc.)
        List<String> expiredList = new ArrayList<>(), todayList = new ArrayList<>(), soonList = new ArrayList<>(), laterList = new ArrayList<>(), goodList = new ArrayList<>(), greatList = new ArrayList<>();
        for (Article article : articles) {
            long daysUntilExpiration = article.getHoursUntilExpiration(this)/24;
            String articleName = article.getName();

            if (daysUntilExpiration < 0) {
                expiredList.add(articleName);
            } else if (daysUntilExpiration == 0) {
                todayList.add(articleName);
            } else if (daysUntilExpiration < 7) {
                soonList.add(articleName);
            } else if (daysUntilExpiration < 14) {
                laterList.add(articleName);
            } else if (daysUntilExpiration < 30) {
                goodList.add(articleName);
            } else {
                greatList.add(articleName);
            }
        }

        //Add group data
        categoryNameList.add(getString(R.string.already_expired) + " (" + expiredList.size() + ")");
        categoryNameList.add(getString(R.string.expires_today) + " (" + todayList.size() + ")");
        categoryNameList.add(getString(R.string.expiration_less_than_7_days) + " (" + soonList.size() + ")");
        categoryNameList.add(getString(R.string.expiration_less_than_14_days) + " (" + laterList.size() + ")");
        categoryNameList.add(getString(R.string.expiration_less_than_30_days) + " (" + goodList.size() + ")");
        categoryNameList.add(getString(R.string.expiration_more_than_30_days) + " (" + greatList.size() + ")");

        //Add child data
        articleDataMap.put(categoryNameList.get(0), expiredList);
        articleDataMap.put(categoryNameList.get(1), todayList);
        articleDataMap.put(categoryNameList.get(2), soonList);
        articleDataMap.put(categoryNameList.get(3), laterList);
        articleDataMap.put(categoryNameList.get(4), goodList);
        articleDataMap.put(categoryNameList.get(5), greatList);

        //Notify the adapter
        expandableListViewAdapter.notifyDataSetChanged();
    }
}