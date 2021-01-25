package com.mrmi.groceryhelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private TextView[] firstThreeNames, firstThreeDates;
    private ArticleList articleList;
    private Button datePatternButton;
    private String datePattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup activity launching buttons
        ImageButton ViewAllArticlesButton = findViewById(R.id.ViewAllArticlesButton);
        ViewAllArticlesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AllArticles.class);
                startActivity(intent);
            }
        });

        ImageButton AddArticleButton = findViewById(R.id.AddArticleButton);
        AddArticleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddArticle.class);
                startActivity(intent);
            }
        });

        //Reference ArticleList class
        articleList = new ArticleList(MainActivity.this);

        //Displaying top 3 expiring articles
        firstThreeNames = new TextView[]{findViewById(R.id.FirstExpiringArticleName), findViewById(R.id.SecondExpiringArticleName), findViewById(R.id.ThirdExpiringArticleName)};
        firstThreeDates = new TextView[]{findViewById(R.id.FirstExpiringArticleExpirationCounter), findViewById(R.id.SecondExpiringArticleExpirationCounter), findViewById(R.id.ThirdExpiringArticleExpirationCounter)};
        articleList = new ArticleList(this);
        displayExpiringArticles();

        //Date pattern changing: dd/MM to MM/dd and vice verca via a togle button
        datePatternButton = findViewById(R.id.datePatternButton);
        displaySelectedDatePattern();

        datePatternButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDatePattern();
            }
        });


        //Set the time for when the daily notification will be picked
        /*
        Button notificationTimePicker = findViewById(R.id.setNotificationTime);
        notificationTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(0, 0, 0, hourOfDay, minute);
                                NotificationManager notificationManager = new NotificationManager();
                                notificationManager.setNotifications(MainActivity.this, calendar);
                            }
                        }, 12, 0, true
                );
                timePickerDialog.show();
            }
        });*/

        initializeAds();
    }

    @Override
    public void onResume() {
        super.onResume();
        displayExpiringArticles();
    }

    //Loads and displays the 3 soonest expiring articles
    private void displayExpiringArticles() {

        //Reference the article list
        ArrayList<Article> articles = articleList.getArticleList();

        //Make all 3 article displays invisible
        for (int i = 0; i < 3; ++i) {
            firstThreeNames[i].setVisibility(View.INVISIBLE);
            firstThreeDates[i].setVisibility(View.INVISIBLE);
        }

        //Make the article displays visible and display the 3 (2, 1 or 0 if more do not exist) soonest expiring articles
        for (int i = 0; i < 3; ++i) {
            if (articles.size() >= i + 1) {
                firstThreeNames[i].setVisibility(View.VISIBLE);
                firstThreeNames[i].setText(articles.get(i).getName());

                firstThreeDates[i].setVisibility(View.VISIBLE);
                firstThreeDates[i].setText(articles.get(i).getExpirationText(this));
            }
        }
    }

    //Loads the saved date pattern using the ArticleList class and sets the pattern button text accordingly
    private void displaySelectedDatePattern() {
        datePattern = articleList.getDatePattern();

        datePatternButton.setText(datePattern);

        System.out.println("[MRMI]: Loaded date pattern: " + datePattern);
    }

    //Changes the date pattern
    private void changeDatePattern() {

        //Swap the datePattern with the unused one
        if (datePattern.equals("MM/dd")) {
            datePattern = "dd/MM";
        } else {
            datePattern = "MM/dd";
        }

        //Display and set the new date pattern
        datePatternButton.setText(datePattern);
        articleList.setDatePattern(datePattern);
    }

    //Initializes ads on the ad banner
    private void initializeAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
}