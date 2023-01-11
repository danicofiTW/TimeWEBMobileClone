package com.dan.timewebclone.fragments;

import android.content.Context;
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
import android.widget.Button;

import com.dan.timewebclone.activitys.MainActivity;
import com.dan.timewebclone.R;

public class InicioFragment extends Fragment {

   Button btnGoToRegister, btnGoToLogin;
    RegisterFragment registerFragment;
    LoginFragment loginFragment;
    private MainActivity myContext;
    FragmentTransaction fragmentTransaction;
    FragmentManager fragmentManager;
    DialogFragment termsAndConditionsFragment;
    Fragment oldFragment;

    @Override
    public void onAttach(@NonNull Context context) {
        myContext = (MainActivity) context;
        super.onAttach(context);
    }

    public InicioFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Toast.makeText(getContext(), "Navego correctamente", Toast.LENGTH_SHORT).show();
        btnGoToRegister = (Button) view.findViewById(R.id.btnGoToRegister);
        btnGoToLogin = (Button) view.findViewById(R.id.btnGoToLogin);
        termsAndConditionsFragment = new TermsAndConditionsFragment();
       // oldFragment = myContext.getSupportFragmentManager().findFragmentByTag(registerFragment.getTag());


        btnGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              goToRegister();
            }
        });

        btnGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginFragment = new LoginFragment();
                fragmentManager = myContext.getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frameLayoutMain, loginFragment ).commit();
                termsAndConditionsFragment.show(fragmentManager, "TermsAndConditionsFragment");
            }
        });

    }

    private void goToRegister() {
        fragmentManager = myContext.getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.frameLayoutMain, registerFragment).hide(this).show(new InicioFragment()).addToBackStack(null).commit();
            termsAndConditionsFragment.show(fragmentManager, "TermsAndConditionsFragment");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

}