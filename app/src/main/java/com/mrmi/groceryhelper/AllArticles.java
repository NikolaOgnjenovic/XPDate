package com.mrmi.groceryhelper;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AllArticles extends AppCompatActivity {

    private ArticleList articleList;
    private ExpandableListView expandableListView;
    private ExpandableListViewAdapter expandableListViewAdapter;
    private List<String> listDataGroup;
    private HashMap<String, List<String>> listDataChild;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_articles);

        Settings.loadLocale(this);

        initialiseViews();
        initialiseObjects();
        initialiseListData();

        /*
        //Display all of the articles using the recyclerView and its adapter
        RecyclerView recyclerView = findViewById(R.id.ArticleRecyclerView);
        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(this);
        recyclerViewAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        */
    }

    //Launch main activity on back pressed
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void initialiseViews() {
        expandableListView = findViewById(R.id.expandableListView);
    }

    private void initialiseObjects() {
        articleList = new ArticleList(AllArticles.this);

        //Initialise the list of groups
        listDataGroup = new ArrayList<>();
        //Initialise the list of children
        listDataChild = new HashMap<>();
        //Initialise the adapter object
        expandableListViewAdapter = new ExpandableListViewAdapter(this, listDataGroup, listDataChild);
        //Set the list adapter
        expandableListView.setAdapter(expandableListViewAdapter);
    }

    private void initialiseListData() {
        //Add group data
        String[] allCategories = this.getResources().getStringArray(R.array.category_names);
        listDataGroup.addAll(Arrays.asList(allCategories));

        //Get all articles from the ArticleList class
        ArrayList<Article> articles = articleList.getArticleList();
        //Loop through all articles and add them to their according lists (expiring soon, later etc.)
        ArrayList<Pair<String, ArrayList<String>>> listOfCategories = new ArrayList<>();
        for(String category : allCategories) {
            listOfCategories.add(new Pair<>(category, new ArrayList<>()));
        }
        List<String> categoryValues = Arrays.asList(this.getResources().getStringArray(R.array.category_values));
        for (Article article : articles) {
            String articleName = article.getName();

            for(Pair<String, ArrayList<String>> category : listOfCategories) {
                if(categoryValues.get(Arrays.asList(this.getResources().getStringArray(R.array.category_names)).indexOf(category.first)).equals(article.getCategory())) {
                    category.second.add(articleName);
                }
            }
        }

        //Add child data
        int index = 0;
        for(Pair<String, ArrayList<String>> category : listOfCategories) {
            listDataChild.put(listDataGroup.get(index), category.second);
            ++index;
        }

        //Notify the adapter
        expandableListViewAdapter.notifyDataSetChanged();
    }
}