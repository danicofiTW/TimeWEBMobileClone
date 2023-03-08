package com.dan.timewebclone.activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.RenderMode;
import com.bumptech.glide.Glide;
import com.dan.timewebclone.R;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.utils.Utils;

import java.lang.ref.WeakReference;

public class ShowNotificationActivity extends AppCompatActivity {


    private static WeakReference<Activity> mActivityRef;
    private static boolean activityActive = false;
    private TextView textViewTitle, textViewBody, textViewName;
    private ImageView imageViewNotify, imageViewBack;
    private Button btnGoApp;
    private String idUser, title, body, image, url;
    private LottieAnimationView animation;

    private DbEmployees dbEmployees;
    private boolean isMain = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_notification);
        setStatusBarColor();

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewBody = findViewById(R.id.textViewBody);
        textViewName = findViewById(R.id.textViewName);
        imageViewNotify = findViewById(R.id.imageViewNotify);
        imageViewBack = findViewById(R.id.imageViewBack);
        btnGoApp = findViewById(R.id.btnGoApp);
        animation = findViewById(R.id.animationShowNotify);
        animation.isHardwareAccelerated();
        animation.setRenderMode(RenderMode.HARDWARE);

        dbEmployees = new DbEmployees(this);

        idUser = getIntent().getStringExtra("idUser");
        title = getIntent().getStringExtra("title");
        body = getIntent().getStringExtra("body");
        image = getIntent().getStringExtra("image");
        url = getIntent().getStringExtra("url");
        isMain = getIntent().getBooleanExtra("main", false);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        if(idUser!=null){
            if(dbEmployees.getEmployee(idUser) != null){
                textViewName.setText(dbEmployees.getEmployee(idUser).getName()+":");
            }
        }
        if(title!=null){
            textViewTitle.setText(title);
        }
        if(body!=null){
            textViewBody.setText(body);
        }
        if(image != null){
            try{
                byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                //RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), decodedByte);
                if(decodedByte != null){
                    imageViewNotify.setImageBitmap(decodedByte);
                } else {
                    if(url != null){
                        Glide.with(this).load(url).into(imageViewNotify);
                    } else {
                        imageViewNotify.setImageResource(R.drawable.ic_broken_image_white);
                    }
                }
            }
            catch(Exception e){
                //e.getMessage();
                if(url != null){
                    Glide.with(this).load(url).into(imageViewNotify);
                } else {
                    imageViewNotify.setImageResource(R.drawable.ic_broken_image_white);
                }
            }
        } else if(url != null){
            Glide.with(this).load(url).into(imageViewNotify);
        } else {
            imageViewNotify.setImageResource(R.drawable.ic_broken_image_white);
        }

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.notify = true;
                HomeTW.updateStatusActivity(false);
                finish();
            }
        });

        //btnGoApp.setVisibility(View.GONE);
        btnGoApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.notify = true;
                HomeTW.updateStatusActivity(false);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(3);
                //Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.dan.timewebclone");
                if(mActivityRef != null && mActivityRef.get() != null){
                    if(mActivityRef.get().isDestroyed()) {
                        Intent intent1 = new Intent(ShowNotificationActivity.this, MainActivity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent1.setAction(Intent.ACTION_RUN);
                        startActivity(intent1);
                    } else {
                        if(activityActive){
                            finish();
                        } else {
                            /*Intent intent1 = new Intent(ShowNotificationActivity.this, MainActivity.class);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent1.setAction(Intent.ACTION_RUN);
                            startActivity(intent1);*/
                            Intent openMainActivity = new Intent(ShowNotificationActivity.this, HomeTW.class);
                            openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivityIfNeeded(openMainActivity, 0);
                        }
                    }
                } else {
                    //if(isMain){
                        finish();
                   /* } else {
                        Intent intent1 = new Intent(ShowNotificationActivity.this, MainActivity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent1.setAction(Intent.ACTION_RUN);
                        startActivity(intent1);
                    }*/
                }
            }
        });

    }

    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorHomeTw, this.getTheme()));
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorHomeTw));
        }
    }

    public static void updateActivity(Activity activity) {
        mActivityRef = new WeakReference<Activity>(activity);
    }

    public static void updateStatusActivity(boolean b) {
        activityActive = b;
    }
}