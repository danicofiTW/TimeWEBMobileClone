package com.dan.timewebclone.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.activitys.MainActivity;
import com.dan.timewebclone.R;
import com.dan.timewebclone.activitys.ProfileActivity;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class LoginFragment extends Fragment {

    private TextInputEditText textInputEmailLogin, textInputPasswordLogin;
    private LinearLayout linearLayoutLogin;
    private CircleImageView mCircleImageBack;
    private Button buttonLogin;

    private EmployeeProvider employeeProvider;
    private AuthProvider authProvider;
    //DatabaseReference mDatabase;
    private ProgressDialog mDialog;
    private MainActivity myContext;

    Employee employee;

    String eName, eIdUser, eRFCEmpresa, eCompany, ePhone, eEmail, eURL;

    FragmentTransaction fragmentTransaction;
    FragmentManager fragmentManager;
    RegisterFragment registerFragment;

    public LoginFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        myContext = (MainActivity) context;
        super.onAttach(context);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textInputEmailLogin = view.findViewById(R.id.textInputEmailLogin);
        textInputPasswordLogin = view.findViewById(R.id.textInputPasswordLogin);
        buttonLogin = view.findViewById(R.id.btnLogin);
        mCircleImageBack = view.findViewById(R.id.circleImageBack);
        linearLayoutLogin = view.findViewById(R.id.linearLayoutLogin);
        authProvider = new AuthProvider();
        //registerFragment = new RegisterFragment();
        //registerFragment.setViewRegisterFragment(false);
        //setViewLoginFragment(true);
        //mDatabase = FirebaseDatabase.getInstance().getReference();

        employeeProvider = new EmployeeProvider();

        mDialog = new ProgressDialog(myContext);
        mDialog.setTitle("ESPERE UN MOMENTO");
        mDialog.setMessage("Iniciando sesion");


        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLogin();
            }
        });

        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { fragmentBack(); }
        });

    }

    public void setViewLoginFragment(boolean viewFragment){
        if(viewFragment){
            linearLayoutLogin.setVisibility(View.VISIBLE);

        } else {
            linearLayoutLogin.setVisibility(View.GONE);
        }
    }

    private void fragmentBack() {
        //OCULTAR EL TECLADO
        InputMethodManager imm = (InputMethodManager) myContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

        myContext.setBtns(true);
        fragmentManager = myContext.getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(this).commit();
    }

    private void checkLogin() {
        String mEmail = textInputEmailLogin.getText().toString();
        String mPassword = textInputPasswordLogin.getText().toString();

        if (!mEmail.isEmpty() && !mPassword.isEmpty()) {
            if(mPassword.length() >= 6){
                    mDialog.show();
                    login(mEmail, mPassword);
                } else {
                Toast.makeText(myContext, "La contraseña debe contener minimo 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(myContext, "Debe completar toda la informacion", Toast.LENGTH_LONG).show();
        }
    }

    private void login(String mEmail, String mPassword) {
        if(isOnlineNet()){
        authProvider.loginEmail(mEmail,mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mDialog.dismiss();
                    goToHome();
                } else {
                    mDialog.dismiss();
                    Toast.makeText(myContext, "La contraseña o el email son incorrectos", Toast.LENGTH_SHORT).show();
                }
            }
        });
        } else {
            mDialog.dismiss();
            Toast.makeText(myContext, "No cuentas con internet", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isOnlineNet() {
        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");
            int val = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDialog.hide();
    }

    private void goToHome() {
        Intent intent = new Intent(myContext, HomeTW.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

}