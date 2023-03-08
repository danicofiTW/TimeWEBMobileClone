package com.dan.timewebclone.activitys;

import static com.dan.timewebclone.fragments.MapFragment.circleImageViewMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dan.timewebclone.R;
import com.dan.timewebclone.adapters.ChecksDbAdapter;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowLocationActivity extends AppCompatActivity implements OnMapReadyCallback {


    private static WeakReference<Activity> mActivityRef;
    private ImageView imageViewBackShowLocation;
    private CircleImageView circleImageViewPhotoShow, circleImageViewTipeShowCheck;
    private TextView textViewName, textViewLocation, textViewDate;
    private double mExtraLatitud, mExtraLongitud;
    private Long mExtraDate;
    private String mExtraId, mExtraTipe;
    private SupportMapFragment mapFragmentLocation;
    private GoogleMap map;
    private Marker marker;
    private Check check;
    private AuthProvider authProvider;
    private boolean showForNotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_location);
        setStatusBarColor();

        imageViewBackShowLocation = findViewById(R.id.imageViewBackShowLocation);
        circleImageViewPhotoShow = findViewById(R.id.circleImageViewShowCheck);
        circleImageViewTipeShowCheck = findViewById(R.id.circleImageViewTipeShowCheck);
        textViewDate = findViewById(R.id.textViewShowDate);
        textViewLocation = findViewById(R.id.textViewShowLocation);
        textViewName = findViewById(R.id.textViewShowName);
        authProvider = new AuthProvider();

        mapFragmentLocation = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapLocation);
        if(mapFragmentLocation != null){
            mapFragmentLocation.getMapAsync(this);
        }

        mExtraLatitud = getIntent().getDoubleExtra("lat",0);
        mExtraLongitud = getIntent().getDoubleExtra("lng", 0);
        mExtraDate = getIntent().getLongExtra("date", 0);
        mExtraTipe = getIntent().getStringExtra("tipe");
        mExtraId = getIntent().getStringExtra("idCheck");
        showForNotify = getIntent().getBooleanExtra("showForNotify", false);


        DbChecks dbChecks = new DbChecks(ShowLocationActivity.this);
        check = dbChecks.getCheck(mExtraId);

        if( check != null){
            if(mExtraLatitud == 0){
                mExtraLatitud = check.getCheckLat();
            }
            if(mExtraLongitud == 0){
                mExtraLongitud = check.getCheckLong();
            }
            if(mExtraDate == 0){
                mExtraDate = check.getTime();
            }
            if(mExtraTipe == null){
                mExtraTipe = check.getTipeCheck();
            }
            setInfo();
        }

        imageViewBackShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mActivityRef != null && mActivityRef.get() != null){
                    if(mActivityRef.get().isDestroyed()) {
                        Intent intent1 = new Intent(ShowLocationActivity.this, MainActivity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent1.setAction(Intent.ACTION_RUN);
                        startActivity(intent1);
                    } else {
                        finish();
                    }
                } else {
                    Intent intent1 = new Intent(ShowLocationActivity.this, MainActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent1.setAction(Intent.ACTION_RUN);
                    startActivity(intent1);
                }
                //finish();
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
        DbEmployees dbEmployees = new DbEmployees(ShowLocationActivity.this);
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
                Intent i = new Intent(ShowLocationActivity.this, ShowImageActivity.class);
                i.putExtra("idCheck", check.getIdCheck());
                startActivity(i);
            }
        });
    }

    //Mostrar el mapa y el marcador
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setScrollGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);

        if(marker!=null){
            marker.remove();
        }
        marker = map.addMarker(new MarkerOptions().position(new LatLng(mExtraLatitud, mExtraLongitud))
                .title("Tu posicion")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_employee_48)));

        map.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(mExtraLatitud, mExtraLongitud))
                        .zoom(18f)
                        .build()
        ));
    }

    public static void updateActivity(Activity activity) {
        mActivityRef = new WeakReference<Activity>(activity);
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

}