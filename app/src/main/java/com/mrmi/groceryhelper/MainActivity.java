package com.mrmi.groceryhelper;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private TextView[] firstThreeNames, firstThreeDates;
    private ArticleList articleListClass;
    private Button datePatternButton;
    private String datePattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        articleListClass = new ArticleList(MainActivity.this);

        initializeAds();

        //Activity launching
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


        //Displaying top 3 expiring articles
        firstThreeNames = new TextView[]{findViewById(R.id.FirstExpiringArticleName), findViewById(R.id.SecondExpiringArticleName), findViewById(R.id.ThirdExpiringArticleName)};
        firstThreeDates = new TextView[]{findViewById(R.id.FirstExpiringArticleExpirationCounter), findViewById(R.id.SecondExpiringArticleExpirationCounter), findViewById(R.id.ThirdExpiringArticleExpirationCounter)};
        articleListClass = new ArticleList(this);
        DisplayExpiringArticles();


        //Date pattern changing: dd/MM to MM/dd and vice verca via a togle button
        datePatternButton = findViewById(R.id.datePatternButton);
        LoadSelectedDatePattern();

        datePatternButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Change the type of date the app will use on button click - MM/dd or dd/MM
                if (datePattern.equals("MM/dd")) {
                    datePattern = "dd/MM";
                } else {
                    datePattern = "MM/dd";
                }

                //Change the text of the button
                datePatternButton.setText(datePattern);
                articleListClass.SaveDatePattern(datePattern);

                articleListClass.LoadArticles(MainActivity.this);

                ArrayList<Article> articles = articleListClass.GetArticleList();

                //Loop through all articles
                for (Article article : articles) {
                    //Change the month and day parts of the current article input
                    String str = article.GetArticleExpirationDate();

                    //Split the date into 3 parts (month, day, year)
                    String[] splitDate = str.split("/");
                    str = splitDate[1] + "/" + splitDate[0] + "/" + splitDate[2];

                    for (String part : splitDate) {
                        System.out.println("[MRMI]: Part: " + part);
                    }

                    //Change the expiration date of the current article
                    article.SetArticleExpirationDate(str);
                }

                //Save the changed articles
                articleListClass.SaveArticles();
            }

        });

        Button notificationTimePicker = findViewById(R.id.setNotificationTime);
        //Set the time for when the daily notification will be picked
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
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        DisplayExpiringArticles();
    }

    //Loads the Articles ArrayList from shared preferences as a decoded json string using gson or creates a new one if it's loaded as null
    private void DisplayExpiringArticles() {
        //Load the articles using the article list class
        articleListClass.LoadArticles();

        //Reference the article list
        ArrayList<Article> articles = articleListClass.GetArticleList();

        for (int i = 0; i < 3; ++i) {
            firstThreeNames[i].setVisibility(View.INVISIBLE);
            firstThreeDates[i].setVisibility(View.INVISIBLE);
        }

        for (int i = 0; i < 3; ++i) {
            if (articles.size() >= i + 1) {
                firstThreeNames[i].setVisibility(View.VISIBLE);
                firstThreeNames[i].setText(articles.get(i).GetArticleName());

                firstThreeDates[i].setVisibility(View.VISIBLE);
                firstThreeDates[i].setText(articles.get(i).GetExpirationText(this));
            }
        }
    }

    //Loads the selected date pattern and checks the datePatternButton accordingly
    private void LoadSelectedDatePattern() {
        articleListClass.LoadDatePattern();
        datePattern = articleListClass.GetDatePattern();

        datePatternButton.setText(datePattern);
        if (datePattern.equals("dd/MM")) {
            System.out.println("[MRMI]: Loaded date pattern: dd/MM");
        } else {
            System.out.println("[MRMI]: Loaded date pattern: MM/dd");
        }
    }

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