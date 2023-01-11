package com.dan.timewebclone.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dan.timewebclone.R;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.fragments.ReviePasswordFragment;
import com.dan.timewebclone.fragments.UpdatePasswordFragment;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText tietEmail, tietPassword, tietNewPassword, tietConfirmNewPassword;
    private TextView textViewChangePassword;
    private Button btnChangePassword;
    private ImageView imageViewBack;
    private LinearLayout linearLayoutEmail;
    private ProgressDialog changePasswordProgress;

    private AuthProvider authProvider;
    private EmployeeProvider employeeProvider;
    private DbEmployees dbEmployees;
    private Employee employee;
    private int levelFragment;

    private UpdatePasswordFragment updatePasswordFragment;
    private ReviePasswordFragment reviePasswordFragment;

    private FragmentTransaction fragmentTransaction;
    private FragmentManager fragmentManager;


    private String password, newPassword, confirmNewPassword, email;
    //public String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        setStatusBarColor();

        tietEmail = findViewById(R.id.textInputEmail);
        //tietPassword = findViewById(R.id.textInputPassword);
        //tietNewPassword = findViewById(R.id.textInputNewPassword);
        //tietConfirmNewPassword = findViewById(R.id.textInputConfirmNewPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        imageViewBack = findViewById(R.id.imageViewBackCP);
        textViewChangePassword = findViewById(R.id.textViewChangePassword);
        linearLayoutEmail = findViewById(R.id.linearLayoutEmail);
        levelFragment = 0;

        updatePasswordFragment = new UpdatePasswordFragment();
        reviePasswordFragment = new ReviePasswordFragment();

        authProvider = new AuthProvider();
        employeeProvider = new EmployeeProvider();
        dbEmployees = new DbEmployees(ChangePasswordActivity.this);
        employee = dbEmployees.getEmployee(authProvider.getId());
        changePasswordProgress = new ProgressDialog(ChangePasswordActivity.this);
        changePasswordProgress.setTitle("Cambiando");
        changePasswordProgress.setMessage("Por favor espere ...");
        //changePasswordProgress.setProgressStyle(changePasswordProgress.STYLE_HORIZONTAL);
        //changePasswordProgress.setProgress(0);
        //changePasswordProgress.setMax(10);
        //changePasswordProgress.show();


        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(levelFragment == 0){
                    email = tietEmail.getText().toString();
                    //password = tietPassword.getText().toString();
                    //newPassword = tietNewPassword.getText().toString();
                    //confirmNewPassword = tietConfirmNewPassword.getText().toString();
                    if(!email.isEmpty()){
                        if(employee!=null){
                            if(employee.getEmail().equals(email)){
                                btnChangePassword.setText("CONFIRMAR CONTRASEÑA");
                                textViewChangePassword.setText("Ingresa la contraseña actual");
                                levelFragment = 1;
                                moveFragment(reviePasswordFragment);
                            } else {
                                Toast.makeText(ChangePasswordActivity.this, "El correo no corresponde", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else{
                        Toast.makeText(ChangePasswordActivity.this, "Ingresa el correo electronico", Toast.LENGTH_SHORT).show();
                    }
                } else if(levelFragment==1){
                    if(reviePasswordFragment.tietPassword != null){
                        password = reviePasswordFragment.tietPassword.getText().toString();
                        if(!password.isEmpty()){
                            if(employee.getPassword().equals(password)){
                                btnChangePassword.setText("ACTUALIZAR CONTRASEÑA");
                                textViewChangePassword.setText("Nueva contraseña");
                                levelFragment = 2;
                                moveFragment(updatePasswordFragment);
                            } else {
                                Toast.makeText(ChangePasswordActivity.this, "La contraseña actual no es valida", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ChangePasswordActivity.this, "Ingresa la contraseña", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                   if(updatePasswordFragment.tietNewPassword != null && updatePasswordFragment.tietConfirmNewPassword != null){
                       newPassword = updatePasswordFragment.tietNewPassword.getText().toString();
                       confirmNewPassword = updatePasswordFragment.tietConfirmNewPassword.getText().toString();
                       if(!newPassword.isEmpty() && !confirmNewPassword.isEmpty()){
                           if(newPassword.equals(confirmNewPassword)){
                               if(newPassword.length()>=6){
                                   if(!newPassword.equals(password)){
                                       if(isOnlineNet()){
                                           changePasswordProgress.show();
                                           changePassword(employee, email, password, newPassword);
                                       } else {
                                           Toast.makeText(ChangePasswordActivity.this, "No cuentas con internet para actualizar la contraseña", Toast.LENGTH_SHORT).show();
                                       }
                                   } else {
                                       Toast.makeText(ChangePasswordActivity.this, "Ya cuentas con esta contraseña", Toast.LENGTH_SHORT).show();
                                   }
                               } else {
                                   Toast.makeText(ChangePasswordActivity.this, "La contraseña debe contener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                               }
                           } else {
                               Toast.makeText(ChangePasswordActivity.this, "La contraseña no coincide", Toast.LENGTH_SHORT).show();
                           }
                       } else {
                           Toast.makeText(ChangePasswordActivity.this, "Ingresa todos los campos", Toast.LENGTH_SHORT).show();
                       }
                   }
                }


                /*if(!email.isEmpty()){
                    if(employee != null){
                        if(employee.getEmail().equals(email)){
                            if(employee.getPassword().equals(password)){
                                if(newPassword.equals(confirmNewPassword)){
                                    if(newPassword.length()>=6){
                                      if(newPassword.equals(password)){
                                           Toast.makeText(ChangePasswordActivity.this, "Es la misma contraseña, cambiala!!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            changePasswordProgress.show();
                                            changePassword(employee, email, password, newPassword);
                                        }
                                    } else {
                                        Toast.makeText(ChangePasswordActivity.this, "La contraseña nueva debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(ChangePasswordActivity.this, "La nueva contraseña no coincide", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ChangePasswordActivity.this, "La contraseña actual no es valida", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ChangePasswordActivity.this, "El correo no corresponde", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Debes llenar todos los campos", Toast.LENGTH_SHORT).show();
                }
                /*if(employee!=null){
                    changePassword(employee, email, password, newPassword);
                }*/
            }
        });

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });

    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void back(){
        if(levelFragment==0){
            finish();
        } else if(levelFragment == 1){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(reviePasswordFragment.tietPassword.getWindowToken(), 0);
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide(reviePasswordFragment).commit();
            textViewChangePassword.setText("Confirma tu correo electronico");
            btnChangePassword.setText("CONFIRMAR CORREO");
            levelFragment = 0;
            linearLayoutEmail.setVisibility(View.VISIBLE);
        } else {
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide(updatePasswordFragment);
            fragmentTransaction.show(reviePasswordFragment).commit();
            reviePasswordFragment.textViewEmailConfirm.setText(employee.getName());
            textViewChangePassword.setText("Ingresa la contraseña actual");
            btnChangePassword.setText("CONFIRMAR CONTRASEÑA");
            levelFragment = 1;
        }
    }

    private void moveFragment(Fragment fragment) {
        if(levelFragment == 1){
            linearLayoutEmail.setVisibility(View.GONE);
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            if (fragment.isHidden()){
                fragmentTransaction.show(fragment).commit();
            } else {
                fragmentTransaction.add(R.id.frameLayoutChangePassword, fragment).addToBackStack(null).commit();
            }
        } else if(levelFragment == 2){
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide(reviePasswordFragment);
            if (fragment.isHidden()){
                fragmentTransaction.show(fragment).commit();
            } else {
                fragmentTransaction.add(R.id.frameLayoutChangePassword, fragment).addToBackStack(null).commit();
            }
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

    private void changePassword(Employee employee, String email, String password, String newPassword) {
        authProvider.getCredential(email, password).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                authProvider.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        employeeProvider.updatePassword(employee.getIdUser(), newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                dbEmployees.updatePassword(employee.getIdUser(), newPassword);
                                employee.setPassword(newPassword);
                                changePasswordProgress.dismiss();
                                updatePasswordFragment.tietNewPassword.setText("");
                                updatePasswordFragment.tietConfirmNewPassword.setText("");
                                back();
                                reviePasswordFragment.tietPassword.setText("");
                                back();
                                tietEmail.setText("");
                                Toast.makeText(ChangePasswordActivity.this, "La contraseña se actualizo correctamente", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                changePasswordProgress.dismiss();
                                Toast.makeText(ChangePasswordActivity.this, "Error al cambiar la contraseña", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        changePasswordProgress.dismiss();
                        Toast.makeText(ChangePasswordActivity.this, "Error al cambiar la contraseña", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                changePasswordProgress.dismiss();
                Toast.makeText(ChangePasswordActivity.this, "Error al cambiar la contraseña", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black, this.getTheme()));
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
    }
}