package com.dan.timewebclone.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CancelReceiver extends BroadcastReceiver {

   // private ClientBookingProvider mClientBookingProvider;

        @Override
        public void onReceive(Context context, Intent intent) {
                String idCheck = intent.getExtras().getString("idCheck");
                String image = intent.getExtras().getString("image");
                String url = intent.getExtras().getString("url");
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if(idCheck != null){
                        manager.cancel(2);
                } else if(image != null || url != null){
                        manager.cancel(3);
                } else {
                        manager.cancel(1);
                }
        }

}
