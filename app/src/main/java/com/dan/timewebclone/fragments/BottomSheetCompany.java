package com.dan.timewebclone.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dan.timewebclone.R;
import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.activitys.ProfileActivity;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetCompany extends BottomSheetDialogFragment {

    private static Context myContext;
    EmployeeProvider employeeProvider;
    //FirebaseAuth mAuth;
    AuthProvider authProvider;
    Button buttonSave, buttonCancel;
    EditText editTextCompany;

    String company;

    public static BottomSheetCompany newInstance(String company, Context context){
        BottomSheetCompany bottomSheetSelectImage = new BottomSheetCompany();
        Bundle args = new Bundle();
        myContext = context;

        args.putString("company", company);
        bottomSheetSelectImage.setArguments(args);
        //myContext = context;
        return bottomSheetSelectImage;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        company = getArguments().getString("company");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_company, container, false);

        buttonSave = view.findViewById(R.id.btnSave);
        buttonCancel = view.findViewById(R.id.btnCancel);
        editTextCompany = view.findViewById(R.id.editTextCompany);

        employeeProvider = new EmployeeProvider();
        //mAuth = FirebaseAuth.getInstance();
        authProvider = new AuthProvider();

        editTextCompany.setText(company);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCompany();
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

    private void updateCompany() {
        String company = editTextCompany.getText().toString();
        DbEmployees dbEmployees = new DbEmployees(myContext);
        dbEmployees.updateCompany(authProvider.getId(), company);
        dismiss();
        if (!company.equals("")) {
                employeeProvider.updateCompany(authProvider.getId(), company).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(myContext, "El nombre de tu empresa se ha actualizado", Toast.LENGTH_SHORT).show();
                }
            });
            }
        ((ProfileActivity)getActivity()).getUserInfo();
    }
}