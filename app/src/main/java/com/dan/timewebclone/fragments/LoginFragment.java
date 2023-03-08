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

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.RenderMode;
import com.dan.timewebclone.activitys.GeocercasActivity;
import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.activitys.MainActivity;
import com.dan.timewebclone.R;
import com.dan.timewebclone.activitys.ProfileActivity;
import com.dan.timewebclone.activitys.SettingsActivity;
import com.dan.timewebclone.db.DbBitacoras;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.db.DbGeocercas;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.BitacoraProvider;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.dan.timewebclone.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class LoginFragment extends Fragment {

    private TextInputEditText textInputEmailLogin, textInputPasswordLogin;
    private LinearLayout linearLayoutLogin;
    private CircleImageView mCircleImageBack;
    private Button buttonLogin;
    private LottieAnimationView animation;

    private EmployeeProvider employeeProvider;
    private AuthProvider authProvider;
    //DatabaseReference mDatabase;
    private ProgressDialog mDialog;
    private MainActivity myContext;
    private DbEmployees dbEmployees;
    private DbBitacoras dbBitacoras;
    private DbChecks dbChecks;
    private DbGeocercas dbGeocercas;
    private BitacoraProvider bitacoraProvider;

    FragmentTransaction fragmentTransaction;
    FragmentManager fragmentManager;
    RegisterFragment registerFragment;
    private Handler mHandler = new Handler();

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
        animation = view.findViewById(R.id.animationLogin);
        textInputPasswordLogin = view.findViewById(R.id.textInputPasswordLogin);
        buttonLogin = view.findViewById(R.id.btnLogin);
        mCircleImageBack = view.findViewById(R.id.circleImageBack);
        linearLayoutLogin = view.findViewById(R.id.linearLayoutLogin);
        authProvider = new AuthProvider();
        dbEmployees = new DbEmployees(myContext);
        dbBitacoras = new DbBitacoras(myContext);
        dbGeocercas = new DbGeocercas(myContext);
        dbChecks = new DbChecks(myContext);
        employeeProvider = new EmployeeProvider();
        bitacoraProvider = new BitacoraProvider();
        animation.isHardwareAccelerated();
        //animation.enableMergePathsForKitKatAndAbove(true);
        animation.setRenderMode(RenderMode.HARDWARE);

        if (authProvider.getId() != null && dbEmployees.getEmployee(authProvider.getId()) != null) {
            textInputEmailLogin.setText(dbEmployees.getEmployee(authProvider.getId()).getEmail());
        }

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

    public void setTextEmail(String text){
        if(textInputEmailLogin!=null){}
       // textInputEmailLogin.setText(text);
    }

    private void checkLogin() {

        mDialog.show();
        String mEmail = textInputEmailLogin.getText().toString();
        String mPassword = textInputPasswordLogin.getText().toString();

        if (!mEmail.isEmpty() && !mPassword.isEmpty()) {
            if(mPassword.length() >= 6){
                    login(mEmail, mPassword);
            } else {
                if(mDialog.isShowing())
                    mDialog.dismiss();
                Toast.makeText(myContext, "La contraseña debe contener minimo 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        } else {
            if(mDialog.isShowing())
                mDialog.dismiss();
            Toast.makeText(myContext, "Debe completar toda la informacion", Toast.LENGTH_LONG).show();
        }
    }

    private void login(String mEmail, String mPassword) {
        if(Utils.isOnlineNet(myContext)){
            if(authProvider.existSesion())
                authProvider.signOut();
            /*authProvider.loginEmail(mEmail,mPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    if(mDialog.isShowing())
                        mDialog.dismiss();
                    goToHome();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if(mDialog.isShowing())
                        mDialog.dismiss();
                    Toast.makeText(myContext, "La contraseña o el email son incorrectos", Toast.LENGTH_SHORT).show();
                }
            });*/
            authProvider.loginEmail(mEmail,mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        if(mDialog.isShowing())
                            mDialog.dismiss();
                            goToHome();
                    } else {
                        if(mDialog.isShowing())
                            mDialog.dismiss();
                        Toast.makeText(myContext, "La contraseña o el email son incorrectos", Toast.LENGTH_SHORT).show();
                    }
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
            mDialog.dismiss();
            Toast.makeText(myContext, "No cuentas con internet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDialog.hide();
    }

    private void goToHome() {
        if(dbEmployees.getEmployee(authProvider.getId()) != null){
            if(dbBitacoras.getBitacorasByIdUser(authProvider.getId()).size() != 0){
                Intent intent = new Intent(myContext, GeocercasActivity.class);
                intent.putExtra("notComeBack", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                if(Utils.isOnlineNet(myContext)){
                    bitacoraProvider.getBitacorasByUser(authProvider.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot querySnapshot) {
                            if(querySnapshot.size() != 0){
                                Intent i = new Intent(myContext, GeocercasActivity.class);
                                i.putExtra("notComeBack", true);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                            } else {
                                dbBitacoras.deleteAllBitacoras();
                                Intent intent = new Intent(myContext, HomeTW.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }
                    });
                } else {
                    dbBitacoras.deleteAllBitacoras();
                    Intent intent = new Intent(myContext, HomeTW.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        } else {
            employeeProvider.getUserInfo(authProvider.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if (task.getResult() != null) {
                            if (task.getResult().exists()) {
                                Employee employee = task.getResult().toObject(Employee.class);
                                if(Utils.isGMS(myContext)){
                                    employeeProvider.deleteToken(authProvider.getId(), myContext);
                                } else {
                                    employeeProvider.deleteTokenHMS(authProvider.getId(), myContext);
                                }
                                if(dbEmployees.deleteAllEmployees() && dbChecks.deleteAllChecks() && dbBitacoras.deleteAllBitacoras() && dbGeocercas.deleteAllGeocercas()){
                                    dbEmployees.insertEmployye(employee);
                                    bitacoraProvider.getBitacorasByUser(authProvider.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if(task.isSuccessful()){
                                                if(task.getResult().size() != 0){
                                                    Intent intent = new Intent(myContext, SettingsActivity.class);
                                                    intent.putExtra("loginNotData", true);
                                                    intent.putExtra("geocercas", true);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                } else {
                                                    Intent intent = new Intent(myContext, SettingsActivity.class);
                                                    intent.putExtra("loginNotData", true);
                                                    intent.putExtra("geocercas", false);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                }
                                            } else {
                                                Intent intent = new Intent(myContext, SettingsActivity.class);
                                                intent.putExtra("loginNotData", true);
                                                intent.putExtra("geocercas", false);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(myContext, "Error en DB", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(myContext, "No se encuentra la informacion del empleado", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(myContext, "No se encuentra la informacion del empleado", Toast.LENGTH_SHORT).show();
                        }
                    } else{
                        Toast.makeText(myContext, "Error al obtener la informacion del empleado", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    /*private void goToSettings() {
        Intent intent = new Intent(myContext, HomeTW.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

}