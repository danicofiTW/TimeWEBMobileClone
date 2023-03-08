package com.dan.timewebclone.activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dan.timewebclone.R;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.utils.Utils;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.MapsInitializer;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.SupportMapFragment;
import com.huawei.hms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowLocationHuaweiActivity extends AppCompatActivity implements OnMapReadyCallback{

    private ImageView imageViewBackShowLocation;
    private CircleImageView circleImageViewPhotoShow, circleImageViewTipeShowCheck;
    private TextView textViewName, textViewLocation, textViewDate;
    private double mExtraLatitud, mExtraLongitud;
    private Long mExtraDate;
    private String mExtraId, mExtraTipe;
    private Check check;
    private HuaweiMap hmapSh;
    private com.huawei.hms.maps.model.Marker markerSh;
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String APY_KEY = "DAEDAHiidNYFRwwGIFtnRv1diOv0FG60k+seNMFCsNtRjh3gTAJ7ZBlMWo6vFvAnPz8bt9jytDQqUkkCiqu6eWw8dFVPWsd3EKh1wQ==";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(this);
        setContentView(R.layout.activity_show_location_huawei);

        MapsInitializer.setApiKey(APY_KEY);
        com.huawei.hms.maps.SupportMapFragment mSupportMapFragment;
        mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragmentShow);
        mSupportMapFragment.getMapAsync(this);

        setStatusBarColor();

        imageViewBackShowLocation = findViewById(R.id.imageViewBackShowLocation);
        circleImageViewPhotoShow = findViewById(R.id.circleImageViewShowCheck);
        circleImageViewTipeShowCheck = findViewById(R.id.circleImageViewTipeShowCheck);
        textViewDate = findViewById(R.id.textViewShowDate);
        textViewLocation = findViewById(R.id.textViewShowLocation);
        textViewName = findViewById(R.id.textViewShowName);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mExtraLatitud = getIntent().getDoubleExtra("lat",0);
        mExtraLongitud = getIntent().getDoubleExtra("lng", 0);
        mExtraDate = getIntent().getLongExtra("date", 0);
        mExtraTipe = getIntent().getStringExtra("tipe");
        mExtraId = getIntent().getStringExtra("idCheck");

        DbChecks dbChecks = new DbChecks(this);
        check = dbChecks.getCheck(mExtraId);

        setInfo();

        imageViewBackShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    //Obtener la informacion del registro
    private void setInfo() {
        mostrarImagen();
        Date aux = new Date(mExtraDate);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String date = sdf.format(aux);
        textViewDate.setText(date);

       if(check.getIdGeocerca() == null){
            try {
                Geocoder geocoder = new Geocoder(this);
                List<Address> addressList = geocoder.getFromLocation(mExtraLatitud, mExtraLongitud, 1);
                String city = addressList.get(0).getLocality();
                //String country = addressList.get(0).getCountryName();
                String address = addressList.get(0).getAddressLine(0);
                textViewLocation.setText(address + " " + city);

            } catch (IOException e) {
                Log.d("Error:", "Mensaje de error: " + e.getMessage());
            }
        } else {
            textViewLocation.setText(check.getNameGeocerca());
        }
        DbEmployees dbEmployees = new DbEmployees(this);
        Employee employee = dbEmployees.getEmployee(check.getIdUser());
        textViewName.setText(employee.getName());
    }

    //Mostrar imagen
    private void mostrarImagen() {
        if(check.getImage90() != null){
            try{
                byte[] decodedString = Base64.decode(check.getImage90(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if(decodedByte != null){
                    circleImageViewPhotoShow.setImageBitmap(decodedByte);
                    viewTipeCheck();
                    openImageView();
                } else {
                    defaultImage();
                }
            }
            catch(Exception e){
                e.getMessage();
            }
        } else {
            defaultImage();
        }
    }

    //Mostrar imagen por defecto cuando el registro no cuenta con imagen
    private void defaultImage(){
        if(mExtraTipe.equals("startWork")){
            circleImageViewPhotoShow.setImageResource(R.drawable.icon_int);
        } else if(mExtraTipe.equals("startEating")){
            circleImageViewPhotoShow.setImageResource(R.drawable.icon_comer);
        } else if(mExtraTipe.equals("finishEating")){
            circleImageViewPhotoShow.setImageResource(R.drawable.icon_termincomer);
        } else if(mExtraTipe.equals("finishWork")){
            circleImageViewPhotoShow.setImageResource(R.drawable.icon_out);
        }
        circleImageViewTipeShowCheck.setVisibility(View.GONE);
    }


    //Mostrar tipo de registro cuando hay imagen
    private void viewTipeCheck(){
        if(mExtraTipe.equals("startWork")){
            circleImageViewTipeShowCheck.setImageResource(R.drawable.icon_int);
        } else if(mExtraTipe.equals("startEating")){
            circleImageViewTipeShowCheck.setImageResource(R.drawable.icon_comer);
        } else if(mExtraTipe.equals("finishEating")){
            circleImageViewTipeShowCheck.setImageResource(R.drawable.icon_termincomer);
        } else if(mExtraTipe.equals("finishWork")){
            circleImageViewTipeShowCheck.setImageResource(R.drawable.icon_out);
        }
        circleImageViewTipeShowCheck.setVisibility(View.VISIBLE);
    }

    //Abrir imagen si es que tiene
    private void openImageView() {
        circleImageViewPhotoShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ShowLocationHuaweiActivity.this, ShowImageActivity.class);
                i.putExtra("idCheck", check.getIdCheck());
                startActivity(i);
            }
        });
    }


    //Cambiar el color de la barra de notificaciones
    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorHomeTw, this.getTheme()));
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorHomeTw));
        }
    }

    @Override
    public void onMapReady(HuaweiMap huaweiMap) {
        hmapSh = huaweiMap;
        if (hmapSh != null) {
            hmapSh.setMapType(HuaweiMap.MAP_TYPE_NORMAL);
            hmapSh.setMyLocationEnabled(false);
            hmapSh.getUiSettings().setScrollGesturesEnabled(false);
            hmapSh.getUiSettings().setZoomControlsEnabled(false);
            hmapSh.getUiSettings().setAllGesturesEnabled(false);
            hmapSh.getUiSettings().setIndoorLevelPickerEnabled(false);
            //startLocation();
        }

        if(markerSh!=null){
            markerSh.remove();
        }

        // MapImage mapImage = MapImageFactory.fromResource(getResources(), R.drawable.here_car);
        markerSh = hmapSh.addMarker(new MarkerOptions().position(new com.huawei.hms.maps.model.LatLng(mExtraLatitud, mExtraLongitud))
                .icon(Utils.bitmapDescriptorFromVector(this,R.drawable.icon_employee_48)));

        CameraUpdate cameraPosition = CameraUpdateFactory.newLatLngZoom(new com.huawei.hms.maps.model.LatLng(mExtraLatitud, mExtraLongitud), 18f);
        hmapSh.moveCamera(cameraPosition);
    }

    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }*/

}