package com.dan.timewebclone.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class AuthProvider {

    private FirebaseAuth mAuth;

   public AuthProvider() {
        mAuth = FirebaseAuth.getInstance();
    }

    /*public Task<AuthResult> signInPhone(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        return mAuth.signInWithCredential(credential);
    }*/

    public Task<AuthResult> register(String email, String password){
        return mAuth.createUserWithEmailAndPassword(email,password);
    }

    public Task<AuthResult> loginEmail(String email, String password){
        return mAuth.signInWithEmailAndPassword(email,password);
    }

    public void signOut() {
        mAuth.signOut();
    }

    public String getId() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        else {
            return null;
        }
    }

    public boolean existSesion(){
        boolean exist = false;
        if(mAuth.getCurrentUser()!=null){
            exist = true;
        }
        return  exist;
    }

    public Task<Void> getCredential(String email, String password){
        AuthCredential authCredential = EmailAuthProvider.getCredential(email, password);
        return mAuth.getCurrentUser().reauthenticate(authCredential);
        //return mAuth.sendPasswordResetEmail(email);
    }

    public Task<Void> updatePassword(String newPassword){
         return mAuth.getCurrentUser().updatePassword(newPassword);
    }

}
