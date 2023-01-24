package com.dan.timewebclone.providers;

import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.models.Geocerca;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class GeocercaProvider {
    private CollectionReference collection;

    //Instancia
    public GeocercaProvider(){
        collection = FirebaseFirestore.getInstance().collection("Geocercas");
    }

    //Obtener informacion de geocerca
    public DocumentReference getGeocerca(String idGeocerca) {
        return collection.document(idGeocerca);
    }

}
