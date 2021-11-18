package com.mrmi.groceryhelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArticleList articleList;

    private CardView[] firstThreeArticlesCardView;
    private TextView[] firstThreeNames, firstThreeDates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton addArticleButton = findViewById(R.id.addArticleButton);
        addArticleButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddArticle.class);
            startActivity(intent);
        });

        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Settings.class);
            startActivity(intent);
        });
        //Setup activity launching buttons
        ImageButton viewAllArticlesButton = findViewById(R.id.viewAllArticlesButton);
        viewAllArticlesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AllArticles.class);
            startActivity(intent);
        });


        //Reference ArticleList class
        articleList = new ArticleList(MainActivity.this);

        //Displaying top 3 expiring articles
        firstThreeNames = new TextView[]{findViewById(R.id.firstExpiringArticleName), findViewById(R.id.secondExpiringArticleName), findViewById(R.id.thirdExpiringArticleName)};
        firstThreeDates = new TextView[]{findViewById(R.id.firstExpiringArticleExpirationCounter), findViewById(R.id.secondExpiringArticleExpirationCounter), findViewById(R.id.thirdExpiringArticleExpirationCounter)};
        firstThreeArticlesCardView = new CardView[]{findViewById(R.id.firstExpiringArticle), findViewById(R.id.secondExpiringArticle), findViewById(R.id.thirdExpiringArticle)};
        displayExpiringArticles();

        initializeAds();
    }

    //Quit the app on back press
    @Override
    public void onBackPressed() {
        Intent quitIntent = new Intent(Intent.ACTION_MAIN);
        quitIntent.addCategory(Intent.CATEGORY_HOME);
        quitIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(quitIntent);
    }

    //Loads and displays the 3 soonest expiring articles
    private void displayExpiringArticles() {

        //Reference the article list
        ArrayList<Article> articles = articleList.getArticleList();

        //Make all 3 article displays invisible
        for (int i = 0; i < 3; ++i) {
            firstThreeArticlesCardView[i].setVisibility(View.INVISIBLE);
        }

        //Make the article displays visible and display the 3 (2, 1 or 0 if more do not exist) soonest expiring articles
        for (int i = 0; i < 3; ++i) {
            if (articles.size() >= i + 1) {
                firstThreeArticlesCardView[i].setVisibility(View.VISIBLE);

                firstThreeNames[i].setText(articles.get(i).getName());
                firstThreeDates[i].setText(articles.get(i).getExpirationText(this));
            }
        }
    }

    //Initializes ads on the ad banner
    private void initializeAds() {
        MobileAds.initialize(this, initializationStatus -> {
        });

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
}