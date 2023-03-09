package com.dan.timewebclone.fragments;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AnalogClock;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.R;
import com.dan.timewebclone.db.DbBitacoras;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.db.DbGeocercas;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.BitacoraProvider;
import com.dan.timewebclone.providers.ChecksProvider;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.dan.timewebclone.providers.GoogleApiProvider;
import com.dan.timewebclone.utils.DecodePoints;
import com.dan.timewebclone.utils.Utils;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.itbehrend.analogclockview.AnalogClockView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executor;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private AuthProvider authProvider;
    private ChecksProvider checksProvider;
    private BitacoraProvider bitacoraProvider;
    private EmployeeProvider employeeProvider;
    private DbChecks dbChecks;
    private DbGeocercas dbGeocercas;
    private DbBitacoras dbBitacoras;
    public Circle mapCircle;

    public FloatingActionsMenu floatingActionsMenu;
    private FloatingActionButton sendStartWork, sendStartEating, sendFinishEating, sendFinishWork;
    private AnalogClockView clockView;

    private GoogleMap map;
    private GoogleApiProvider googleApiProvider;
    private View mView, viewMoveLocation, viewViewRout;
    private TextView textViewGoodTime, textViewTime, textViewName, textViewState;
    private SupportMapFragment mapFragment;
    public FrameLayout frameLayoutLoading, frameLayoutMoveLocation, frameLayoutGoToGoogleMaps, frameLayoutViewRout, frameLayoutTakePhoto;
    private Toast mToast = null;
    int numberChecksSendLate = 0;

    public final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private Marker marker;
    private Button buttonConnect;
    private boolean isConnect = false;
    private boolean withoutInternet = true;
    public static CircleImageView circleImageViewMap;
    private ImageView imageViewPhotoMap, imageViewMoveLocation, imageViewRout;

    private LatLng currentLatLng;
    private HomeTW myContext;
    //private Long date;
    private Date mDateD;
    public ProgressDialog pdSendCheck;
    public Date time1;
    private String zone = "America/Mexico_City";

    private FusedLocationProviderClient fusedLocation;
    private LocationRequest locationRequest;

    private DbEmployees dbEmployees;
    public Employee employee;
    private Calendar calendar;
    private SimpleDateFormat sdf1;

    private boolean updateGeocerca = false;
    public boolean firstReviewGeoface;
    private Geofence geofencing;
    private GeofencingClient geofencingClient;
    private LatLng latLngGeoFence;
    private Marker markerDestination;
    private List<LatLng> polyLineList;
    private PolylineOptions polylineOptions;
    private Polyline routPolyLine;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private int canUseBiometrics = 0;
    private Location mLocation, mLocation2;
    private String mTipe;

    public boolean secondsIsOver = false;
    public boolean takePhoto = false;
    public boolean biometria = false;
    private boolean viewRout = false;
    private boolean moveLocation = false;
    public long timeReal;

    Handler handler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (employee != null) {
                if (Utils.isTimeAutomaticEnabled(myContext)) {
                    secondsIsOver = false;
                          mDateD = Utils.getTime();
                    String date1 = sdf1.format(mDateD.getTime());
                    if (textViewTime != null) {
                        textViewTime.setText(date1);
                    }
                } else {
                    disconnect();
                    secondsIsOver = true;
                    if (textViewTime != null) {
                        textViewTime.setText("");
                    }
                }
            } else {
                employee = dbEmployees.getEmployee(authProvider.getId());
                if (employee != null) {
                    setGreeting();
                    reviewTakePhoto();
                }
            }

            handler.postDelayed(runnable, 1000);
        }
    };

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                mLocation2 = location;
                //Log.d("UBICACION", "Location: " + mLocation.getLatitude()+ ", " + mLocation.getLongitude());
                if (Utils.isMockLocationOn(location, myContext)) {
                    disconnect();
                    Toast.makeText(myContext, "Se ha detectado ubicacion de prueba, por lo que no se puede enviar registros", Toast.LENGTH_LONG).show();
                } else {
                    currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    //date = location.getTime();

                    if (marker != null) {
                        marker.remove();
                    }
                    marker = map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("Tu posicion")
                            .anchor(0.5f,1f)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_employee_48)));

                    if(!moveLocation && !viewRout)
                        map.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                        .zoom(17f)
                                        .build()
                        ));
                    //icono del conductor

                }


                if (myContext.geoRadio != 0 && !updateGeocerca) {
                    updateGeocerca = true;
                    createGeofencig();
                }

                if (myContext.linearLayoutLoadingHome.getVisibility() == View.VISIBLE ) {
                    myContext.linearLayoutLoadingHome.setVisibility(View.GONE);
                }
                if (updateGeocerca && myContext.revieUpdateRegisters && myContext.review2Tipe >= 2) {
                    myContext.revieUpdateRegisters = false;
                    myContext.review2Tipe = 0;
                    myContext.mostrarUpdateChecks();
                } else {
                    loadin(false);
                }
            }
        }
    };

    public MapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_map, container, false);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        authProvider = new AuthProvider();
        checksProvider = new ChecksProvider();
        bitacoraProvider = new BitacoraProvider();
        dbChecks = new DbChecks(myContext);
        dbGeocercas = new DbGeocercas(myContext);
        dbBitacoras = new DbBitacoras(myContext);
        googleApiProvider = new GoogleApiProvider(myContext);
        employeeProvider = new EmployeeProvider();

        firstReviewGeoface = false;
        //viewRout = true;
        fusedLocation = LocationServices.getFusedLocationProviderClient(mView.getContext());

        floatingActionsMenu = mView.findViewById(R.id.groupButton);
        buttonConnect = mView.findViewById(R.id.btnConectDriver);
        sendStartWork = mView.findViewById(R.id.sendStartWork);
        sendStartEating = mView.findViewById(R.id.sendStartEating);
        sendFinishEating = mView.findViewById(R.id.sendFinishEating);
        sendFinishWork = mView.findViewById(R.id.sendFinishWork);
        circleImageViewMap = mView.findViewById(R.id.circleImageMap);
        imageViewPhotoMap = mView.findViewById(R.id.mapSelectImage);
        textViewGoodTime = mView.findViewById(R.id.textViewGoodTime);
        textViewTime = mView.findViewById(R.id.textViewTime);
        textViewName = mView.findViewById(R.id.textViewName);
        textViewState = mView.findViewById(R.id.textViewStatus);
        frameLayoutLoading = mView.findViewById(R.id.loading);
        frameLayoutMoveLocation = mView.findViewById(R.id.frameLayoutMoveLocation);
        frameLayoutGoToGoogleMaps = mView.findViewById(R.id.frameLayoutGoToGoogleMaps);
        imageViewMoveLocation = mView.findViewById(R.id.imageViewMoveLocation);
        viewMoveLocation = mView.findViewById(R.id.viewMoveLocation);
        frameLayoutViewRout = mView.findViewById(R.id.frameLayoutViewRout);
        imageViewRout = mView.findViewById(R.id.imageViewRout);
        viewViewRout = mView.findViewById(R.id.viewRout);
        clockView = mView.findViewById(R.id.clockView);
        frameLayoutTakePhoto = mView.findViewById(R.id.frameLayoutTakePhoto);

        geofencingClient = LocationServices.getGeofencingClient(myContext);
        dbEmployees = new DbEmployees(myContext);

        if (employee == null) {
            employee = dbEmployees.getEmployee(authProvider.getId());
        }
        textViewState.setText("!Conectate para enviar¡");
        sdf1 = new SimpleDateFormat("HH:mm");

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnect) {
                    disconnect();
                } else {
                    if (map == null) {
                        Toast.makeText(myContext, "No se puede conectar", Toast.LENGTH_SHORT).show();
                    } else if (!secondsIsOver) {
                        startLocation();
                    } else {
                        /*AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
                        builder.setMessage("Por favor activa la fecha y hora proporcionadas por la red")
                                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        startActivityForResult(new Intent(Settings.ACTION_DATE_SETTINGS), 0);
                                    }
                                }).create().show();*/
                    }
                }
            }
        });

        sendStartWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLatLng != null && isConnect) {
                    if (takePhoto) {
                        if (!myContext.imagetoBase64.equals("")) {
                            seendCheck("startWork");
                        } else {
                            Toast.makeText(myContext, "La fotografia es necesaria para el registro", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        seendCheck("startWork");
                    }
                } else {
                    Toast.makeText(myContext, "Conectate para enviar el registro", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sendStartEating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLatLng != null && isConnect) {
                    if (takePhoto) {
                        if (!myContext.imagetoBase64.equals("")) {
                            seendCheck("startEating");
                        } else {
                            Toast.makeText(myContext, "La fotografia es necesaria para el registro", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        seendCheck("startEating");
                    }
                } else {
                    Toast.makeText(myContext, "Conectate para enviar el registro", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sendFinishEating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLatLng != null && isConnect) {
                    if (takePhoto) {
                        if (!myContext.imagetoBase64.equals("")) {
                            seendCheck("finishEating");
                        } else {
                            Toast.makeText(myContext, "La fotografia es necesaria para el registro", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        seendCheck("finishEating");
                    }
                } else {
                    Toast.makeText(myContext, "Conectate para enviar el registro", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sendFinishWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLatLng != null && isConnect) {
                    if (takePhoto) {
                        if (!myContext.imagetoBase64.equals("") || !myContext.image90.equals("")) {
                            seendCheck("finishWork");
                        } else {
                            Toast.makeText(myContext, "La fotografia es necesaria para el registro", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        seendCheck("finishWork");
                    }
                } else {
                    Toast.makeText(myContext, "Conectate para enviar el registro", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imageViewPhotoMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });

        circleImageViewMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });


        frameLayoutGoToGoogleMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myContext.geoRadio == 0) {
                    Toast.makeText(myContext, "No se puede realizar la busqueda en mapa", Toast.LENGTH_SHORT).show();
                }else{
                    String uri = "geo:<" + myContext.geoLat + ">,<" + myContext.geoLong + ">?q=<" + myContext.geoLat + ">,<" + myContext.geoLong + ">(" + dbGeocercas.getGeocerca(myContext.idGeocerca).getGeoNombre() + ")";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);
                }
            }
        });

        frameLayoutViewRout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isConnect){
                    reviewRoutLocation();
                } else {
                    Toast.makeText(myContext, "Conectate para obtener la ruta", Toast.LENGTH_SHORT).show();
                }
            }
        });

        frameLayoutMoveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               selectMoveLocation();
            }
        });

        //runnable.run();
        myContext.review2Tipe = 0;
        if(myContext.updateData == false)
            myContext.checkUpdateSend();
        return mView;
    }

    private void selectMoveLocation(){
        if(moveLocation){
            moveLocation = false;
            imageViewMoveLocation.setImageResource(R.drawable.icon_dont_move);
            viewMoveLocation.setBackground(ContextCompat.getDrawable(myContext, R.drawable.circular_view_blue));
            map.getUiSettings().setScrollGesturesEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setAllGesturesEnabled(false);
            if(mLocation2!=null && !viewRout){
                map.animateCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .target(new LatLng(mLocation2.getLatitude(), mLocation2.getLongitude()))
                                .zoom(17f)
                                .build()
                ));
            }
        } else {
            moveLocation = true;
            imageViewMoveLocation.setImageResource(R.drawable.icon_move_location);
            viewMoveLocation.setBackground(ContextCompat.getDrawable(myContext, R.drawable.circular_view));
            map.getUiSettings().setScrollGesturesEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setAllGesturesEnabled(true);
        }
    }

    private void drawRoute(LatLng originLatLng, LatLng destinationLatLng){
        googleApiProvider.getDirections(originLatLng,destinationLatLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try{
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polyLines = route.getJSONObject("overview_polyline");
                    String points = polyLines.getString("points");
                    polyLineList = DecodePoints.decodePoly(points);
                    polylineOptions = new PolylineOptions();
                    polylineOptions.color(Color.DKGRAY);
                    polylineOptions.width(13f);
                    polylineOptions.startCap(new SquareCap());
                    polylineOptions.jointType(JointType.ROUND);
                    polylineOptions.addAll(polyLineList);
                    routPolyLine = map.addPolyline(polylineOptions);

                } catch (Exception e){
                    Toast.makeText(myContext, "Error encontrado: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void reviewRoutLocation( ){
        if(!viewRout){
            //fusedLocation.removeLocationUpdates(locationCallback);
            if(Utils.isOnlineNet(myContext)){
                if (checkIfLocationOpened()) {
                    if (ContextCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocation.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if(task.isSuccessful()){
                                    float distance;
                                    if(updateGeocerca && myContext.geoRadio != 0){
                                        distance = reviewDistance(task.getResult());
                                    } else {
                                        distance=0;
                                    }
                                    if(distance>0){
                                        viewRout = true;
                                        imageViewRout.setImageResource(R.drawable.ic_route_white);
                                        viewViewRout.setBackground(ContextCompat.getDrawable(myContext, R.drawable.circular_view));
                                        LatLng originLatLng = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                                        LatLng destinationLatLng = new LatLng(myContext.geoLat, myContext.geoLong);
                                        //map.addMarker(new MarkerOptions().position(originLatLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_marker_red)));
                                        markerDestination = map.addMarker(new MarkerOptions().position(destinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_marker_blue)));

                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                        builder.include(originLatLng);
                                        builder.include(markerDestination.getPosition());
                                        LatLngBounds bounds = builder.build();
                                        int width = getResources().getDisplayMetrics().widthPixels;
                                        int height = getResources().getDisplayMetrics().heightPixels;
                                        int padding = (int) (width * 0.20); // offset from edges of the map 10% of screen

                                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

                                        map.animateCamera(cu);
                                       /* map.animateCamera(CameraUpdateFactory.newCameraPosition(
                                                new CameraPosition.Builder()
                                                        .target(originLatLng)
                                                        .zoom(14f)
                                                        .build()
                                        ));*/
                                        drawRoute(originLatLng, destinationLatLng);
                                    } else {
                                        Toast.makeText(myContext, "Ya te encuentras en la geocerca asignada", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                    }
                }
            } else {
                Toast.makeText(myContext, "No cuentas con internet para poder trazar la ruta", Toast.LENGTH_SHORT).show();
            }
        } else {
            imageViewRout.setImageResource(R.drawable.ic_move_location);
            viewViewRout.setBackground(ContextCompat.getDrawable(myContext, R.drawable.circular_view_blue));
            viewRout = false;
            if(mLocation2!=null){
                map.animateCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .target(new LatLng(mLocation2.getLatitude(), mLocation2.getLongitude()))
                                .zoom(17f)
                                .build()
                ));
            }
            if(markerDestination != null){
                markerDestination.remove();
            }
            if(routPolyLine != null){
                routPolyLine.remove();
            }
        }
    }

    private float reviewDistance(Location location){
        Location locationA = new Location("centerGeoface");
        locationA.setLatitude(latLngGeoFence.latitude);
        locationA.setLongitude(latLngGeoFence.longitude);

        Location locationB = new Location("miLocation");
        locationB.setLatitude(location.getLatitude());
        locationB.setLongitude(location.getLongitude());

        float distance = locationA.distanceTo(locationB)-myContext.geoRadio;
        return distance;
    }

    private void createGeofencig() {

        if(map!=null){
        latLngGeoFence = new LatLng(myContext.geoLat, myContext.geoLong);
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLngGeoFence);
        circleOptions.radius(myContext.geoRadio);
        circleOptions.strokeColor(R.color.colorGris);
        circleOptions.fillColor(R.color.colorHomeTw2);
        circleOptions.strokeWidth(4);
        frameLayoutGoToGoogleMaps.setVisibility(View.VISIBLE);
        //if(isConnect)
        //frameLayoutViewRout.setVisibility(View.VISIBLE);

            if(mapCircle!=null){
                mapCircle.remove();
                mapCircle = map.addCircle(circleOptions);
            } else {
                mapCircle = map.addCircle(circleOptions);
            }
        } else {
            updateGeocerca = false;
        }
    }

    private void reviewTakePhoto() {
        //SharedPreferences prefe = myContext.getSharedPreferences("datos", Context.MODE_PRIVATE);
        takePhoto = employee.isStateCamera();
        if (takePhoto) {
            imageViewPhotoMap.setVisibility(View.VISIBLE);
            circleImageViewMap.setClickable(true);
            frameLayoutTakePhoto.setVisibility(View.VISIBLE);
            clockView.setVisibility(View.GONE);
            setImageDefault();
        } else {
            imageViewPhotoMap.setVisibility(View.GONE);
            circleImageViewMap.setClickable(false);
            circleImageViewMap.setImageResource(R.drawable.ic_time_orange);
            frameLayoutTakePhoto.setVisibility(View.GONE);
            clockView.setVisibility(View.VISIBLE);
        }
    }

    public void loadin(boolean b) {
        if(frameLayoutLoading != null){
            if (b) {
                frameLayoutLoading.setVisibility(View.VISIBLE);
                buttonConnect.setEnabled(false);
                imageViewPhotoMap.setEnabled(false);
                floatingActionsMenu.setEnabled(false);
            } else {
                if (frameLayoutLoading.getVisibility() == View.VISIBLE) {
                    frameLayoutLoading.setVisibility(View.GONE);
                    buttonConnect.setEnabled(true);
                    imageViewPhotoMap.setEnabled(true);
                    floatingActionsMenu.setEnabled(true);
                }
            }
        }
    }


    private void updateInfo(Date Date) {
        if (Date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH");
            String dateTime = sdf.format(Date.getTime());
            /*SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
            String date1 = sdf1.format(Date.getTime());*/
            String texto = "";
            //mDateL = Date.getTime();

            int hour = Integer.parseInt(dateTime);
            if (hour >= 6 && hour < 12) {
                texto = "Buenos días";
            }
            if (hour >= 12 && hour < 19) {
                texto = "Buenas tardes";
            }
            if (hour >= 19 || hour < 6) {
                texto = "Buenas noches";
            }
            if (textViewGoodTime != null) {
                //textViewTime.setText(date1);
                textViewGoodTime.setText(texto);
            }
        }
    }

    private boolean checkIfLocationOpened() {
        LocationManager lm = (LocationManager) myContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (gps_enabled && network_enabled) {
            return true;
        } else {
            loadin(false);
            new AlertDialog.Builder(myContext)
                    .setMessage("Activa tu ubicacion para continuar")
                    .setPositiveButton("Ajustes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            myContext.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        }
        return false;
    }

    private void takePhoto() {
        ((HomeTW) getActivity()).checkPermissionStorage();
    }


    public void setImageDefault() {
        if (employee.isStateCamera()) {
            if(myContext.imagenBitmap == null)
                circleImageViewMap.setImageResource(R.drawable.icon_image);
        }
    }

    private void reviewBiometrics(){
        executor = ContextCompat.getMainExecutor(myContext);
        biometricPrompt = new BiometricPrompt(myContext, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                myContext.constraintLayoutProgress.setVisibility(View.GONE);
                Toast.makeText(myContext, "Error al validar biometria!!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                messageSend(mTipe,mLocation);
                //Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                myContext.constraintLayoutProgress.setVisibility(View.GONE);
                Toast.makeText(myContext, "No es posible validar", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Enviar registro")
                .setSubtitle("Verifique su identidad para continuar")
                //.setNegativeButtonText("Usar contraseña")
                .setConfirmationRequired(true)
                .setAllowedAuthenticators(BIOMETRIC_STRONG|BIOMETRIC_WEAK|DEVICE_CREDENTIAL)
                .build();

        BiometricManager biometricManager = BiometricManager.from(myContext);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG |BIOMETRIC_WEAK| DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                //Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                canUseBiometrics = 0;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                //Log.e("MY_APP_TAG", "No biometric features available on this device.");
                canUseBiometrics = 1;
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                //Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                canUseBiometrics = 1;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                canUseBiometrics = 2;
                break;
        }
    }

    private void seendCheck(String tipe) {
        if (checkIfLocationOpened()) {
            if (ContextCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocation.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        float distance;
                        if(updateGeocerca && myContext.geoRadio != 0){
                           distance = reviewDistance(location);
                        } else {
                            distance=0;
                        }
                        if(distance <= 0){
                            if(biometria){
                                if(canUseBiometrics == 0){
                                    mLocation = location;
                                    mTipe = tipe;
                                    biometricPrompt.authenticate(promptInfo);
                                } else {
                                    myContext.constraintLayoutProgress.setVisibility(View.GONE);
                                    Toast.makeText(myContext, "No se puede usar biometria", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                messageSend(tipe, location);
                            }
                        } else {
                            //loadin(false);

                            myContext.constraintLayoutProgress.setVisibility(View.GONE);
                            Toast.makeText(myContext, "Te encuentras a "+getNumByDecimal(distance)+" m de distancia de la geocerca", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else {
                myContext.constraintLayoutProgress.setVisibility(View.GONE);
                //loadin(false);
            }
        } else {
            myContext.constraintLayoutProgress.setVisibility(View.GONE);
            //loadin(false);
            disconnect();
        }
    }

    private void messageSend(String tipe, Location location){
        myContext.constraintLayoutProgress.setVisibility(View.VISIBLE);
        time1 = Utils.getTime();
        Check check = new Check();
        check.setTipeCheck(tipe);
        check.setIdUser(authProvider.getId());
        check.setTime(time1.getTime());
        check.setCheckLat(location.getLatitude());
        check.setCheckLong(location.getLongitude());
        check.setStatusSend(0);

        setImageMessage(check);

        if(myContext.idGeocerca != null && !myContext.idGeocerca.equals("")){
            check.setIdGeocerca(myContext.idGeocerca);
            String geoName = dbGeocercas.getGeocerca(myContext.idGeocerca).getGeoNombre();
            if(geoName != null && !geoName.equals("")){
                check.setNameGeocerca(geoName);
            }
        }

        myContext.numberChecksSendLate++;
        seendMessageToFirebase(check);

        long id = dbChecks.insertCheck(check);
        myContext.image = null;
        if (id > 0) {
            myContext.updateViewLateCheck();
            myContext.imagenBitmap = null;
            setImageDefault();
            if (!Utils.isOnlineNet(myContext)) {
                //loadin(false);
                withoutInternet = false;
                myContext.constraintLayoutProgress.setVisibility(View.GONE);
                enviarToast(false, "");
            }
        } else {
            myContext.constraintLayoutProgress.setVisibility(View.GONE);
            Toast.makeText(myContext, "Error al registrar", Toast.LENGTH_SHORT).show();
        }
    }

    private void setImageMessage(Check check) {
        if (myContext.imagetoBase64 != null) {
            if (myContext.imagetoBase64 != "") {
                check.setImage(myContext.imagetoBase64);
                myContext.imagetoBase64 = "";
            }
        }

        if (myContext.image90 != null) {
            if (myContext.image90 != "") {
                check.setImage90(myContext.image90);
                myContext.image90 = "";
                myContext.imagenBitmap = null;
            }
        }
    }

    private void seendMessageToFirebase (Check check){
        checksProvider.createCheck(check).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(myContext.constraintLayoutProgress.getVisibility() == View.GONE){
                    myContext.constraintLayoutProgress.setVisibility(View.VISIBLE);
                }
                int tipeSend;
                Date time2 = Utils.getTime();

                if (time1 != null) {
                    if (withoutInternet == true) {
                        tipeSend = 1;
                    } else {
                        tipeSend = 2;
                    }
                } else {
                    tipeSend = 2;
                }

                time1 = null;
                check.setStatusSend(tipeSend);

                checksProvider.updateStatus(check.getIdCheck(), tipeSend, time2.getTime());
                myContext.updateChecks(check.getIdCheck(), tipeSend, check.getTime(), time2.getTime());

                //loadin(false);

                myContext.constraintLayoutProgress.setVisibility(View.GONE);
                if (myContext.numberChecksSendLate != 0 ) {
                    if (!myContext.updateChecksNotSend){
                        enviarToast(true, check.getIdCheck());
                        myContext.numberChecksSendLate = 0;
                        withoutInternet = true;
                        myContext.updateChecksNotSend = false;
                    } else {
                        myContext.numberChecksSendLate = 0;
                        withoutInternet = true;
                    }
                }
            }
        });
    }

    private String getNumByDecimal(float valor){
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(1); //Define decimales.
        return format.format(valor);
    }


    private void enviarToast(boolean b, String idCheck) {
        CharSequence text;
        int duration = Toast.LENGTH_SHORT;
        if (b) {
            if (myContext.numberChecksSendLate == 1) {
                text = "Registro enviado";
                //myContext.sendNotification(text+"");
            } else {
                text = "Se enviaron " + myContext.numberChecksSendLate + " registros";
                //myContext.sendNotification(text+"");
            }
        } else {
            text = "Registro pendiente, conectate a internet para enviar!!";
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (mToast == null || !mToast.getView().isShown()) {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(myContext, text, duration);
                mToast.show();
                if(b)
                    myContext.askNotificationPermission(text+"", idCheck);
                //myContext.sendNotification(text+"", idCheck);
            }
        } else {
            if (mToast != null) mToast.cancel();
            mToast = Toast.makeText(myContext, text, duration);
            mToast.show();
            if(b)
                myContext.askNotificationPermission(text+"", idCheck);
            //myContext.sendNotification(text+"", idCheck);
        }
    }


    public static boolean evaluarLimite(Date date1, Date date2) {
        boolean correcto = false;
        long diferencia = (Math.abs(date1.getTime() - date2.getTime())) / 1000;
        long limit = (5 * 1000) / 1000L;//limite de tiempo

        if (diferencia <= limit) {
            correcto = true;
        }
        return correcto;
    }

    private void disconnect() {
        if (fusedLocation != null && marker != null && locationCallback != null) {
            buttonConnect.setText("Conectarse");
            textViewState.setText("!Conectate para enviar¡");
            isConnect = false;
            fusedLocation.removeLocationUpdates(locationCallback);
            marker.remove();
        } else {
            // Toast.makeText(myContext, "No se puede desconectar", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    //if(!isOnlineNet()){
                    if (myContext.linearLayoutLoadingHome.getVisibility() == View.GONE) {
                        loadin(true);
                    }
                    if(frameLayoutMoveLocation.getVisibility() == View.GONE){
                        frameLayoutMoveLocation.setVisibility(View.VISIBLE);
                    }

                    buttonConnect.setText("Desconectarse");
                    textViewState.setText("¡Listo para enviar registros!");
                    isConnect = true;
                    fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    map.setMyLocationEnabled(false);
                    // }
                } else {
                    showAlertDialogNOGPS();
                }
            } else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                if (myContext.linearLayoutLoadingHome.getVisibility() == View.GONE) {
                    loadin(true);
                }
                if(frameLayoutMoveLocation.getVisibility() == View.GONE){
                    frameLayoutMoveLocation.setVisibility(View.VISIBLE);
                }

                buttonConnect.setText("Desconectarse");
                textViewState.setText("¡Listo para enviar registros!");
                isConnect = true;
                fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                map.setMyLocationEnabled(false);
            } else {
                showAlertDialogNOGPS();
            }
        }
    }

    public void startLocation2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    //if(!isOnlineNet()){
                    if(frameLayoutMoveLocation.getVisibility() == View.GONE){
                        frameLayoutMoveLocation.setVisibility(View.VISIBLE);
                    }
                    buttonConnect.setText("Desconectarse");
                    textViewState.setText("¡Listo para enviar registros!");
                    isConnect = true;
                    fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    map.setMyLocationEnabled(false);
                    // }
                } else {
                    showAlertDialogNOGPS();
                }
            } else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                if(frameLayoutMoveLocation.getVisibility() == View.GONE){
                    frameLayoutMoveLocation.setVisibility(View.VISIBLE);
                }
                fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                map.setMyLocationEnabled(false);
            } else {
                showAlertDialogNOGPS();
            }
        }
    }

    public boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) myContext.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    public void showAlertDialogNOGPS() {
        //myContext.pdRevieData.dismiss();
        if (myContext.linearLayoutLoadingHome.getVisibility() == View.VISIBLE ) {
            myContext.linearLayoutLoadingHome.setVisibility(View.GONE);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();


    }

    public void checkLocationPermissions() {
        //myContext.pdRevieData.dismiss();
        if (myContext.linearLayoutLoadingHome.getVisibility() == View.VISIBLE) {
            myContext.linearLayoutLoadingHome.setVisibility(View.GONE);
        }
        if (ContextCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(myContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(myContext)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Requiere de los permisos de ubicacion para poder usarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(myContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(myContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        myContext = (HomeTW) context;
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        employee = dbEmployees.getEmployee(authProvider.getId());
        updateGeocerca = false;
        //if(myContext.geoRadio == 0){
        reviewGeocerca();
        if(employee != null){
            setGreeting();
            reviewTakePhoto();
            biometria = employee.isStateBiometrics();
            if(biometria){
                reviewBiometrics();
            }
        }
        runnable.run();
        //checkTime();
        super.onResume();
    }


    private void reviewGeocerca() {
        SharedPreferences sharedPref = myContext.getSharedPreferences("geocerca", Context.MODE_PRIVATE);
        myContext.geoLat = sharedPref.getFloat("geoLat",0);
        myContext.geoLong = sharedPref.getFloat("geoLong",0);
        myContext.geoRadio = sharedPref.getFloat("geoRadio",0);
        myContext.idGeocerca = sharedPref.getString("idGeocerca","");
        if(myContext.geoRadio == 0){
            if(dbBitacoras.getBitacorasByIdUser(authProvider.getId()).size() == 0){
                myContext.removeGeocerca();
                updateGeocerca = true;
            }
        } else {
            if(dbBitacoras.getBitacorasByIdUser(authProvider.getId()).size() == 0){
                myContext.removeGeocerca();
            } else {
                updateGeocerca = true;
                createGeofencig();
            }
        }
    }

    public void setGreeting(){
        textViewName.setText(employee.getName());
        calendar = Calendar.getInstance(TimeZone.getTimeZone(TimeZone.getDefault().getID()), Locale.getDefault());
        Date date = calendar.getTime();
        updateInfo(date);
    }

    public void removeSecondProces(boolean remove) {
        if (remove) {
            if(isConnect){
                if (locationCallback != null && fusedLocation != null) {
                    fusedLocation.removeLocationUpdates(locationCallback);
                }
            }
            handler.removeCallbacks(runnable);
        } else {
            if(isConnect){
                startLocation2();
            }
            runnable.run();
        }
    }

    @Override
    public void onDestroy() {
        removeSecondProces(true);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setScrollGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setIndoorLevelPickerEnabled(false);

        /*locationRequest = new LocationRequest.Builder(1000);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(2);*/

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(500)
                .setMaxUpdateDelayMillis(100)
                .build();

        generateToken();

        if(dbBitacoras.getBitacorasByIdUser(authProvider.getId()).size() == 0){
            startLocation();
        } else if(myContext.geoRadio != 0){
            startLocation();
        }
    }


    private void generateToken(){
        if(employeeProvider != null){
            employee = dbEmployees.getEmployee(authProvider.getId());
            if(employee!=null){
                employeeProvider.updateToken(employee.getIdUser(), myContext);
                employee = dbEmployees.getEmployee(authProvider.getId());
            }
        }
    }


}