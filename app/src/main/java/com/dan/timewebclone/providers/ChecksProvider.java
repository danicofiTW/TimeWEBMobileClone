package com.dan.timewebclone.providers;

import android.content.Context;

import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.models.Check;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChecksProvider {

    CollectionReference mCollection;

    String url;

    public ChecksProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Checks");
    }

    public Task<Void> createCheck(Check check) {
        DocumentReference document = mCollection.document();
        check.setIdCheck(document.getId());
        return document.set(check);
    }

    public Query getChecksByUser(String idUser) {
        return mCollection.whereEqualTo("idUser", idUser).orderBy("time", Query.Direction.DESCENDING);
    }

    public Query getChecksByUserAndStatusSend(String idUser, int statusSend) {
        return mCollection.whereEqualTo("idUser", idUser).whereEqualTo("statusSend", statusSend).orderBy("time", Query.Direction.DESCENDING);
    }

    public Query getChecksNotSend(String idUser) {
        return mCollection.whereEqualTo("idUser", idUser).whereEqualTo("statusSend", 0);
    }


    public void updateStatus(String idCheck, int tipeSend){
        mCollection.document(idCheck).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                        Map<String, Object> map = new HashMap<>();
                        map.put("statusSend", tipeSend);
                        mCollection.document(idCheck).update(map);
                }
            }
        });
    }

    public void deleteCheck(Check ch) {
        mCollection.document(ch.getIdCheck()).delete();
    }

    /*public Query getCheckByChat(String idUser) {
        return mCollection.whereEqualTo("idUser", idUser).orderBy("time", Query.Direction.DESCENDING);
    }*/

}
