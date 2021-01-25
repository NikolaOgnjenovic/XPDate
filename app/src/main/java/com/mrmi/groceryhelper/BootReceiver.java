package com.mrmi.groceryhelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            /*Intent i = new Intent(context, MyService.class);
            context.startService(i);
            */
            ArticleList articleList = new ArticleList(context);
            //articleList.SetNotifications();

            Toast.makeText(context, "Grocery booting completed", Toast.LENGTH_SHORT).show();
        }
    }
}
