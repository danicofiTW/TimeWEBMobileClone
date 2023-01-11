package com.dan.timewebclone.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dan.timewebclone.R;
import com.dan.timewebclone.activitys.ChangePasswordActivity;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.google.android.material.textfield.TextInputEditText;


public class UpdatePasswordFragment extends Fragment {
    private DbEmployees dbEmployees;
    private AuthProvider authProvider;
    private Employee employee;
    private ChangePasswordActivity myContext;

    public TextInputEditText tietNewPassword, tietConfirmNewPassword;
    public TextView textViewPasswordConfirm;

    public UpdatePasswordFragment() {
        // Required empty public constructor
    }


    public static UpdatePasswordFragment newInstance(String param1, String param2) {
        UpdatePasswordFragment fragment = new UpdatePasswordFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        myContext = (ChangePasswordActivity) context;
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tietNewPassword = view.findViewById(R.id.textInputNewPassword);
        tietConfirmNewPassword = view.findViewById(R.id.textInputConfirmNewPassword);
        textViewPasswordConfirm = view.findViewById(R.id.textViewPasswordConfirm);
        dbEmployees = new DbEmployees(myContext);
        authProvider = new AuthProvider();
        employee = dbEmployees.getEmployee(authProvider.getId());
        textViewPasswordConfirm.setText(employee.getName());
    }
}