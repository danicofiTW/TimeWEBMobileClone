package com.dan.timewebclone.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dan.timewebclone.R;
import com.dan.timewebclone.activitys.ProfileActivity;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.dan.timewebclone.utils.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetPhone extends BottomSheetDialogFragment {

    EmployeeProvider employeeProvider;
    //FirebaseAuth mAuth;
    AuthProvider authProvider;
    Button buttonSave, buttonCancel;
    EditText editTextPhone;

    private static ProfileActivity myContext;
    String phone;

    public static BottomSheetPhone newInstance(String phone1, ProfileActivity context){
        BottomSheetPhone bottomSheetSelectImage = new BottomSheetPhone();
        Bundle args = new Bundle();
        myContext = context;
        args.putString("phone", phone1);
        bottomSheetSelectImage.setArguments(args);
        return bottomSheetSelectImage;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        phone = getArguments().getString("phone");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_phone, container, false);

        buttonSave = view.findViewById(R.id.btnSave);
        buttonCancel = view.findViewById(R.id.btnCancel);
        editTextPhone = view.findViewById(R.id.editTextPhone);

        employeeProvider = new EmployeeProvider();
        //mAuth = FirebaseAuth.getInstance();
        authProvider = new AuthProvider();

        editTextPhone.setText(phone);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePhone();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return view;
    }

    private void updatePhone() {
        String phone = editTextPhone.getText().toString();
        DbEmployees dbEmployees = new DbEmployees(myContext);
        dismiss();
        if (!phone.equals("")) {
            employeeProvider.updatePhone(authProvider.getId(), phone).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    dbEmployees.updatePhone(authProvider.getId(), phone);
                    Toast.makeText(myContext, "El telefono se ha actualizado", Toast.LENGTH_SHORT).show();
                    myContext.getUserInfo();
                }
            });
        }
        if(!Utils.isOnlineNet(myContext)){
            Toast.makeText(myContext, "Conectate a internet para actualizar correctamente tu forto de perfil", Toast.LENGTH_SHORT).show();
        }
    }
}