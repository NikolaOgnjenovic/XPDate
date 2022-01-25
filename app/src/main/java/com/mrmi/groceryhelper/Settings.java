package com.mrmi.groceryhelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import java.util.Locale;

public class Settings extends AppCompatActivity {

    private ArticleList articleListClass;
    private SharedPreferences sharedPreferences;
    private Button datePatternButton, notificationTimePicker, changeLanguageButton;
    private String datePattern;
    private SwitchCompat dailyNotificationSwitch;
    private TimePickerDialog timePicker;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initialiseViews();
        initialiseObjects();
        initialiseListeners();

        displaySelectedDatePattern();
        displayNotificationTime();
    }

    //Launch main activity on back pressed
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void initialiseViews() {
        datePatternButton = findViewById(R.id.datePatternButton);
        dailyNotificationSwitch = findViewById(R.id.dailyNotificationSwitch);
        notificationTimePicker = findViewById(R.id.setNotificationTime);
        changeLanguageButton = findViewById(R.id.changeLanguageButton);
    }

    private void initialiseObjects() {
        articleListClass = new ArticleList(this);
        sharedPreferences = getSharedPreferences("Shared preferences", MODE_PRIVATE);

        //Enable and disable daily notifications using the switch
        dailyNotificationSwitch.setChecked(sharedPreferences.getBoolean("SendingDailyNotifications", true));
        if (dailyNotificationSwitch.isChecked()) {
            enableNotifications();
        }
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
        notificationTimePicker.setOnClickListener(v -> displayNotificationTimePicker());

        changeLanguageButton.setOnClickListener(v -> showChangeLanguageDialog());
    }

    //Loads the saved date pattern using the ArticleList class and sets the pattern button text accordingly
    private void displaySelectedDatePattern() {
        datePattern = articleListClass.getDatePattern();

        String buttonText = getString(R.string.date_info) + "\n" + patternLocale();
        datePatternButton.setText(buttonText);

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
        String buttonText = getString(R.string.date_info) + "\n" + patternLocale();
        datePatternButton.setText(buttonText);
        articleListClass.setDatePattern(datePattern);
    }

    private String patternLocale() {
        if(datePattern.equals("MM/dd"))
            return getString(R.string.month_day_pattern);
        return getString(R.string.day_month_pattern);
    }

    private void enableNotifications() {
        NotificationHandler.enableNotifications(this);
    }

    private void displayNotificationTimePicker() {
        int hour = getNotificationHour();
        int minutes = getNotificationMinute();
        //Enable notifications again with the newly set hour and minutes if they are toggled on
        if(timePicker!=null) {
            timePicker.dismiss();
        }

        timePicker = new TimePickerDialog(this,
                //THEME_HOLO_LIGHT,
                R.style.TimePicker,
                (tp, sHour, sMinute) -> {
                    sharedPreferences.edit().putInt("notificationHour", sHour).apply();
                    sharedPreferences.edit().putInt("notificationMinute", sMinute).apply();
                    displayNotificationTime();

                    //Enable notifications again with the newly set hour and minutes if they are toggled on
                    if (sharedPreferences.getBoolean("SendingDailyNotifications", false)) {
                        enableNotifications();
                    }
                }, hour, minutes, true);

        timePicker.setOnShowListener(dialogInterface -> {
            timePicker.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), timePicker);
            timePicker.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            timePicker.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), timePicker);
            timePicker.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        });

        timePicker.show();
    }

    //Displays the time at which the daily notification is sent
    private void displayNotificationTime() {
        int notificationHour = getNotificationHour(), notificationMinute = getNotificationMinute();
        String notificationTimeText = "";

        if (notificationHour < 10)
            notificationTimeText += "0";
        notificationTimeText += notificationHour + ":";
        if (notificationMinute < 10)
            notificationTimeText += "0";
        notificationTimeText += notificationMinute;

        String notificationButtonText = getString(R.string.set_notification_time) + "\n(" + notificationTimeText + ")";
        notificationTimePicker.setText(notificationButtonText);
    }

    private int getNotificationHour() {
        return sharedPreferences.getInt("notificationHour", 9);
    }

    private int getNotificationMinute() {
        return sharedPreferences.getInt("notificationMinute", 0);
    }

    private void showChangeLanguageDialog() {
        final String[] languages = {"English", "Српски", "Srpski"};
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.DialogTheme);
        alertDialogBuilder.setTitle(getString(R.string.choose_language));
        alertDialogBuilder.setSingleChoiceItems(languages, -1, (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    setLocale(this, "en");
                    recreate();
                    break;
                case 1:
                    setLocale(this, "sr");
                    recreate();
                    break;
                case 2:
                    setLocale(this, "bs");
                    recreate();
                    break;
            }

            //Dismiss the alert dialog when the user selects a language
            dialogInterface.dismiss();
        });

        if(alertDialog!=null) {
            alertDialog.dismiss();
        }
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static void setLocale(Context context, String selectedLocale) {
        Locale locale = new Locale(selectedLocale);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        context.getSharedPreferences("Shared preferences", MODE_PRIVATE).edit().putString("Selected_locale", selectedLocale).apply();
    }

    public static void loadLocale(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("Shared preferences", MODE_PRIVATE);
        String locale = sharedPrefs.getString("Selected_locale", "bs");
        Settings.setLocale(context, locale);
    }
}