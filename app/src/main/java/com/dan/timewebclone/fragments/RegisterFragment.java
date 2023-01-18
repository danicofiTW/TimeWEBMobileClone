package com.dan.timewebclone.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.activitys.MainActivity;
import com.dan.timewebclone.R;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.hbb20.CountryCodePicker;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterFragment extends Fragment{

    View mView;
    private MainActivity myContext;
    TextInputEditText textInputName, textInputClaveUser, textInputRFCCompany, textInputEmail, textInputPassword, textInputConfimPassword;
    EditText editTextPhone;
    Button btnRegister;
    ProgressDialog mDialog;
    CountryCodePicker countryCode;
    CircleImageView imageViewBack;
    LinearLayout linearLayoutRegister;

    FragmentTransaction fragmentTransaction;
    FragmentManager fragmentManager;
    LoginFragment loginFragment;
    DialogFragment termsAndConditionsFragment;

    AuthProvider authProvider;
    EmployeeProvider employeeProvider;
    //public FirebaseAuth mAuth;
    Bundle datosAEnviar;

    Employee employee;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        myContext = (MainActivity) context;
        super.onAttach(context);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnRegister = (Button) view.findViewById(R.id.btnRegister);
        imageViewBack =  view.findViewById(R.id.circleImageBackRegister);
        textInputName =  view.findViewById(R.id.textInputName);
        textInputClaveUser =  view.findViewById(R.id.textInputKey);
        textInputRFCCompany =  view.findViewById(R.id.textInputRFC);
        textInputPassword =  view.findViewById(R.id.textInputPassword);
        textInputEmail =  view.findViewById(R.id.textInputEmail);
        textInputConfimPassword =  view.findViewById(R.id.textInputConfirmPassword);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        countryCode = view.findViewById(R.id.ccp);
        linearLayoutRegister = view.findViewById(R.id.linearLayoutRegister);

        /*loginFragment = new LoginFragment();
        loginFragment.setViewLoginFragment(false);
        setViewRegisterFragment(true);*/


        datosAEnviar = new Bundle();
        authProvider = new AuthProvider();
        //mAuth = FirebaseAuth.getInstance();
        employeeProvider = new EmployeeProvider();

        mDialog = new ProgressDialog(myContext);
        mDialog.setTitle("ESPERE UN MOMENTO");
        mDialog.setMessage("Guardando informacion");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                clickRegisterUser();
            }
        });

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentBack();
            }
        });


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



    private void clickRegisterUser() {
        String mName = textInputName.getText().toString();
        String mClave = textInputClaveUser.getText().toString();
        String mRFC = textInputRFCCompany.getText().toString();
        String mEmail = textInputEmail.getText().toString();
        String mPhone = editTextPhone.getText().toString();
        String mPassword = textInputPassword.getText().toString();
        String mConfirmPassword = textInputConfimPassword.getText().toString();

        if (!mName.isEmpty() && !mClave.isEmpty() && !mRFC.isEmpty()  && !mEmail.isEmpty() && !mPhone.isEmpty() && !mPassword.isEmpty() && !mConfirmPassword.isEmpty()) {
            String code = countryCode.getSelectedCountryCodeWithPlus();
            mPhone= code + mPhone;
            //Toast.makeText(myContext, "Informacion completa", Toast.LENGTH_LONG).show();
            //mAuth.sendCodeVerification(mPhone, mCallbacks);
            if(mPassword.length() >= 6){
                if(Integer.parseInt(mPassword) == Integer.parseInt(mConfirmPassword)){
                    mDialog.show();
                        register(mName, mClave, mRFC, mEmail, mPhone, mPassword);
                } else{
                    Toast.makeText(myContext, "La contraseña debe conincidir", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(myContext, "La contraseña debe contener minimo 6 caracteres", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(myContext, "Debe completar toda la informacion", Toast.LENGTH_LONG).show();
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


    private void register(String name, String clave, String rfc, String email, String phone, String password) {
        if(isOnlineNet()){
            authProvider.register(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    mDialog.hide();
                    String id = authProvider.getId();
                    Employee employee = new Employee(name,clave,rfc,email,phone,password,id);
                    create(employee);
                }
            });
        } else {
            mDialog.hide();
            Toast.makeText(myContext, "No cuentas con internet", Toast.LENGTH_SHORT).show();
        }
    }

    void create(Employee employee){
       employeeProvider.create(employee).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    getUserInfo();
                    Intent intent = new Intent(myContext, HomeTW.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(myContext, "No se pudo registrar el usuario en internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

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

    /*private void goToVerificationCode() {
        loginFragment = new VerificationFragment();
        datosAEnviar.putString("phone", mPhone);
        datosAEnviar.putString("email", mEmail);
        datosAEnviar.putString("password", mPassword);
        datosAEnviar.putString("RFC", mRFC);
        loginFragment.setArguments(datosAEnviar);
        fragmentManager = myContext.getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayoutMain, loginFragment );
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        //termsAndConditionsFragment.show(fragmentManager, "TermsAndConditionsFragment");
    }*/





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return  inflater.inflate(R.layout.fragment_register, container, false);

    }



}