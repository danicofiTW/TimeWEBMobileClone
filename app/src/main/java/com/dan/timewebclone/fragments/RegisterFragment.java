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
import android.widget.Switch;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.RenderMode;
import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.activitys.MainActivity;
import com.dan.timewebclone.R;
import com.dan.timewebclone.activitys.SettingsActivity;
import com.dan.timewebclone.db.DbBitacoras;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.db.DbGeocercas;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.dan.timewebclone.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.hbb20.CountryCodePicker;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterFragment extends Fragment{


    private MainActivity myContext;
    private TextInputEditText textInputName, textInputClaveUser, textInputRFCCompany, textInputEmail, textInputPassword, textInputConfimPassword;
    private EditText editTextPhone;
    private Button btnRegister;
    private ProgressDialog mDialog;
    private CountryCodePicker countryCode;
    private CircleImageView imageViewBack;
    private LinearLayout linearLayoutRegister;
    private Switch switchAceptPerms;
    private LottieAnimationView animation;

    FragmentTransaction fragmentTransaction;
    FragmentManager fragmentManager;
    LoginFragment loginFragment;
    DialogFragment termsAndConditionsFragment;

    private AuthProvider authProvider;
    private EmployeeProvider employeeProvider;
    private DbEmployees dbEmployees;
    private DbChecks dbChecks;
    private DbGeocercas dbGeocercas;
    private DbBitacoras dbBitacoras;
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

        btnRegister = view.findViewById(R.id.btnRegister);
        animation = view.findViewById(R.id.animationRegister);
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
        switchAceptPerms = view.findViewById(R.id.switch1);
        animation.isHardwareAccelerated();
        //animation.enableMergePathsForKitKatAndAbove(true);
        animation.setRenderMode(RenderMode.HARDWARE);
        /*loginFragment = new LoginFragment();
        loginFragment.setViewLoginFragment(false);
        setViewRegisterFragment(true);*/

        datosAEnviar = new Bundle();
        authProvider = new AuthProvider();
        employeeProvider = new EmployeeProvider();
        dbEmployees = new DbEmployees(myContext);
        dbChecks = new DbChecks(myContext);
        dbBitacoras = new DbBitacoras(myContext);
        dbGeocercas = new DbGeocercas(myContext);

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
                    if(switchAceptPerms.isChecked()){
                        mDialog.show();
                        register(mName, mClave, mRFC, mEmail, mPhone, mPassword);
                    } else {
                        Toast.makeText(myContext, "Acepta los permisos para continuar", Toast.LENGTH_LONG).show();
                    }
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

    private void register(String name, String clave, String rfc, String email, String phone, String password) {
        if(Utils.isOnlineNet(myContext)){
            authProvider.register(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(mDialog.isShowing())
                        mDialog.dismiss();
                    String id = authProvider.getId();
                    Employee employee = new Employee(name,clave,rfc,email,phone,password,id);
                    create(employee);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if(mDialog.isShowing())
                        mDialog.dismiss();
                    Toast.makeText(myContext, "Error al registrar el usuario: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            if(mDialog.isShowing())
                mDialog.dismiss();
            Toast.makeText(myContext, "No cuentas con internet", Toast.LENGTH_SHORT).show();
        }
    }

    void create(Employee employee){
       employeeProvider.create(employee).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    if(Utils.isGMS(myContext)){
                        employeeProvider.deleteToken(authProvider.getId(), myContext);
                    } else {
                        employeeProvider.deleteTokenHMS(authProvider.getId(), myContext);
                    }
                    if(dbEmployees.deleteAllEmployees() && dbChecks.deleteAllChecks() && dbBitacoras.deleteAllBitacoras() && dbGeocercas.deleteAllGeocercas()){
                        dbEmployees.insertEmployye(employee);
                        Intent intent = new Intent(myContext, SettingsActivity.class);
                        intent.putExtra("loginNotData", true);
                        intent.putExtra("geocercas", false);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(myContext, "Error en DB", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(myContext, "No se pudo registrar el usuario en internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return  inflater.inflate(R.layout.fragment_register, container, false);

    }

}