package com.dan.timewebclone.providers;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class BitacoraProvider {
    private CollectionReference collection;

    //Instancia
    public BitacoraProvider(){
        collection = FirebaseFirestore.getInstance().collection("Bitacora");
    }

    //Obtener informacion de geocerca
    public DocumentReference getBitacora(String idBitacora) {
        return collection.document(idBitacora);
    }

    //Obtener registros por usuario
    public Query getBitacorasByUser(String idUser) {
        return collection.whereEqualTo("idUser", idUser);
    }

    public Query getBitacorasByUserAndState(String idUser) {
        return collection.whereEqualTo("idUser", idUser);
    }

}
