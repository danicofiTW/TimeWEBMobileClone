package com.dan.timewebclone.activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.dan.timewebclone.R;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.models.Check;
import com.squareup.picasso.Picasso;

import java.nio.charset.StandardCharsets;

public class ShowImageActivity extends AppCompatActivity {

    private ImageView imageViewBackShow, imageViewPictureShow;
    private String mExtraId;
    private Check check;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        setStatusBarColor();

        imageViewBackShow = findViewById(R.id.imageViewBackShow1);
        imageViewPictureShow = findViewById(R.id.imageViewPictureShow);
        mExtraId = getIntent().getStringExtra("idCheck");

        DbChecks dbChecks = new DbChecks(ShowImageActivity.this);
        check = dbChecks.getCheck(mExtraId);

        getImage();

        imageViewBackShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    //Mostrar imagen
    private void getImage() {
        if(check.getUrlImage() != null){
            Uri uri;
            uri = Uri.parse(check.getUrlImage());
            ContentResolver contentResolver = getContentResolver();
            Bitmap bitmap = null;
            try {
                if(Build.VERSION.SDK_INT < 28) {
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);
                } else {
                    ImageDecoder.Source source = ImageDecoder.createSource(contentResolver, uri);
                    bitmap = ImageDecoder.decodeBitmap(source);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(uri != null && bitmap != null){
                Glide.with(ShowImageActivity.this).load(uri).into(imageViewPictureShow);
                //imageViewPictureShow.setImageURI(uri);
            } else {
                try {
                    byte[] decodedString = Base64.decode(check.getImage(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    if (decodedByte != null) {
                        imageViewPictureShow.setImageBitmap(decodedByte);
                    }
                } catch(Exception e){
                    e.getMessage();
                }
            }
        } else if(check.getImage() != null){
            try {
                byte[] decodedString = Base64.decode(check.getImage(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedByte != null) {
                    imageViewPictureShow.setImageBitmap(decodedByte);
                }
            } catch(Exception e){
                e.getMessage();
            }
        }
    }

    //Cambiar el color de la barra de notificaciones
    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorHomeTw, this.getTheme()));
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorHomeTw));
        }
    }

}