package com.dan.timewebclone.services;

import static androidx.legacy.content.WakefulBroadcastReceiver.startWakefulService;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.legacy.content.WakefulBroadcastReceiver;

import com.dan.timewebclone.R;
import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.activitys.MainActivity;
import com.dan.timewebclone.activitys.ShowLocationActivity;
import com.dan.timewebclone.activitys.ShowNotificationActivity;
import com.dan.timewebclone.channel.NotificationHelper;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.receivers.AcceptReceiver;
import com.dan.timewebclone.receivers.CancelReceiver;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class FirebaseBackgroundService extends BroadcastReceiver {

    private static final String TAG = "FirebaseServiceBackGround";
    private static final int NOTIFICATION_CODE = 110;
    AuthProvider authProvider;
    Context myContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "APP in background!!!");
        myContext = context;
        authProvider = new AuthProvider();

        String title = null;
        String idCheck = null;
        String body = null;
        String idUser = null;
        String url = null;
        String image = null;


        if (intent.getExtras() != null) {
            title = intent.getExtras().getString("gcm.notification.title");
            body = intent.getExtras().getString("gcm.notification.body");
            url = intent.getExtras().getString("url");
            image = intent.getExtras().getString("image");
            idCheck = intent.getExtras().getString("idCheck");
            idUser = authProvider.getId();
            if(url == null){
                url = intent.getExtras().getString("urlNotify");
            }
            if(title == null){
                title = intent.getExtras().getString("titleNotify");
            }
            if(body == null){
                body = intent.getExtras().getString("bodyNotify");
            }
        }

        NotificationManager notificationManager = (NotificationManager) myContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (title != null) {
            HomeTW.updateStatusActivity(true);
            //notificationManager.cancel(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (title.contains("REGISTROS")) {
                    notificationManager.cancel(2);
                    showNotificationApiOreoShowCheck(title, body, idUser, idCheck);
                } else if (url!=null || image != null){
                    notificationManager.cancel(3);
                    //showNotificationActivity(title, body, idUser, image, url);
                    MainActivity.updateNotify(title, body, url, image, idUser);
                    MainActivity.notify = false;
                    ShowNotificationActivity.updateStatusActivity(true);
                } else {
                    notificationManager.cancel(4);
                }
            } else {
                if (title.contains("REGISTROS")) {
                    notificationManager.cancel(2);
                    showNotificationShowCheck(title, body, idUser, idCheck);
                    //showNotificationActivity(idClient, origin, destination, min, distance);
                } else if (url != null || image != null){
                    notificationManager.cancel(3);
                    ShowNotificationActivity.updateStatusActivity(true);
                    MainActivity.notify = false;
                    MainActivity.updateNotify(title, body, url, image, idUser);
                    //showNotificationActivity(title, body, idUser, image, url);
                    //showNotificationActionView(title, body, idUser, image, url);
                    //showNotificationActivity(title, body, idUser, image, url);
                } else {
                    notificationManager.cancel(4);
                    //showNotification(title, body);
                }
            }
        }

    }


    private void showNotification(String title, String body) {
        PendingIntent intent = PendingIntent.getActivity(myContext, 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper notificationHelper = new NotificationHelper(myContext);
        NotificationCompat.Builder builder = notificationHelper.getNotificationOldApi(title, body, intent, sound);
        notificationHelper.getManager().notify(4, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreo(String title, String body) {
        PendingIntent intent = PendingIntent.getActivity(myContext, 0, new Intent(), PendingIntent.FLAG_ONE_SHOT|PendingIntent.FLAG_MUTABLE);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper notificationHelper = new NotificationHelper(myContext);
        Notification.Builder builder = notificationHelper.getNotification(title, body, intent, sound);
        notificationHelper.getManager().notify(4, builder.build());
    }



    private void showNotificationShowCheck(String title, String body, String idUser, String idCheck) {
        Intent viewCheckIntent = new Intent(myContext, ShowLocationActivity.class);
        viewCheckIntent.putExtra("idUser", idUser);
        viewCheckIntent.putExtra("idCheck", idCheck);
        viewCheckIntent.putExtra("title", title);

        PendingIntent viewCheckPendingIntent = PendingIntent.getActivity(myContext, NOTIFICATION_CODE, viewCheckIntent, PendingIntent.FLAG_UPDATE_CURRENT );

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(myContext);
        NotificationCompat.Builder builder = notificationHelper.getNotificationOldApi(title, body, viewCheckPendingIntent, sound);
        notificationHelper.getManager().notify(2, builder.build());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreoShowCheck(String title, String body, String idUser,String idCheck) {

        Intent viewCheckIntent = new Intent(myContext, ShowLocationActivity.class);
        viewCheckIntent.putExtra("idUser", idUser);
        viewCheckIntent.putExtra("idCheck", idCheck);
        viewCheckIntent.putExtra("title", title);
        PendingIntent viewCheckPendingIntent = PendingIntent.getActivity(myContext, NOTIFICATION_CODE, viewCheckIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(myContext);
        Notification.Builder builder = notificationHelper.getNotification(title, body, viewCheckPendingIntent, sound);
        notificationHelper.getManager().notify(2, builder.build());
    }

    private void showNotificationActivity(String title, String body, String idUser, String image, String url) {

        PowerManager pm = (PowerManager) myContext.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        if(!isScreenOn){
            PowerManager.WakeLock wakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.ON_AFTER_RELEASE,"AppName:MyLock"
            );
            wakeLock.acquire(10000);
        }
        Intent intent1 = new Intent(myContext, ShowNotificationActivity.class);
        intent1.putExtra("idUser", idUser);
        intent1.putExtra("title", title);
        intent1.putExtra("body", body);
        intent1.putExtra("image", image);
        intent1.putExtra("url", url);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        myContext.startActivity(intent1);
    }





}
