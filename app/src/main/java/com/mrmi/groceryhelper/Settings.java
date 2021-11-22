package com.mrmi.groceryhelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import static android.app.AlertDialog.THEME_HOLO_DARK;

public class Settings extends AppCompatActivity {

    private ArticleList articleListClass;
    private SharedPreferences sharedPreferences;
    private Button datePatternButton;
    private String datePattern;
    private TimePickerDialog timePicker;
    private SwitchCompat dailyNotificationSwitch;
    private Button notificationTimePicker;
    private TextView notificationTimeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initialiseViews();
        initialiseObjects();
        initialiseListeners();

        displaySelectedDatePattern();
        displayNotificationTime();

        //Enable and disable daily notifications using the switch
        dailyNotificationSwitch.setChecked(sharedPreferences.getBoolean("SendingDailyNotifications", true));
        if (dailyNotificationSwitch.isChecked()) {
            enableNotifications();
        }
    }

    private void initialiseViews() {
        datePatternButton = findViewById(R.id.datePatternButton);
        dailyNotificationSwitch = findViewById(R.id.dailyNotificationSwitch);
        notificationTimePicker = findViewById(R.id.setNotificationTime);
        notificationTimeTextView = findViewById(R.id.notificationTimeText);
    }

    private void initialiseObjects() {
        articleListClass = new ArticleList(this);
        sharedPreferences = getSharedPreferences("Shared preferences", MODE_PRIVATE);
    }

    private void initialiseListeners() {
        //Date pattern changing: dd/MM to MM/dd and vice versa via a toggle button
        datePatternButton.setOnClickListener(v -> changeDatePattern());

        //If the daily notification switch is checked, enable notifications, else disable them
        dailyNotificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                enableNotifications();
            } else {
                NotificationHandler.disableNotifications(this);
            }
            sharedPreferences.edit().putBoolean("SendingDailyNotifications", isChecked).apply();
        });

        //Set the time for when the daily notification will be picked using a picker displayed when the button is pressed
        notificationTimePicker.setOnClickListener(v -> {
            int hour = getNotificationHour();
            int minutes = getNotificationMinute();
            timePicker = new TimePickerDialog(this,
                    THEME_HOLO_DARK,
                    (tp, sHour, sMinute) -> {
                        sharedPreferences.edit().putInt("notificationHour", sHour).apply();
                        sharedPreferences.edit().putInt("notificationMinute", sMinute).apply();
                        displayNotificationTime();

                        //Enable notifications again with the newly set hour and minutes if they are toggled on
                        if (sharedPreferences.getBoolean("SendingDailyNotifications", false)) {
                            enableNotifications();
                        }
                    }, hour, minutes, true);
            timePicker.show();
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

    private void enableNotifications() {
        NotificationHandler.enableNotifications(this);
    }

    //Displays the time at which the daily notification is sent
    private void displayNotificationTime() {
        int notificationHour = getNotificationHour(), notificationMinute = getNotificationMinute();
        String notificationTimeText = "Sending daily notifications at ";
        if (notificationHour < 10)
            notificationTimeText += "0";
        notificationTimeText += notificationHour + ":";
        if (notificationMinute < 10)
            notificationTimeText += "0";
        notificationTimeText += notificationMinute;

        notificationTimeTextView.setText(notificationTimeText);
    }

    private int getNotificationHour() {
        return sharedPreferences.getInt("notificationHour", 9);
    }

    private int getNotificationMinute() {
        return sharedPreferences.getInt("notificationMinute", 0);
    }
}