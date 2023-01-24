package com.dan.timewebclone.activitys;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import android.view.View;
import android.widget.Button;

import com.dan.timewebclone.R;
import com.dan.timewebclone.fragments.LoginFragment;
import com.dan.timewebclone.fragments.TermsAndConditionsFragment;
import com.dan.timewebclone.fragments.RegisterFragment;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button btnGoToRegister, btnGoToLogin;
    private AlertDialog.Builder builderDialogExit;

    private LoginFragment loginFragment;
    private RegisterFragment registerFragment;

    private FragmentTransaction fragmentTransaction;
    private FragmentManager fragmentManager;
    private DialogFragment termsAndConditionsFragment;

    private AuthProvider mAuth = null;

    private String changePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setStatusBarColor();
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        loginFragment = new LoginFragment();
        registerFragment = new RegisterFragment();
        termsAndConditionsFragment = new TermsAndConditionsFragment();
        mAuth = new AuthProvider();
        builderDialogExit = new AlertDialog.Builder(this);

        //Cachar variable que se entrega al cerrar sesion y al cambiar password
        changePassword = getIntent().getStringExtra("ChangePassword");
        if (changePassword != null) {
            moveFragment(loginFragment);
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Si el usuario ya inicio sesion ingresar al Home
        if (mAuth.getId() != null) {
            Intent i = new Intent(MainActivity.this, HomeTW.class);
            i.putExtra("revieEmployee", false);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
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


    /*public void saveInfoUser(Employee employee) {
        SharedPreferences sharedPref = getSharedPreferences("Employee", Context.MODE_PRIVATE);
        //sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("nameUser", employee.getName());
        editor.putString("idUser", employee.getIdUser());
        editor.putString("rfc", employee.getRfcCompany());
        editor.putString("company", employee.getCompany());
        editor.putString("phone", employee.getPhone());
        editor.putString("email", employee.getEmail());
        editor.putString("url", employee.getImage());
        editor.apply();
        editor.commit();
    }*/


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