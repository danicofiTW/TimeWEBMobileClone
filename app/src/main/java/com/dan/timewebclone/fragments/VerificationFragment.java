package com.dan.timewebclone.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.activitys.MainActivity;
import com.dan.timewebclone.R;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;


public class VerificationFragment extends Fragment {

    EditText claveEmpleado, RFCEmpleado, emailEmpleado, contraseña, confirmarContraseña;
    CountryCodePicker countryCode;
    Button buttonVerification;
    String extraPhone, mVerificationId, extraPassword, extraCorreo, extraRFC;
    //PhoneAuthProvider.ForceResendingToken mResendToken;
    //AuthProvider miAuthProvider;
    //AuthProvider miAuthProvider;
    EditText editTextCode;
    TextView miTextViewSMS;
    ProgressBar miProgressBar;
    EmployeeProvider mEmployeeProvider;
    public FirebaseAuth mAuth1;
    private MainActivity myContext;
    RegisterFragment registerFragment;
    Employee employee;

    RegisterFragment fragment;
    FragmentTransaction fragmentTransaction;
    FragmentManager fragmentManager;
    Toolbar toolbar;
    

    public VerificationFragment() {
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
        buttonVerification = view.findViewById(R.id.btnVerification);
        //miAuthProvider = new AuthProvider();
        //extraPhone = employee.getPhone();
        //extraPhone = registerFragment.getPhone();
        editTextCode = view.findViewById(R.id.editTextCodeVerification);
        miTextViewSMS =view. findViewById(R.id.textViewSMS);
        miProgressBar = view.findViewById(R.id.progressBar);
        mAuth1 = FirebaseAuth.getInstance();
        mEmployeeProvider = new EmployeeProvider();

        myContext.setSupportActionBar(toolbar);
        iniciarToolBar();



        Bundle datosRecuperados = getArguments();
        if (datosRecuperados == null) {
            // No hay datos, manejar excepción
            return;
        } else {
            extraPhone = datosRecuperados.getString("phone");
            extraCorreo = datosRecuperados.getString("email");
            extraPassword = datosRecuperados.getString("password");
            extraRFC = datosRecuperados.getString("RFC");
            // Imprimimos, pero en tu caso haz lo necesario
        }

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth1)
                        .setPhoneNumber(extraPhone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(myContext)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAnterior();
            }
        });

        buttonVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String codigo = editTextCode.getText().toString();
                if(!codigo.equals("") && codigo.length() >= 6){
                    signId(codigo);
                }
                else {
                    Toast.makeText(myContext, "Ingresa el codigo", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void goToAnterior() {
        fragment = new RegisterFragment();
        fragmentManager = myContext.getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayoutMain, fragment);
        fragmentTransaction.commit();
    }

    private void iniciarToolBar() {
        // add back arrow to toolbar
        if (myContext.getSupportActionBar() != null){
            myContext.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            myContext.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            miProgressBar.setVisibility(View.GONE);
            miTextViewSMS.setVisibility(View.GONE);
            String verCode = phoneAuthCredential.getSmsCode();
            if(verCode != null){
                editTextCode.setText(verCode);
                signId(verCode);
            }
            /*else {
                Toast.makeText(VerificationCode.this, "NO SE ENVIO NADA", Toast.LENGTH_LONG).show();
            }*/
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            miProgressBar.setVisibility(View.GONE);
            miTextViewSMS.setVisibility(View.GONE);
            Toast.makeText(myContext, "Error" + e.getMessage(), Toast.LENGTH_LONG).show();

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }

        }

        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            miProgressBar.setVisibility(View.GONE);
            miTextViewSMS.setVisibility(View.GONE);
            Toast.makeText(myContext, "EL CODIGO SE ENVIO", Toast.LENGTH_LONG).show();
            mVerificationId = verificationId;
        }
    };

    private void signId(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        mAuth1.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Employee employee = new Employee();

                    if(mAuth1.getCurrentUser() != null) {
                        /*employee.setClave(mAuth1.getCurrentUser().getUid());
                        employee.setPhone(extraPhone);
                        employee.setRfc(extraRFC);
                        employee.setEmail(extraCorreo);
                        employee.setContraseña(extraPassword);*/

                        mEmployeeProvider.getUserInfo(mAuth1.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (!documentSnapshot.exists()) {
                                    mEmployeeProvider.create(employee).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            goToHomeW();
                                        }
                                    });
                                } else if(documentSnapshot.contains("clave")) {
                                    String clave = documentSnapshot.getString("clave");
                                    if(clave != null){
                                        if(!clave.equals("")){
                                            goToHomeW();
                                        }
                                        else {
                                            goToAnterior();
                                        }
                                    }else{
                                        goToAnterior();
                                    }
                                }
                                else{
                                    goToAnterior();
                                }
                            }
                        });
                    }
                }
                else {
                    Toast.makeText(myContext, "No se pudo autenticar", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void goToHomeW() {
        Intent i = new Intent(myContext, HomeTW.class);
        //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verification_code, container, false);
    }
}