package com.mrmi.groceryhelper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.widget.ExpandableListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
    private List<String> listDataGroup;
    private HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_articles);

        initialiseViews();
        initialiseListeners();
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

    private void initialiseListeners() {
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {

            AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.DialogTheme).create();
            alertDialog.setTitle(getString(R.string.removal_title));
            alertDialog.setMessage(getString(R.string.removal_message));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.remove), (dialog, which) -> {

                List<String> selectedGroupList = listDataChild.get(listDataGroup.get(groupPosition));
                assert selectedGroupList != null;
                selectedGroupList.remove(childPosition);
                articleList.remove(childPosition);
                articleListClass.saveArticles();

                expandableListViewAdapter.notifyDataSetChanged();
            });
            alertDialog.show();

            return false;
        });
    }

    private void initialiseObjects() {
        articleListClass = new ArticleList(AllArticles.this);
        articleList = articleListClass.getArticleList();

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

        //Loop through all articles and add them to their according lists (expiring soon, later etc.)
        ArrayList<Pair<String, ArrayList<String>>> listOfCategories = new ArrayList<>();
        for(String category : allCategories) {
            listOfCategories.add(new Pair<>(category, new ArrayList<>()));
        }
        List<String> categoryValues = Arrays.asList(this.getResources().getStringArray(R.array.category_values));
        for (Article article : articleList) {
            String articleInfo = article.getName() + "\n" + getString(R.string.expiration_date) + " " + article.getExpirationDate() + "\n" + article.getExpirationText(this);

            for(Pair<String, ArrayList<String>> category : listOfCategories) {
                if(categoryValues.get(Arrays.asList(this.getResources().getStringArray(R.array.category_names)).indexOf(category.first)).equals(article.getCategory())) {
                    category.second.add(articleInfo);
                }
            }
        }

        for(int i = 0; i < allCategories.length; ++i) {
            listDataGroup.add(allCategories[i]+ " (" + listOfCategories.get(i).second.size() + ")");
        }

        //Add child data
        int index = 0;
        for(Pair<String, ArrayList<String>> category : listOfCategories) {
            sortList(category.second);
            listDataChild.put(listDataGroup.get(index), category.second);
            ++index;
        }

        //Notify the adapter
        expandableListViewAdapter.notifyDataSetChanged();
    }

    private void sortList(ArrayList<String> list) {
        Pattern datePattern = Pattern.compile("^([0-2][0-9]|(3)[0-1])(/)(((0)[0-9])|((1)[0-2]))(/)\\d{4}$");
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
}