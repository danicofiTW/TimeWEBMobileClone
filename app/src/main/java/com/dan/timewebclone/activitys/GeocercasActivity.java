package com.dan.timewebclone.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.UFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dan.timewebclone.R;
import com.dan.timewebclone.adapters.ChecksDbAdapter;
import com.dan.timewebclone.adapters.GeocercasDbAdapter;
import com.dan.timewebclone.db.DbBitacoras;
import com.dan.timewebclone.db.DbGeocercas;
import com.dan.timewebclone.models.Bitacora;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.models.Geocerca;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.BitacoraProvider;
import com.dan.timewebclone.providers.GeocercaProvider;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.rpc.context.AttributeContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class GeocercasActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private SearchView searchView;
    private EditText editTextSearch;
    private TextView textViewNoGeocercas;
    private AlertDialog.Builder builderDialogExit;

    private MenuItem menuItemSearch;
    private RecyclerView mReciclerView;
    private LinearLayoutManager linearLayoutManager;
    private ConstraintLayout constraintLayoutProgress;

    private GeocercaProvider geocercaProvider;
    private BitacoraProvider bitacoraProvider;
    private AuthProvider authProvider;
    private DbBitacoras dbBitacoras;
    private DbGeocercas dbGeocercas;
    private GeocercasDbAdapter geocercasDbAdapter;

    private ArrayList<String> idGeocercas;
    private ArrayList<String> idBitacoras;
    private ArrayList<Geocerca> geocercas;
    private ArrayList<Bitacora> bitacoras;
    private int numberIdsGeo = 0;
    private boolean notComeBack;
    private boolean updateForOne;
    private boolean reviewSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geocercas);
        setStatusBarColor();

        toolbar = findViewById(R.id.toolbar_geocercas);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Geocercas");
        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        textViewNoGeocercas = findViewById(R.id.textViewNoGeocercas);
        mReciclerView = findViewById(R.id.rvGeocercas);
        constraintLayoutProgress = findViewById(R.id.progressLayout);
        builderDialogExit = new AlertDialog.Builder(this);
        linearLayoutManager = new LinearLayoutManager(this);
        mReciclerView.setLayoutManager(linearLayoutManager);
        mReciclerView.setHasFixedSize(true);
        updateForOne = false;


        notComeBack = getIntent().getBooleanExtra("notComeBack",false);
        reviewSettings = getIntent().getBooleanExtra("reviewSettings",false);

        geocercaProvider = new GeocercaProvider();
        bitacoraProvider = new BitacoraProvider();
        authProvider = new AuthProvider();
        dbBitacoras = new DbBitacoras(this);
        dbGeocercas = new DbGeocercas(this);
        idGeocercas = new ArrayList<>();
        geocercas = new ArrayList<>();
        bitacoras = new ArrayList<>();
        idBitacoras = new ArrayList<>();

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.itemUpdate){
                    updateGeocercas();
                }
                return true;
            }
        });

        getGeocercas();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_geocerca, menu);
        //Ajustes SearchView
        menuItemSearch = menu.findItem(R.id.actionSearchGeocerca);
        searchView = (SearchView) menuItemSearch.getActionView();
        editTextSearch = (EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        editTextSearch.setTextColor(getResources().getColor(R.color.white));
        editTextSearch.setHintTextColor(getResources().getColor(R.color.white));
        searchView.setQueryHint("Buscar...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTextSearchView(newText);
                return true;
            }
        });
        return true;
    }

    public void getGeocercas() {
        geocercasDbAdapter = new GeocercasDbAdapter(geocercas, GeocercasActivity.this);
        mReciclerView.setAdapter(geocercasDbAdapter);
        geocercasDbAdapter.notifyDataSetChanged();
        if(isOnlineNet() && !updateForOne){
            updateForOne = true;
            updateGeocercas();
        } else {
            bitacoras = dbBitacoras.getBitacorasByIdUser(authProvider.getId());
            if(bitacoras.size() != 0){
                for(int i = 0; i < bitacoras.size(); i++){
                    if(!idBitacoras.contains(bitacoras.get(i).getIdBitacora())){
                        idBitacoras.add(bitacoras.get(i).getIdBitacora());
                    }
                    if(!idGeocercas.contains(bitacoras.get(i).getIdGeocerca()))
                    idGeocercas.add(bitacoras.get(i).getIdGeocerca());
                }
                if(idGeocercas.size() != 0){
                    for(int j = 0; j < idGeocercas.size(); j++){
                        if(!geocercas.contains(dbGeocercas.getGeocerca(idGeocercas.get(j))))
                        geocercas.add(dbGeocercas.getGeocerca(idGeocercas.get(j)));
                    }
                    if(geocercas.size()!=0){
                        //Collections.reverse(geocercas);

                        geocercasDbAdapter.updateGeocercas(geocercas);
                        textViewNoGeocercas.setVisibility(View.GONE);
                    } else {
                        SharedPreferences sharedPref = getSharedPreferences("geocerca", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putFloat("geoLat", 0);
                        editor.putFloat("geoLong", 0);
                        editor.putFloat("geoRadio", 0);
                        editor.putString("idGeocerca", "");
                        editor.apply();
                        editor.commit();
                        if(notComeBack){
                            goNotBack();
                        } else {
                            textViewNoGeocercas.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    SharedPreferences sharedPref = getSharedPreferences("geocerca", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putFloat("geoLat", 0);
                    editor.putFloat("geoLong", 0);
                    editor.putFloat("geoRadio", 0);
                    editor.putString("idGeocerca", "");
                    editor.apply();
                    editor.commit();
                    if(notComeBack){
                        goNotBack();
                    } else {
                        textViewNoGeocercas.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                updateGeocercas();
                SharedPreferences sharedPref = getSharedPreferences("geocerca", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putFloat("geoLat", 0);
                editor.putFloat("geoLong", 0);
                editor.putFloat("geoRadio", 0);
                editor.putString("idGeocerca", "");
                editor.apply();
                editor.commit();
                if(notComeBack){
                    goNotBack();
                } else {
                    textViewNoGeocercas.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void goToHome(float geoLat, float geoLong, float geoRadio, String idGeocerca){

        SharedPreferences sharedPref = getSharedPreferences("geocerca", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat("geoLat", geoLat);
        editor.putFloat("geoLong", geoLong);
        editor.putFloat("geoRadio", geoRadio);
        editor.putString("idGeocerca", idGeocerca);
        editor.apply();
        editor.commit();
        //finish();
        if(notComeBack){
            Intent in = new Intent(this, HomeTW.class);
            /*in.putExtra("geoLat", geoLat);
            in.putExtra("geoLong", geoLong);*/
            in.putExtra("geoRadio", geoRadio);
            in.putExtra("notRevie", true);
            //if(reviewSettings){
                in.putExtra("reviewSettings", true);
            //}
            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(in);
        } else {
            finish();
        }
    }

    private void goNotBack(){
        Intent in = new Intent(this, HomeTW.class);
        //in.putExtra("geoRadio", geoRadio);
        in.putExtra("notRevie", true);
        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(in);
    }

    private void updateGeocercas() {
        if(isOnlineNet()){
            if(bitacoraProvider!=null){
                if(geocercaProvider!=null){
                    constraintLayoutProgress.setVisibility(View.VISIBLE);
                    bitacoraProvider.getBitacorasByUser(authProvider.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                bitacoras.clear();
                                idBitacoras.clear();
                                idGeocercas.clear();
                                geocercas.clear();
                                dbGeocercas.deleteAllGeocercas();
                                dbBitacoras.deleteAllBitacoras();
                                for (DocumentSnapshot document : task.getResult()) {
                                    if(!idBitacoras.contains(document.toObject(Bitacora.class).getIdBitacora())){
                                        dbBitacoras.insertBitacora(document.toObject(Bitacora.class));
                                        idBitacoras.add(document.toObject(Bitacora.class).getIdBitacora());
                                        if(!idGeocercas.contains(document.toObject(Bitacora.class).getIdGeocerca()))
                                        idGeocercas.add(document.toObject(Bitacora.class).getIdGeocerca());
                                    }
                                }
                                if(idGeocercas.size() != 0){
                                    for (numberIdsGeo = 0; numberIdsGeo < idGeocercas.size(); numberIdsGeo++) {
                                        geocercaProvider.getGeocerca(idGeocercas.get(numberIdsGeo)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful()){
                                                    dbGeocercas.insertGeocerca(task.getResult().toObject(Geocerca.class));
                                                    geocercas.add(task.getResult().toObject(Geocerca.class));
                                                    if(numberIdsGeo ==  idGeocercas.size()-1 || numberIdsGeo == idGeocercas.size()){
                                                        if(geocercas.size() != 0){
                                                            Collections.reverse(geocercas);
                                                            if(geocercasDbAdapter!=null){
                                                                constraintLayoutProgress.setVisibility(View.GONE);
                                                                textViewNoGeocercas.setVisibility(View.GONE);
                                                                geocercasDbAdapter.updateGeocercas(geocercas);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    if(notComeBack){
                                        constraintLayoutProgress.setVisibility(View.GONE);
                                        goNotBack();
                                    } else {
                                        constraintLayoutProgress.setVisibility(View.GONE);
                                        geocercasDbAdapter.notifyDataSetChanged();
                                        textViewNoGeocercas.setVisibility(View.VISIBLE);
                                    }
                                }
                            } else {
                                constraintLayoutProgress.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        } else {
            Toast.makeText(this, "Conectate a internet para actualizar las geocercas", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterTextSearchView(String newText) {
        ArrayList<Geocerca> mGeocercas = dbGeocercas.getAllGeocercas();
        ArrayList<Geocerca> mGeocercasFilter = new ArrayList<>();
        for (int i = 0; i < mGeocercas.size(); i++) {
            String nameGeo = mGeocercas.get(i).getGeoNombre();
            if (nameGeo.toLowerCase().contains(newText.toLowerCase())) {
                mGeocercasFilter.add(mGeocercas.get(i));
            } else {
                String directGeo = mGeocercas.get(i).getDireccion();
                if(directGeo.toLowerCase().contains(newText.toLowerCase())){
                    mGeocercasFilter.add(mGeocercas.get(i));
                }
            }
        }
        geocercasDbAdapter.filtrar(mGeocercasFilter);
    }

    public boolean isOnlineNet() {
        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");
            int val = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if(notComeBack){
            mostrarSalida();
        } else {
            super.onBackPressed();
        }
    }

    public void mostrarSalida(){
        builderDialogExit.setMessage("¿Deseas salir de TimeWEBMobile?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Intent in = new Intent(Intent.ACTION_MAIN);
                        in.addCategory(Intent.CATEGORY_HOME);
                        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(in);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setNeutralButton("", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        builderDialogExit.show();
    }

    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorHomeTw, this.getTheme()));
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorHomeTw));
        }
    }


}