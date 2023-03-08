package com.dan.timewebclone.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

import com.dan.timewebclone.R;
import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.activitys.MainActivity;
import com.dan.timewebclone.activitys.ShowLocationActivity;
import com.dan.timewebclone.activitys.ShowLocationHuaweiActivity;
import com.dan.timewebclone.activitys.ShowNotificationActivity;
import com.dan.timewebclone.channel.NotificationHelper;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.utils.Utils;


public class AcceptReceiver extends BroadcastReceiver {


    private static final int NOTIFICATION_CODE = 110;

    @Override
    public void onReceive(Context context, Intent intent) {
        //String idUser = intent.getExtras().getString("idUser");
        String idCheck = intent.getExtras().getString("idCheck");
        String idUser = intent.getExtras().getString("idUser");
        String title = intent.getExtras().getString("title");
        String body = intent.getExtras().getString("body");
        String image = intent.getExtras().getString("image");
        String url = intent.getExtras().getString("url");


        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        if(idCheck!=null){
            DbChecks dbChecks = new DbChecks(context);
            Check check = dbChecks.getCheck(idCheck);
            if(check!=null)
                manager.cancel(2);
                goToShowCheck(context,check);
        } else if(image!= null || url != null){
            manager.cancel(3);
            goToShowNotify(context,idUser,title,body,image,url);
        }


    }

    private void goToShowCheck(Context context, Check check){
        if(Utils.isGMS(context)){

            /*PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();
            if(!isScreenOn){
                PowerManager.WakeLock wakeLock = pm.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK |
                                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                PowerManager.ON_AFTER_RELEASE,"AppName:MyLock"
                );
                wakeLock.acquire(10000);
            }*/

            Intent intent1 = new Intent(context, ShowLocationActivity.class);
            intent1.putExtra("lat", check.getCheckLat());
            intent1.putExtra("lng", check.getCheckLong());
            intent1.putExtra("date", check.getTime());
            intent1.putExtra("tipe", check.getTipeCheck());
            intent1.putExtra("showForNotify", true);
            intent1.putExtra("idCheck",check.getIdCheck());
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            context.startActivity(intent1);

        } else {
            Intent intent1 = new Intent(context, ShowLocationHuaweiActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //intent1.setAction(Intent.ACTION_RUN);
            //intent1.putExtra("idUser",idUser);
            intent1.putExtra("lat", check.getCheckLat());
            intent1.putExtra("lng", check.getCheckLong());
            intent1.putExtra("date", check.getTime());
            intent1.putExtra("tipe", check.getTipeCheck());
            intent1.putExtra("showForNotify", true);
            intent1.putExtra("idCheck",check.getIdCheck());

            context.startActivity(intent1);
        }
    }

    private void goToShowNotify(Context context, String idUser, String title, String body, String image, String url){
        if(Utils.isGMS(context)){

            Intent intent1 = new Intent(context, ShowNotificationActivity.class);
            //.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent1.putExtra("idUser", idUser);
            intent1.putExtra("titleNotify", title);
            intent1.putExtra("bodyNotify", body);
            intent1.putExtra("image", image);
            intent1.putExtra("urlNotify", url);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //context.startActivity(intent1);
            intent1.setAction("android.intent.action.MAIN");

            intent1.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(intent1);
            //PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_ONE_SHOT);

        } else {
            Intent intent1 = new Intent(context, ShowNotificationActivity.class);
            //intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent1.putExtra("idUser", idUser);
            intent1.putExtra("title", title);
            intent1.putExtra("body", body);
            intent1.putExtra("image", image);
            intent1.putExtra("url", url);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent1);
        }
    }
}
