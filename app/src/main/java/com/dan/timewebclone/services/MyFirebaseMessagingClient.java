package com.dan.timewebclone.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.dan.timewebclone.R;
import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.activitys.MainActivity;
import com.dan.timewebclone.activitys.ShowLocationActivity;
import com.dan.timewebclone.activitys.ShowNotificationActivity;
import com.dan.timewebclone.channel.NotificationHelper;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.receivers.AcceptReceiver;
import com.dan.timewebclone.receivers.CancelReceiver;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingClient extends FirebaseMessagingService {

    private static final int NOTIFICATION_CODE = 110;
    AuthProvider authProvider;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();
        authProvider = new AuthProvider();

        String title = data.get("title");
        String idCheck = data.get("idCheck");
        String body = data.get("body");
        String idUser = data.get("idUser");
        String url = data.get("urlNotify");
        //String remitente = data.get("remitente");
        String image = data.get("image");
        if(title == null){
            title = remoteMessage.getNotification().getTitle();
        }
        if(body == null){
            body = remoteMessage.getNotification().getBody();
        }
        if(idUser == null){
            idUser = authProvider.getId();
        }
        if(image == null){
            //image = remoteMessage.getNotification().getImageUrl()+"";
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (title != null) {
            HomeTW.updateStatusActivity(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (title.contains("REGISTROS")) {
                    notificationManager.cancel(2);
                    showNotificationApiOreoShowCheck(title, body, idUser, idCheck);
                    //showNotificationActivity(idClient, origin, destination, min, distance);
                } else if (url!=null || image != null){
                    //NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    //notificationManager.cancel(3);
                    showNotificationApiOreoViewNotify(title, body, idUser, image, url);
                    showNotificationActivity(title, body, idUser, image, url);
                } else {
                    //notificationManager.cancel(4);
                    showNotificationApiOreo(title, body);
                }
            } else {
                if (title.contains("REGISTROS")) {
                    //String idUser = data.get("idUser");
                    notificationManager.cancel(2);
                    showNotificationShowCheck(title, body, idUser, idCheck);
                    //showNotificationActivity(idClient, origin, destination, min, distance);
                } else if (url != null || image != null){
                    //NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    //notificationManager.cancel(3);
                    showNotificationViewNotify(title, body, idUser, image, url);
                    showNotificationActivity(title, body, idUser, image, url);
                } else {
                    //notificationManager.cancel(4);
                    showNotification(title, body);
                }
            }
        }
    }

    /*@Override
    public final void handleIntent(Intent intent) {
        this.onMessageReceived(builder.build());
    }*/

    private void showNotification(String title, String body) {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), PendingIntent.FLAG_ONE_SHOT|PendingIntent.FLAG_IMMUTABLE);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotificationOldApi(title, body, intent, sound);
        notificationHelper.getManager().notify(4, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreo(String title, String body) {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), PendingIntent.FLAG_ONE_SHOT|PendingIntent.FLAG_IMMUTABLE);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotification(title, body, intent, sound);
        notificationHelper.getManager().notify(4, builder.build());
    }

    private void showNotificationShowCheck(String title, String body, String idUser, String idCheck) {
        // ACEPTAR
        //[[UIApplication sharedApplication] registerUserNotificationSettings:[self createUserNotificationSettings]];
        Intent viewCheckIntent = new Intent(this, ShowLocationActivity.class);
        viewCheckIntent.putExtra("idUser", idUser);
        viewCheckIntent.putExtra("idCheck", idCheck);
        viewCheckIntent.putExtra("title", title);

        PendingIntent viewCheckPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_CODE, viewCheckIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE );

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotificationOldApi(title, body, viewCheckPendingIntent, sound);
        notificationHelper.getManager().notify(2, builder.build());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreoShowCheck(String title, String body, String idUser,String idCheck) {

        Intent viewCheckIntent = new Intent(this, ShowLocationActivity.class);
        viewCheckIntent.putExtra("idUser", idUser);
        viewCheckIntent.putExtra("idCheck", idCheck);
        viewCheckIntent.putExtra("title", title);
        PendingIntent viewCheckPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_CODE, viewCheckIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotification(title, body, viewCheckPendingIntent, sound);
        notificationHelper.getManager().notify(2, builder.build());
    }

    private void showNotificationViewNotify(String title, String body, String idUser, String image, String url) {
        // ACEPTAR
        Intent showIntent = new Intent(this, ShowNotificationActivity.class);
        showIntent.putExtra("idUser", idUser);
        showIntent.putExtra("title", title);
        showIntent.putExtra("body", body);
        showIntent.putExtra("image", image);
        showIntent.putExtra("url", url);
        PendingIntent showPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_CODE, showIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE );

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotificationOldApi(title, body, showPendingIntent, sound);
        notificationHelper.getManager().notify(3, builder.build());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreoViewNotify(String title, String body, String idUser, String image, String url) {

        Intent showIntent = new Intent(this, ShowNotificationActivity.class);
        showIntent.putExtra("idUser", idUser);
        showIntent.putExtra("title", title);
        showIntent.putExtra("body", body);
        showIntent.putExtra("image", image);
        showIntent.putExtra("url", url);
        PendingIntent showPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_CODE, showIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotification(title, body, showPendingIntent, sound);
        notificationHelper.getManager().notify(3, builder.build());
    }


    private void showNotificationActivity(String title, String body, String idUser, String image, String url) {

        PowerManager pm = (PowerManager) getBaseContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        if(!isScreenOn){
            PowerManager.WakeLock wakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.ON_AFTER_RELEASE,"AppName:MyLock"
            );
            wakeLock.acquire(10000);
        }
        Intent intent1 = new Intent(getBaseContext(), ShowNotificationActivity.class);
        intent1.putExtra("idUser", idUser);
        intent1.putExtra("title", title);
        intent1.putExtra("body", body);
        intent1.putExtra("image", image);
        intent1.putExtra("url", url);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent1);
    }



}
