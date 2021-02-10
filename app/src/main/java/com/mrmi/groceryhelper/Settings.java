package com.mrmi.groceryhelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TimePicker;

import static android.app.AlertDialog.THEME_HOLO_DARK;

public class Settings extends AppCompatActivity {

    private ArticleList articleListClass;
    private SharedPreferences sharedPreferences;
    private Button datePatternButton;
    private String datePattern;
    private TimePickerDialog timePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        articleListClass = new ArticleList(Settings.this);

        //Date pattern changing: dd/MM to MM/dd and vice verca via a togle button
        datePatternButton = findViewById(R.id.datePatternButton);
        displaySelectedDatePattern();

        datePatternButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDatePattern();
            }
        });

        sharedPreferences = getSharedPreferences("Shared preferences", MODE_PRIVATE);

        //Enable and disable daily notifications using the switch
        SwitchCompat dailyNotificationSwitch = findViewById(R.id.dailyNotificationSwitch);
        final NotificationManager notificationManager = new NotificationManager(Settings.this);
        dailyNotificationSwitch.setChecked(sharedPreferences.getBoolean("SendingDailyNotifications", false));
        /*if(dailyNotificationSwitch.isChecked()) {
            notificationManager.enableDailyNotifications();
        }*/
        dailyNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    notificationManager.enableDailyNotifications();
                } else {
                    notificationManager.disableDailyNotifications();
                }
                sharedPreferences.edit().putBoolean("SendingDailyNotifications", isChecked).apply();
            }
        });

        //Set the time for when the daily notification will be picked using a picker displayed when the button is pressed
        Button notificationTimePicker = findViewById(R.id.setNotificationTime);
        notificationTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = sharedPreferences.getInt("notificationHour", 9);
                int minutes = sharedPreferences.getInt("notificationMinute", 0);
                timePicker = new TimePickerDialog(Settings.this,
                        THEME_HOLO_DARK,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                                //If notification sending is enabled, set the saved time for the notifications to be sent and enable them with the new saved time
                                if(sharedPreferences.getBoolean("SendingDailyNotifications", false)) {
                                    sharedPreferences.edit().putInt("notificationHour", sHour).apply();
                                    sharedPreferences.edit().putInt("notificationMinute", sMinute).apply();
                                    notificationManager.enableDailyNotifications();
                                }
                            }
                        }, hour, minutes, true);
                timePicker.show();
            }
        });
    }

    //Loads the saved date pattern using the ArticleList class and sets the pattern button text accordingly
    private void displaySelectedDatePattern() {
        datePattern = articleListClass.getDatePattern();

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
        articleListClass.setDatePattern(datePattern);
    }
}