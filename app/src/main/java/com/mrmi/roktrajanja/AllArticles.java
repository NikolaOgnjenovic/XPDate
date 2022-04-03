package com.mrmi.roktrajanja;

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
    private List<String> categoryValues;

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
        //When the user clicks on a child (article), show a dialog which asks the user if he wants to delete the article from the list
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

                recreate();
            });
            alertDialog.show();

            return false;
        });
    }

    private void initialiseObjects() {
        articleListClass = new ArticleList(AllArticles.this);
        articleList = articleListClass.getArticleList();

        listDataGroup = new ArrayList<>();
        listDataChild = new HashMap<>();
        expandableListViewAdapter = new ExpandableListViewAdapter(this, listDataGroup, listDataChild, true);
        expandableListView.setAdapter(expandableListViewAdapter);

        //Get all category name values used in code (values are saved locally in Article objects, display values are loaded when displaying them here)
        categoryValues = Arrays.asList(this.getResources().getStringArray(R.array.category_values));
    }

    private void initialiseListData() {
        //Get all category name display values (values are saved locally in Article objects, display values are loaded when displaying them here)
        String[] allCategories = this.getResources().getStringArray(R.array.category_names);

        //Loop through all articles and add them to their respective lists (meat, canned goods, sauces...)
        ArrayList<Pair<String, ArrayList<String>>> listOfCategories = new ArrayList<>();
        for (String category : allCategories) {
            listOfCategories.add(new Pair<>(category, new ArrayList<>()));
        }
        for (Article article : articleList) {
            String articleInfo = article.getName() + "\n" + getString(R.string.expiration_date) + " " + article.getExpirationDate() + "\n" + article.getExpirationText(this);

            for (Pair<String, ArrayList<String>> category : listOfCategories) {
                if (categoryValues.get(Arrays.asList(this.getResources().getStringArray(R.array.category_names)).indexOf(category.first)).equals(article.getCategory())) {
                    category.second.add(articleInfo);
                }
            }
        }

        //Add group data
        for (int i = 0; i < allCategories.length; ++i) {
            listDataGroup.add(allCategories[i] + " (" + listOfCategories.get(i).second.size() + ")");
        }

        //Add child data
        int index = 0;
        for (Pair<String, ArrayList<String>> category : listOfCategories) {
            sortList(category.second);
            listDataChild.put(listDataGroup.get(index), category.second);
            ++index;
        }

        //Notify the adapter
        expandableListViewAdapter.notifyDataSetChanged();
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