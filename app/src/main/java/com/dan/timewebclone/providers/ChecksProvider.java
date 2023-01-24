package com.dan.timewebclone.providers;

import android.content.Context;

import androidx.annotation.NonNull;

import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.models.Check;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChecksProvider {

    CollectionReference mCollection;

    //Instancia
    public ChecksProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Checks");
    }

    //Crear un registro
    public Task<Void> createCheck(Check check) {
        DocumentReference document = mCollection.document();
        check.setIdCheck(document.getId());
        return document.set(check);
    }

    //Obtener registros por usuario
    public Query getChecksByUser(String idUser) {
        return mCollection.whereEqualTo("idUser", idUser).orderBy("time", Query.Direction.ASCENDING);
    }

    //Obtener registros por usuario y estado de enviado
    public Query getChecksByUserAndStatusSend(String idUser, int statusSend) {
        return mCollection.whereEqualTo("idUser", idUser).whereEqualTo("statusSend", statusSend).orderBy("time", Query.Direction.ASCENDING);
    }

    //Obtener registros no enviados por usuario
    public Query getChecksNotSend(String idUser) {
        return mCollection.whereEqualTo("idUser", idUser).whereEqualTo("statusSend", 0);
    }


    //Actualizar estado de enviado
    public void updateStatus(String idCheck, int tipeSend, long timeSend){
        mCollection.document(idCheck).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                        Map<String, Object> map = new HashMap<>();
                        map.put("statusSend", tipeSend);
                        map.put("timeSend", timeSend);
                        mCollection.document(idCheck).update(map);
                }
            }
        });
    }

    //Eliminar registro
    public void deleteCheck(Check ch) {
        mCollection.document(ch.getIdCheck()).delete();
    }

    //Eliminar registros por id
    public void deleteChecksForId(ArrayList<String> idsChecks) {
        for(int i = 0; i < idsChecks.size(); i++) {
            mCollection.document(idsChecks.get(i)).delete();
        }
    }

    /*public Query getCheckByChat(String idUser) {
        return mCollection.whereEqualTo("idUser", idUser).orderBy("time", Query.Direction.DESCENDING);
    }*/

}
