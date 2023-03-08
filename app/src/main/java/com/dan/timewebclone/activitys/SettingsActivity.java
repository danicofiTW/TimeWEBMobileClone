package com.dan.timewebclone.activitys;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.dan.timewebclone.R;
import com.dan.timewebclone.db.DbBitacoras;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.db.DbGeocercas;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.BitacoraProvider;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class SettingsActivity extends AppCompatActivity {

    private boolean takePhoto;
    private SwitchCompat switchPhoto, switchBiometrics;
    private ImageView mImageBack;
    private Button btnSaveSettings;
    private SharedPreferences sharedPref;
    private DbEmployees dbEmployees;
    private DbChecks dbChecks;
    private DbGeocercas dbGeocercas;
    private DbBitacoras dbBitacoras;
    private AuthProvider authProvider;
    private EmployeeProvider employeeProvider;
    private Employee employee;
    private AlertDialog.Builder builderDialogSettingsLock;
    private int canUseBiometrics;
    private boolean stateBiometrics;
    private boolean loginNotData;
    private boolean geocercas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setStatusBarColor();

        switchPhoto = findViewById(R.id.switchPhoto);
        mImageBack = findViewById(R.id.circleImageBack);
        switchBiometrics = findViewById(R.id.switchBiometrics);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        dbEmployees = new DbEmployees(this);
        authProvider = new AuthProvider();
        employeeProvider = new EmployeeProvider();
        dbChecks = new DbChecks(this);
        dbGeocercas = new DbGeocercas(this);
        dbBitacoras = new DbBitacoras(this);
        builderDialogSettingsLock = new AlertDialog.Builder(this);

        loginNotData = getIntent().getBooleanExtra("loginNotData",false);
        geocercas = getIntent().getBooleanExtra("geocercas",false);

        employee = dbEmployees.getEmployee(authProvider.getId());
        //Si el usuario logeado no existe en db se actualiza la informacion
        if(employee == null){
            reviewEmployee();
        } else {
            takePhoto = dbEmployees.getEmployee(authProvider.getId()).isStateCamera();
            stateBiometrics = dbEmployees.getEmployee(authProvider.getId()).isStateBiometrics();
        }

        reviewBiometrics();
        if(takePhoto){
            switchPhoto.setChecked(true);
        }
        if(stateBiometrics){
            switchBiometrics.setChecked(true);
        }

        switchBiometrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(canUseBiometrics == 1){
                    Toast.makeText(SettingsActivity.this, "No se cuenta con biometria", Toast.LENGTH_SHORT).show();
                } else if(canUseBiometrics == 2){
                   mostrarSettings();
                }
            }
        });

        switchBiometrics.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                reviewBiometrics();
                if(canUseBiometrics != 0){
                    switchBiometrics.setChecked(false);
                }
            }
        });

        mImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(loginNotData){
                    if(geocercas){
                        Intent in = new Intent(SettingsActivity.this, GeocercasActivity.class);
                        in.putExtra("notComeBack", true);
                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(in);
                    } else {
                        Intent in = new Intent(SettingsActivity.this, HomeTW.class);
                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(in);
                    }
                } else {
                    finish();
                }
            }
        });

        btnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTakePhoto(switchPhoto.isChecked());
                saveUseBiometrics(switchBiometrics.isChecked());
                if(loginNotData){
                    if(geocercas){
                        Intent in = new Intent(SettingsActivity.this, GeocercasActivity.class);
                        in.putExtra("notComeBack", true);
                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(in);
                    } else {
                        Intent in = new Intent(SettingsActivity.this, HomeTW.class);
                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(in);
                    }
                } else {
                    finish();
                }
            }
        });

    }


    private void reviewBiometrics() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG |BIOMETRIC_WEAK)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                //Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                canUseBiometrics = 0;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                //Log.e("MY_APP_TAG", "No biometric features available on this device.");
                stateBiometrics = false;
                saveUseBiometrics(stateBiometrics);
                canUseBiometrics = 1;
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                //Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                stateBiometrics = false;
                saveUseBiometrics(stateBiometrics);
                canUseBiometrics = 1;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                stateBiometrics = false;
                saveUseBiometrics(stateBiometrics);
                canUseBiometrics = 2;
                break;
        }
    }

    //Guardar imagen obligatoria en registro
    private void saveTakePhoto(boolean takePhoto){
        if(dbEmployees.updateStateCamera(authProvider.getId(), takePhoto)){
            employeeProvider.updateStateCamera(authProvider.getId(), takePhoto);
        }
    }

    private void saveUseBiometrics(boolean stateBiometrics){
        if(dbEmployees.updateStateBiometrics(authProvider.getId(), stateBiometrics)){
            employeeProvider.updateStateBiometrics(authProvider.getId(), stateBiometrics);
        }
    }

    private void reviewEmployee() {
            employeeProvider.getUserInfo(authProvider.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if (task.getResult() != null) {
                            if (task.getResult().exists()) {
                                employee = task.getResult().toObject(Employee.class);
                                if(dbEmployees.deleteAllEmployees() && dbChecks.deleteAllChecks() && dbBitacoras.deleteAllBitacoras() && dbGeocercas.deleteAllGeocercas()){
                                    takePhoto = employee.isStateCamera();
                                    stateBiometrics = employee.isStateBiometrics();
                                    dbEmployees.insertEmployye(employee);
                                }
                            }
                        }
                    }
                }
            });
    }

    public void mostrarSettings(){
        builderDialogSettingsLock.setMessage("No se cuenta con biometria en el dispositivo, ¿Deseas agregar alguna huella o rostro?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if(canUseBiometrics != 1){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                startActivity(new Intent(Settings.ACTION_BIOMETRIC_ENROLL));
                            }
                            else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    startActivity(new Intent(Settings.ACTION_FINGERPRINT_ENROLL));
                                }
                                else {
                                    startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
                                }
                            }
                        }
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setNeutralButton("", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        builderDialogSettingsLock.show();
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