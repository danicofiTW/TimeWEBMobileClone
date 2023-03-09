package com.dan.timewebclone.activitys;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.content.ContentValues.TAG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.RenderMode;
import com.dan.timewebclone.R;
import com.dan.timewebclone.db.DbBitacoras;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.db.DbGeocercas;
import com.dan.timewebclone.fragments.LoginFragment;
import com.dan.timewebclone.fragments.TermsAndConditionsFragment;
import com.dan.timewebclone.fragments.RegisterFragment;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.BitacoraProvider;
import com.dan.timewebclone.services.HmsMessageService;
import com.dan.timewebclone.utils.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Set;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private Button btnGoToRegister, btnGoToLogin;
    private AlertDialog.Builder builderDialogExit;
    private LottieAnimationView animation;

    private LoginFragment loginFragment;
    private RegisterFragment registerFragment;

    private FragmentTransaction fragmentTransaction;
    private FragmentManager fragmentManager;
    private DialogFragment termsAndConditionsFragment;
    private DbEmployees dbEmployees;
    private DbBitacoras dbBitacoras;
    private BitacoraProvider bitacoraProvider;

    private AuthProvider mAuth = null;

    private boolean changePassword;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private int canUseBiometrics;

    static String titleNotify;
    static String bodyNotify;
    static String urlNotify;
    static String imageNotify;
    String idCheckotify;
    static String idUser;
    public static boolean notify;
    boolean biometric = false;

    private AppUpdateManager updateManager;
    private Task<AppUpdateInfo> taskUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setStatusBarColor();
        reviewUpdate();
        //askNotificationPermission();
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);
        animation = findViewById(R.id.animationMain);
        animation.isHardwareAccelerated();
        //animation.enableMergePathsForKitKatAndAbove(true);
        animation.setRenderMode(RenderMode.HARDWARE);
        notify = false;


        loginFragment = new LoginFragment();
        registerFragment = new RegisterFragment();
        dbEmployees = new DbEmployees(this);
        dbBitacoras = new DbBitacoras(this);
        termsAndConditionsFragment = new TermsAndConditionsFragment();
        mAuth = new AuthProvider();
        bitacoraProvider = new BitacoraProvider();
        builderDialogExit = new AlertDialog.Builder(this);

        //Cachar variable que se entrega al cerrar sesion y al cambiar password
        changePassword = getIntent().getBooleanExtra("ChangePassword", false);
        if (changePassword) {
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
                biometric = false;
                moveFragment(loginFragment);
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                //Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                if(dbBitacoras.getBitacorasByIdUser(mAuth.getId()).size() != 0){

                    biometric = false;
                    Intent intent = new Intent(MainActivity.this, GeocercasActivity.class);
                    intent.putExtra("notComeBack", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    if(Utils.isOnlineNet(MainActivity.this)){

                        biometric = false;
                        bitacoraProvider.getBitacorasByUser(mAuth.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot querySnapshot) {
                                if(querySnapshot.size() != 0){
                                    Intent i = new Intent(MainActivity.this, GeocercasActivity.class);
                                    i.putExtra("notComeBack", true);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                } else {
                                    Intent intent = new Intent(MainActivity.this, HomeTW.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            }
                        });
                    } else {

                        biometric = false;
                        Intent intent = new Intent(MainActivity.this, HomeTW.class);
                        intent.putExtra("revieEmployee", false);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                    biometric = false;
                    moveFragment(loginFragment);
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

    private void reviewUpdate() {
        updateManager = AppUpdateManagerFactory.create(this);
        taskUpdate = updateManager.getAppUpdateInfo();
        taskUpdate.addOnSuccessListener(new com.google.android.play.core.tasks.OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                if(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE){
                    if(getLifecycle().getCurrentState() == Lifecycle.State.RESUMED){
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Actualización disponible !!");
                        builder.setMessage("Se encuentra una nueva version de timeWEBMobile, actualiza para continuar");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    Uri uri = Uri.parse("market://details?id="+getPackageName());
                                    intent.setData(uri);
                                    intent.setPackage("com.android.vending");
                                    startActivity(intent);
                                } catch (ActivityNotFoundException e){
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    Uri uri = Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName());
                                    intent.setData(uri);
                                    intent.setPackage("com.android.vending");
                                    startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Si el usuario ya inicio sesion ingresar al Home
        if (mAuth.getId() != null && dbEmployees.getEmployee(mAuth.getId()) != null) {
            //getIntent().hasExtra("data") getIntent().getExtras().getString("data")
            if (getIntent().getExtras() != null) {
                titleNotify = getIntent().getExtras().getString("titleNotify");
                bodyNotify = getIntent().getExtras().getString("bodyNotify");
                urlNotify = getIntent().getExtras().getString("urlNotify");
                imageNotify = getIntent().getExtras().getString("imageNotify");
                idUser = mAuth.getId();
                if(urlNotify != null && !notify){
                    notify = true;
                    biometricPrompt.cancelAuthentication();
                    Intent i = new Intent(this, ShowNotificationActivity.class);
                    i.putExtra("idUser", idUser);
                    i.putExtra("title", titleNotify);
                    i.putExtra("body", bodyNotify);
                    i.putExtra("image", imageNotify);
                    i.putExtra("url", urlNotify);
                    i.putExtra("main", true);
                    startActivity(i);
                } else {
                    if(canUseBiometrics == 0){
                        if(dbEmployees.getEmployee(mAuth.getId()).isStateBiometrics()){
                            if(!changePassword){
                                biometricPrompt.authenticate(promptInfo);
                                biometric = true;
                            }
                        } else {
                            moveFragment(loginFragment);
                            loginFragment.setTextEmail(dbEmployees.getEmployee(mAuth.getId()).getName());
                            biometric = false;
                        }
                    } else {
                        moveFragment(loginFragment);
                        loginFragment.setTextEmail(dbEmployees.getEmployee(mAuth.getId()).getName());
                        biometric = false;
                    }
                }
            } else {
                if(titleNotify != null){
                    if(urlNotify != null && !notify){
                        notify = true;
                        biometricPrompt.cancelAuthentication();
                        Intent i = new Intent(this, ShowNotificationActivity.class);
                        i.putExtra("idUser", idUser);
                        i.putExtra("title", titleNotify);
                        i.putExtra("body", bodyNotify);
                        i.putExtra("image", imageNotify);
                        i.putExtra("url", urlNotify);
                        i.putExtra("main", true);
                        startActivity(i);
                    } else {
                        if(canUseBiometrics == 0){
                            if(dbEmployees.getEmployee(mAuth.getId()).isStateBiometrics()){
                                if(!changePassword){
                                    biometricPrompt.authenticate(promptInfo);
                                    biometric = true;
                                }
                            } else {
                                moveFragment(loginFragment);
                                loginFragment.setTextEmail(dbEmployees.getEmployee(mAuth.getId()).getName());
                                biometric = false;
                            }
                        } else {
                            moveFragment(loginFragment);
                            loginFragment.setTextEmail(dbEmployees.getEmployee(mAuth.getId()).getName());
                            biometric = false;
                        }
                    }
                } else {
                    if(canUseBiometrics == 0){
                        if(dbEmployees.getEmployee(mAuth.getId()).isStateBiometrics()){
                            if(!changePassword){
                                biometricPrompt.authenticate(promptInfo);
                                biometric = true;
                            }
                        } else {
                            moveFragment(loginFragment);
                            loginFragment.setTextEmail(dbEmployees.getEmployee(mAuth.getId()).getName());
                            biometric = false;
                        }
                    } else {
                        moveFragment(loginFragment);
                        loginFragment.setTextEmail(dbEmployees.getEmployee(mAuth.getId()).getName());
                        biometric = false;
                    }
                }
            }
        }
    }

    public void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
                //shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS);
                //requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Permiso otorgado para enviar notificaciones", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No cuentas con permiso para enviar notificaciones", Toast.LENGTH_SHORT).show();
                }
            });

    public static void updateNotify(String title, String body, String url, String image, String idUserN){
        titleNotify = title;
        bodyNotify = body;
        urlNotify = url;
        imageNotify = image;
        idUser = idUserN;
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*long l = Utils.getTimeLongNet();
        if(l != 0){
            Log.d("TIME", "YOUR TIME: " + l);
        }*/
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

    @Override
    protected void onPause() {
        super.onPause();
        if(biometricPrompt != null)
        biometricPrompt.cancelAuthentication();
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