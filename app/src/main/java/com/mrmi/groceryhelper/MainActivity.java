package com.mrmi.groceryhelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ArticleList articleList;

    private ExpandableListView expandableListView;
    private ExpandableListViewAdapter expandableListViewAdapter;
    private List<String> listDataGroup;
    private HashMap<String, List<String>> listDataChild;
    private ImageButton addArticleButton, settingsButton, viewAllArticlesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Settings.loadLocale(this);

        initialiseViews();
        initialiseObjects();
        initialiseListeners();
        initialiseListData();
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
        addArticleButton = findViewById(R.id.addArticleButton);
        settingsButton = findViewById(R.id.settingsButton);
        viewAllArticlesButton = findViewById(R.id.viewAllArticlesButton);
        expandableListView = findViewById(R.id.expandableListView);
    }

    private void initialiseObjects() {
        articleList = new ArticleList(MainActivity.this);

        //Enable notifications
        if (this.getSharedPreferences("Shared preferences", MODE_PRIVATE).getBoolean("SendingDailyNotifications", true)) {
            NotificationHandler.enableNotifications(this);
        }

        //Initialise the list of groups
        listDataGroup = new ArrayList<>();
        //Initialise the list of children
        listDataChild = new HashMap<>();
        //Initialise the adapter object
        expandableListViewAdapter = new ExpandableListViewAdapter(this, listDataGroup, listDataChild);
        //Set the list adapter
        expandableListView.setAdapter(expandableListViewAdapter);
    }

    private void initialiseListeners() {
        addArticleButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddArticle.class);
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        });
        viewAllArticlesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllArticles.class);
            startActivity(intent);
        });
    }

    private void initialiseListData() {
        //Add group data
        listDataGroup.add(getString(R.string.already_expired));
        listDataGroup.add(getString(R.string.expiring_today));
        listDataGroup.add(getString(R.string.expiration_less_than_7_days));
        listDataGroup.add(getString(R.string.expiration_less_than_14_days));
        listDataGroup.add(getString(R.string.expiration_less_than_30_days));
        listDataGroup.add(getString(R.string.expiration_more_than_30_days));

        //Get all articles from the ArticleList class
        ArrayList<Article> articles = articleList.getArticleList();
        //Loop through all articles and add them to their according lists (expiring soon, later etc.)
        List<String> expiredList = new ArrayList<>(), todayList = new ArrayList<>(), soonList = new ArrayList<>(), laterList = new ArrayList<>(), goodList = new ArrayList<>(), greatList = new ArrayList<>();
        for (Article article : articles) {
            int daysUntilExpiration = article.getDaysUntilExpiration(this);
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

        //Add child data
        listDataChild.put(listDataGroup.get(0), expiredList);
        listDataChild.put(listDataGroup.get(1), todayList);
        listDataChild.put(listDataGroup.get(2), soonList);
        listDataChild.put(listDataGroup.get(3), laterList);
        listDataChild.put(listDataGroup.get(4), goodList);
        listDataChild.put(listDataGroup.get(5), greatList);

        //Notify the adapter
        expandableListViewAdapter.notifyDataSetChanged();
    }
}