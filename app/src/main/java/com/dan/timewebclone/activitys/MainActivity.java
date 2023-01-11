package com.dan.timewebclone.activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
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

    Button btnGoToRegister, btnGoToLogin;

    AuthProvider mAuth = null;


    LoginFragment loginFragment;
    RegisterFragment registerFragment;

    FragmentTransaction fragmentTransaction;
    FragmentManager fragmentManager;
    DialogFragment termsAndConditionsFragment;



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

        checkViewFragment(registerFragment);
        checkViewFragment(loginFragment);

        btnGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveFragment(registerFragment);
                termsAndConditionsFragment.show(fragmentManager, "TermsAndConditionsFragment");
            }
        });

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
       if(mAuth.getId() != null){
            Intent i = new Intent(MainActivity.this, HomeTW.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } else{
           //Toast.makeText(MainActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
       }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void moveFragment(Fragment fragment) {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if (fragment.isHidden()){
            fragmentTransaction.show(fragment).commit();
        } else {
            fragmentTransaction.add(R.id.frameLayoutMain, fragment).addToBackStack(null).commit();
        }
    }

    private void checkViewFragment(Fragment fragment) {
        if (fragment.isVisible()){
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide(fragment).commit();
        }
    }

    public void saveInfoUser(Employee employee) {
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
    }

    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorNotificationToolbarMain, this.getTheme()));
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorNotificationToolbarMain));
        }
    }


}