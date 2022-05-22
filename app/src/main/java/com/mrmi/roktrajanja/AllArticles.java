package com.mrmi.roktrajanja;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AllArticles extends AppCompatActivity {

    private ArticleList articleListClass;
    private ArrayList<Article> articleList;
    private ExpandableListView expandableListView;
    private ExpandableListViewAdapter expandableListViewAdapter;
    private List<String> categoryNameList;
    private HashMap<String, List<String>> articleDataMap; //Mapped using the article's category name as a key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_articles);

        initialiseViews();
        initialiseObjects();
        initialiseListData();
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
        articleListClass = new ArticleList(AllArticles.this);
        articleList = articleListClass.getArticleList();

        categoryNameList = new ArrayList<>();
        articleDataMap = new HashMap<>();
        expandableListViewAdapter = new ExpandableListViewAdapter(this, categoryNameList, articleDataMap, true);
        expandableListView.setAdapter(expandableListViewAdapter);
    }

    private void initialiseListData() {
        //Get all category name display values (changed by changing locale)
        String[] categoryNames = this.getResources().getStringArray(R.array.category_names);

        //A list of lists of strings - list of categories holding all of their articles' information
        List<ArrayList<String>> categorizedArticleInfo = new ArrayList<>();

        //Add empty categories
        for(int i = 0; i < categoryNames.length; ++i) {
            categorizedArticleInfo.add(new ArrayList<>());
        }

        //Loop through all articles
        for(Article article : articleList) {
            //Generate it's info text and put the article into it's respective category using it's category Id
            categorizedArticleInfo.get(article.getArticleCategoryId()).add(article.getArticleInfo(this));
        }

        //Add group data - display each category's name and how many children it has (the size of each ArrayList of strings it's holding)
        for (int i = 0; i < categoryNames.length; ++i) {
            categoryNameList.add(categoryNames[i] + " (" + categorizedArticleInfo.get(i).size() + ")");
        }

        //Add child data - strings kept in ArrayList in the ArrayList
        for(int i = 0; i < categorizedArticleInfo.size(); ++i) {
            sortList(categorizedArticleInfo.get(i));
            articleDataMap.put(categoryNameList.get(i), categorizedArticleInfo.get(i));
        }

        //Notify the adapter
        expandableListViewAdapter.notifyDataSetChanged();

        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.DialogTheme).create();
            alertDialog.setTitle(getString(R.string.removal_title));
            alertDialog.setMessage(getString(R.string.removal_message));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.remove), (dialog, which) -> {
                articleListClass.removeByArticleInfo(Objects.requireNonNull(articleDataMap.get(categoryNameList.get(groupPosition))).get(childPosition));
                //articleList.remove(childPosition);
                articleListClass.saveArticles();
                expandableListViewAdapter.notifyDataSetChanged();
                recreate();
            });
            alertDialog.show();

            return false;
        });
    }

    /*Sorts a given ArrayList of strings by their expiration dates (found using a regex which finds xx/xx/xxxx in a string)
    Used to sort articles in their respective categories when displaying them in the ExpandableListView */
    private void sortList(ArrayList<String> list) {
        Pattern datePattern = Pattern.compile("^([0-2][0-9]|(3)[0-1])(/)(((0)[0-9])|((1)[0-2]))(/)\\d{4}$");
        Collections.sort(list, (o1, o2) -> {
            Matcher matcher1 = datePattern.matcher(o1);
            Matcher matcher2 = datePattern.matcher(o2);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(datePattern + "/yyyy");
            try {
                return Objects.requireNonNull(sdf.parse(matcher1.group())).compareTo(sdf.parse(matcher2.group()));
            } catch (Exception e) {
                return 0;
            }
        });
    }
}