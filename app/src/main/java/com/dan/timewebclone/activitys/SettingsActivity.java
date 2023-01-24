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
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.EmployeeProvider;

public class SettingsActivity extends AppCompatActivity {

    private boolean takePhoto;
    private SwitchCompat switchPhoto;
    private ImageView mImageBack;
    private SharedPreferences sharedPref;
    private DbEmployees dbEmployees;
    private AuthProvider authProvider;
    private EmployeeProvider employeeProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setStatusBarColor();

        switchPhoto = findViewById(R.id.switchPhoto);
        mImageBack = findViewById(R.id.circleImageBack);
        dbEmployees = new DbEmployees(this);
        authProvider = new AuthProvider();
        employeeProvider = new EmployeeProvider();

        takePhoto = dbEmployees.getEmployee(authProvider.getId()).isStateCamera();

        /*sharedPref = getSharedPreferences("datos", MODE_PRIVATE);
        takePhoto = sharedPref.getBoolean("takePhoto", false);*/

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
        if(dbEmployees.updateStateCamera(authProvider.getId(), takePhoto)){
            employeeProvider.updateStateCamera(authProvider.getId(), takePhoto);
        }

        /*SharedPreferences sharedPref = getSharedPreferences("datos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("takePhoto", takePhoto);
        editor.commit();*/
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