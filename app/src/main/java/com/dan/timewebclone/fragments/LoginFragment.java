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

    TextInputEditText textInputEmailLogin, textInputPasswordLogin;
    private CircleImageView mCircleImageBack;
    Button buttonLogin;
    EmployeeProvider employeeProvider;

    AuthProvider authProvider;
    //DatabaseReference mDatabase;
    AlertDialog dialog;
    ProgressDialog mDialog;

    private MainActivity myContext;


    Employee employee;


    String eName, eIdUser, eRFCEmpresa, eCompany, ePhone, eEmail, eURL;



    FragmentTransaction fragmentTransaction;
    FragmentManager fragmentManager;

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
        authProvider = new AuthProvider();
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

    private void fragmentBack() {
        //OCULTAR EL TECLADO
        InputMethodManager imm = (InputMethodManager) myContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDialog.hide();
    }

    private void goToHome() {
        getUserInfo();
        Intent intent = new Intent(myContext, HomeTW.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    private void getUserInfo() {
        employeeProvider.getUserInfo(authProvider.getId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if(documentSnapshot!= null){
                    if(documentSnapshot.exists()){
                        employee = documentSnapshot.toObject(Employee.class);
                        myContext.saveInfoUser(employee);
                    }
                }
            }
        });

    }

}