package com.dan.timewebclone.providers;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.db.DbHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.huawei.agconnect.AGConnectOptionsBuilder;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.support.api.push.service.HmsMsgService;

import java.util.HashMap;
import java.util.Map;

public class EmployeeProvider {
    private CollectionReference collection;
    private DbEmployees dbEmployees;

    private String appId = "107706035";
    private String tokenScope = "HCM";
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

    public void updateToken(String id, Context context){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String token) {
                dbEmployees = new DbEmployees(context);
                dbEmployees.updateTokenDB(id,token);
                Map<String, Object> map = new HashMap<>();
                map.put("token", token);
                collection.document(id).update(map);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //getToken(id, context);
                Toast.makeText(context, "Problema al obtener token", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateTokenHMS(String id, Context context) {

        new Thread() {
            @Override
            public void run() {
                try {
                    //String APY_KEY = "DAEDAHiidNYFRwwGIFtnRv1diOv0FG60k+seNMFCsNtRjh3gTAJ7ZBlMWo6vFvAnPz8bt9jytDQqUkkCiqu6eWw8dFVPWsd3EKh1wQ==";

                    String token = HmsInstanceId.getInstance(context).getToken(appId, tokenScope);
                    if(!TextUtils.isEmpty(token)) {
                        sendRegTokenToServer(id, token, context);
                    } else {
                        //Toast.makeText(context, "Problema al obtener token", Toast.LENGTH_SHORT).show();
                    }
                } catch (ApiException e) {
                    Log.e(TAG, "TOKEN HMS ERROR: " + e);
                    //Toast.makeText(context, "Problema al obtener token", Toast.LENGTH_SHORT).show();
                }
            }
        }.start();
    }
    private void sendRegTokenToServer(String id, String token, Context context) {
        dbEmployees = new DbEmployees(context);
        dbEmployees.updateTokenDB(id,token);
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        collection.document(id).update(map);
        //Toast.makeText(context, "Se obtubo token HMS", Toast.LENGTH_SHORT).show();
    }

    public void deleteTokenHMS(String idUser, Context context){
        new Thread() {
            @Override
            public void run() {
                try {
                    dbEmployees = new DbEmployees(context);
                    dbEmployees.updateTokenDB(idUser,"");
                    Map<String, Object> map = new HashMap<>();
                    map.put("token", "");
                    collection.document(idUser).update(map);
                    HmsInstanceId.getInstance(context).deleteToken(appId, tokenScope);
                    Log.i(TAG, "token deleted successfully");
                } catch (ApiException e) {
                    Log.e(TAG, "deleteToken failed." + e);
                }
            }
        }.start();
    }

    public void deleteToken(String idUser, Context context){
        dbEmployees = new DbEmployees(context);
        dbEmployees.updateTokenDB(idUser,"");
        Map<String, Object> map = new HashMap<>();
        map.put("token", "");
        collection.document(idUser).update(map);
        FirebaseMessaging.getInstance().deleteToken();
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
