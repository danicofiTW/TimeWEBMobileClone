package com.dan.timewebclone.activitys;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import android.widget.Toast;

import com.dan.timewebclone.R;
import com.dan.timewebclone.db.DbBitacoras;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.fragments.LoginFragment;
import com.dan.timewebclone.fragments.TermsAndConditionsFragment;
import com.dan.timewebclone.fragments.RegisterFragment;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private Button btnGoToRegister, btnGoToLogin;
    private AlertDialog.Builder builderDialogExit;

    private LoginFragment loginFragment;
    private RegisterFragment registerFragment;

    private FragmentTransaction fragmentTransaction;
    private FragmentManager fragmentManager;
    private DialogFragment termsAndConditionsFragment;
    private DbEmployees dbEmployees;
    private DbBitacoras dbBitacoras;

    private AuthProvider mAuth = null;

    private String changePassword;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private int canUseBiometrics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setStatusBarColor();
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        loginFragment = new LoginFragment();
        registerFragment = new RegisterFragment();
        dbEmployees = new DbEmployees(this);
        dbBitacoras = new DbBitacoras(this);
        termsAndConditionsFragment = new TermsAndConditionsFragment();
        mAuth = new AuthProvider();
        builderDialogExit = new AlertDialog.Builder(this);

        //Cachar variable que se entrega al cerrar sesion y al cambiar password
        changePassword = getIntent().getStringExtra("ChangePassword");
        if (changePassword != null) {
            moveFragment(loginFragment);
            if(dbEmployees.getEmployee(mAuth.getId())!=null)
            loginFragment.setTextEmail(dbEmployees.getEmployee(mAuth.getId()).getEmail());
        }

        //Ir a registrarte
        btnGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveFragment(registerFragment);
                termsAndConditionsFragment.show(fragmentManager, "TermsAndConditionsFragment");
            }
        });

        //Iniciar sesion
        btnGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveFragment(loginFragment);
            }
        });


        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                    moveFragment(loginFragment);
                    loginFragment.setTextEmail(dbEmployees.getEmployee(mAuth.getId()).getEmail());

                //Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                //Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                if(dbBitacoras.getBitacorasByIdUser(mAuth.getId()).size() != 0){
                    Intent intent = new Intent(MainActivity.this, GeocercasActivity.class);
                    intent.putExtra("notComeBack", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, HomeTW.class);
                    intent.putExtra("revieEmployee", false);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                //if(!loginFragment.isHidden()){
                    moveFragment(loginFragment);
                    loginFragment.setTextEmail(dbEmployees.getEmployee(mAuth.getId()).getEmail());
                //}
                //Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Iniciar sesión")
                .setSubtitle("Verifique su identidad para iniciar sesión")
                //.setNegativeButtonText("Usar contraseña")
                .setConfirmationRequired(true)
                .setAllowedAuthenticators(BIOMETRIC_STRONG|BIOMETRIC_WEAK|DEVICE_CREDENTIAL)
                .build();

        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG |BIOMETRIC_WEAK| DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                //Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                canUseBiometrics = 0;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                //Log.e("MY_APP_TAG", "No biometric features available on this device.");
                canUseBiometrics = 1;
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                //Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                canUseBiometrics = 1;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                canUseBiometrics = 2;
                break;
        }

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
        /*Button biometricLoginButton = findViewById(R.id.biometric_login);
        biometricLoginButton.setOnClickListener(view -> {
        });*/


    }

    @Override
    protected void onStart() {
        super.onStart();
        //Si el usuario ya inicio sesion ingresar al Home
        if (mAuth.getId() != null && dbEmployees.getEmployee(mAuth.getId()) != null) {
            if(canUseBiometrics == 0){
                if(dbEmployees.getEmployee(mAuth.getId()).isStateBiometrics()){
                    biometricPrompt.authenticate(promptInfo);
                } else {
                    moveFragment(loginFragment);
                    loginFragment.setTextEmail(dbEmployees.getEmployee(mAuth.getId()).getName());
                }
            } else {
                /*if(dbBitacoras.getBitacorasByIdUser(mAuth.getId()).size() != 0){
                    Intent intent = new Intent(MainActivity.this, GeocercasActivity.class);
                    intent.putExtra("notComeBack", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, HomeTW.class);
                    intent.putExtra("revieEmployee", false);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                Intent i = new Intent(MainActivity.this, HomeTW.class);
                i.putExtra("revieEmployee", false);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);*/
                moveFragment(loginFragment);
                loginFragment.setTextEmail(dbEmployees.getEmployee(mAuth.getId()).getName());
            }
        }
    }

    //Cambiar fragment a mostrar
    private void moveFragment(Fragment fragment) {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if (fragment.isHidden()) {
            setBtns(false);
            fragmentTransaction.show(fragment).commit();
        } else {
            setBtns(false);
            if(!fragment.isAdded())
            fragmentTransaction.add(R.id.frameLayoutMain, fragment).addToBackStack(null).commit();
        }
    }


    //Accion hacia atras del dispositivo
    @Override
    public void onBackPressed() {
        setBtns(true);
        if (!registerFragment.isVisible() && !loginFragment.isVisible()) {
            mostrarSalida();
        } else {
            super.onBackPressed();
        }
    }

    //Mensaje de salida de la app
    public void mostrarSalida(){
        builderDialogExit.setMessage("¿Deseas salir de TimeWEBMobile?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Intent in = new Intent(Intent.ACTION_MAIN);
                        in.addCategory(Intent.CATEGORY_HOME);
                        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(in);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builderDialogExit.show();
    }

    //Inhabilitar botones del main
    public void setBtns(boolean clickBottom){
        if(clickBottom){
            btnGoToLogin.setEnabled(true);
            btnGoToRegister.setEnabled(true);
        } else {
            btnGoToLogin.setEnabled(false);
            btnGoToRegister.setEnabled(false);
        }
    }

    //Cambiar el color de la barra de notificaciones
    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorNotificationToolbarMain, this.getTheme()));
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorNotificationToolbarMain));
        }
    }


}