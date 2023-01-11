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
import com.dan.timewebclone.activitys.MainActivity;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.google.android.material.textfield.TextInputEditText;

public class ReviePasswordFragment extends Fragment {
    private ChangePasswordActivity myContext;
    public TextInputEditText tietPassword;
    public TextView textViewEmailConfirm;
    private DbEmployees dbEmployees;
    private AuthProvider authProvider;
    private Employee employee;

    public ReviePasswordFragment() {
        // Required empty public constructor
    }

    public static ReviePasswordFragment newInstance(String param1, String param2) {
        ReviePasswordFragment fragment = new ReviePasswordFragment();
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
        return inflater.inflate(R.layout.fragment_revie_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tietPassword = view.findViewById(R.id.textInputPassword);
        textViewEmailConfirm = view.findViewById(R.id.textViewEmailConfirm);
        dbEmployees = new DbEmployees(myContext);
        authProvider = new AuthProvider();
        employee = dbEmployees.getEmployee(authProvider.getId());
        textViewEmailConfirm.setText(employee.getName());
    }
}