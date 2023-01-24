package com.dan.timewebclone.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class AuthProvider {

    private FirebaseAuth mAuth;

    //Instancia
   public AuthProvider() {
        mAuth = FirebaseAuth.getInstance();
    }

    /*public Task<AuthResult> signInPhone(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        return mAuth.signInWithCredential(credential);
    }*/

    //Registrar usuario
    public Task<AuthResult> register(String email, String password){
        return mAuth.createUserWithEmailAndPassword(email,password);
    }

    //Ingresar con correo electronico
    public Task<AuthResult> loginEmail(String email, String password){
        return mAuth.signInWithEmailAndPassword(email,password);
    }

    //Cerrar sesion
    public void signOut() {
        mAuth.signOut();
    }

    //Obtener id de Usuario logeado
    public String getId() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        else {
            return null;
        }
    }

    //Saber si existe una sesion
    public boolean existSesion(){
        boolean exist = false;
        if(mAuth.getCurrentUser()!=null){
            exist = true;
        }
        return  exist;
    }

    //Obtener contraseña de usuario
    public Task<Void> getCredential(String email, String password){
        AuthCredential authCredential = EmailAuthProvider.getCredential(email, password);
        return mAuth.getCurrentUser().reauthenticate(authCredential);
        //return mAuth.sendPasswordResetEmail(email);
    }

    //Actualizar contraseña
    public Task<Void> updatePassword(String newPassword){
         return mAuth.getCurrentUser().updatePassword(newPassword);
    }

}
