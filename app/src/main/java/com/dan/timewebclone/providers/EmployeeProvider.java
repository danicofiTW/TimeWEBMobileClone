package com.dan.timewebclone.providers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.db.DbHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class EmployeeProvider {
    private CollectionReference collection;

    //Instancia
    public EmployeeProvider(){
        collection = FirebaseFirestore.getInstance().collection("Employees");
    }

    //Obtener informacion de usuario
    public DocumentReference getUserInfo(String idUser) {
        return collection.document(idUser);
    }

    //Crear empleado
    public Task<Void> create (Employee employee){
        return collection.document(employee.getIdUser()).set(employee);
    }

    //Actualizar Imagen
    public Task<Void> updateImage(String id, String url){
        Map<String, Object> map = new HashMap<>();
        map.put("image",url);
        return collection.document(id).update(map);
    }

    /*public Task<Void> updateUrl(String id, String url){
        Map<String, Object> map = new HashMap<>();
        map.put("url",url);
        return collection.document(id).update(map);
    }*/

    //Actualizar password
    public Task<Void> updatePassword(String id, String password){
        Map<String, Object> map = new HashMap<>();
        map.put("password", password);
        return collection.document(id).update(map);
    }

    //Actualizar nombre de usuario
    public Task<Void> updateUserName(String id, String name){
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        return collection.document(id).update(map);
    }

    //Actualizar telefono
    public Task<Void> updatePhone(String id, String phone){
        Map<String, Object> map = new HashMap<>();
        map.put("phone", phone);
        return collection.document(id).update(map);
    }

    //Actualizar la compania del usuario
    public Task<Void> updateCompany(String id, String company){
        Map<String, Object> map = new HashMap<>();
        map.put("company", company);
        return collection.document(id).update(map);
    }

    //Guardar ubicacion del usuario
    public Task<Void> saveLocation(String id, LatLng location){
        Map<String, Object> map = new HashMap<>();
        map.put("checkLat", location.latitude);
        map.put("checkLong", location.longitude);
        return collection.document(id).update(map);
    }

    //Actualizar ajuste de la camara
    public Task<Void> updateStateCamera(String id, boolean stateCamera){
        Map<String, Object> map = new HashMap<>();
        map.put("stateCamera", stateCamera);
        return collection.document(id).update(map);
    }

    public Task<Void> updateStateBiometrics(String id, boolean stateBiometrics){
        Map<String, Object> map = new HashMap<>();
        map.put("stateBiometrics", stateBiometrics);
        return collection.document(id).update(map);
    }

}
