package com.dan.timewebclone.activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.dan.timewebclone.R;

public class SettingsActivity extends AppCompatActivity {

    private boolean takePhoto;
    private SwitchCompat switchPhoto;
    private ImageView mImageBack;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setStatusBarColor();

        switchPhoto = findViewById(R.id.switchPhoto);
        mImageBack = findViewById(R.id.circleImageBack);

        sharedPref = getSharedPreferences("datos", MODE_PRIVATE);
        takePhoto = sharedPref.getBoolean("takePhoto", false);

        if(takePhoto){
            switchPhoto.setChecked(true);
        }

        switchPhoto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked ) {
                if(isChecked){
                    takePhoto = true;
                    savePreference(takePhoto);
                } else{
                    takePhoto = false;
                    savePreference(takePhoto);
                }
            }
        });


        mImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    //Guardar imagen obligatoria en registro
    private void savePreference(boolean takePhoto){
        SharedPreferences sharedPref = getSharedPreferences("datos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("takePhoto", takePhoto);
        editor.commit();
    }

    //Cambiar el color de la barra de notificaciones
    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black, this.getTheme()));
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
    }

}