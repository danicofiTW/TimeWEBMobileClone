package com.dan.timewebclone.fragments;

import android.content.Context;
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
import com.dan.timewebclone.activitys.ProfileActivity;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetUserName extends BottomSheetDialogFragment {

    private static Context myContext;
    EmployeeProvider employeeProvider;
    //FirebaseAuth mAuth;
    AuthProvider authProvider;
    Button buttonSave, buttonCancel;
    EditText editTextUserName;

    String username;

    public static BottomSheetUserName newInstance(String username, Context context){
        BottomSheetUserName bottomSheetSelectImage = new BottomSheetUserName();
        Bundle args = new Bundle();
        myContext = context;
        args.putString("username", username);
        bottomSheetSelectImage.setArguments(args);
        return bottomSheetSelectImage;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        username = getArguments().getString("username");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_user_name, container, false);

        buttonSave = view.findViewById(R.id.btnSave);
        buttonCancel = view.findViewById(R.id.btnCancel);
        editTextUserName = view.findViewById(R.id.editTextUserName);

        employeeProvider = new EmployeeProvider();
        //mAuth = FirebaseAuth.getInstance();
        authProvider = new AuthProvider();

        editTextUserName.setText(username);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserName();
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

    private void updateUserName() {
        String username = editTextUserName.getText().toString();
        DbEmployees dbEmployees = new DbEmployees(myContext);
        dbEmployees.updateName(authProvider.getId(), username);
        dismiss();
        if (!username.equals("")) {
            employeeProvider.updateUserName(authProvider.getId(), username).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(myContext, "El nombre de usuario se ha actualizado", Toast.LENGTH_SHORT).show();
                }
            });
        }
        ((ProfileActivity)getActivity()).getUserInfo();
    }
}