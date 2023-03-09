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
import android.content.res.Resources;
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

import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dan.timewebclone.R;
import com.dan.timewebclone.activitys.GeocercasActivity;
import com.dan.timewebclone.activitys.HomeTW;
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
import com.dan.timewebclone.utils.Utils;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.QuerySnapshot;
import com.huawei.hms.location.LocationAvailability;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.SettingsClient;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.MapsInitializer;
import com.huawei.hms.maps.model.Circle;
import com.huawei.hms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executor;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapHuaweiFragment extends Fragment implements com.huawei.hms.maps.OnMapReadyCallback {

    private AuthProvider authProvider;
    private ChecksProvider checksProvider;
    private BitacoraProvider bitacoraProvider;
    private EmployeeProvider employeeProvider;
    private DbChecks dbChecks;
    private DbGeocercas dbGeocercas;
    private DbBitacoras dbBitacoras;
    public Circle mapCircle;


    private Context context;
    private Resources resources;

    public FloatingActionsMenu floatingActionsMenu;
    private FloatingActionButton sendStartWork, sendStartEating, sendFinishEating, sendFinishWork;

    private View mView, viewMoveLocation, viewViewRout;
    private TextView textViewGoodTime, textViewTime, textViewName, textViewState;
    public FrameLayout frameLayoutLoading, frameLayoutMoveLocation, frameLayoutGoToPetalMaps, frameLayoutViewRout;
    private Toast mToast = null;
    int numberChecksSendLate = 0;

    public final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    //private Marker marker;
    private Button buttonConnect;
    private boolean isConnect = false;
    private boolean withoutInternet = true;
    public  CircleImageView circleImageViewMap;
    private ImageView imageViewPhotoMap, imageViewMoveLocation, imageViewRout;

    private LatLng currentLatLng;
    private HomeTW myContext;
    //private Long date;
    private Date mDateD;
    public ProgressDialog pdSendCheck;
    public Date time1;
    private String zone = "America/Mexico_City";

    private com.huawei.hms.location.LocationRequest mLocationRequest;
    private com.huawei.hms.location.FusedLocationProviderClient fusedLocationProviderClient;
    private SettingsClient settingsClient;
    private HuaweiMap hmap;
    private com.huawei.hms.maps.model.Marker marker;
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String APY_KEY = "DAEDAHiidNYFRwwGIFtnRv1diOv0FG60k+seNMFCsNtRjh3gTAJ7ZBlMWo6vFvAnPz8bt9jytDQqUkkCiqu6eWw8dFVPWsd3EKh1wQ==";


    private DbEmployees dbEmployees;
    public Employee employee;
    private Calendar calendar;
    private String timezoneID;
    private SimpleDateFormat sdf1;

    private boolean updateGeocerca = false;
    public boolean firstReviewGeoface;
    private Geofence geofencing;
    private GeofencingClient geofencingClient;
    private com.huawei.hms.maps.model.LatLng latLngGeoFence;
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
                    setGreeting();
                } else {
                    disconnect();
                    secondsIsOver = true;
                    if (textViewTime != null) {
                        textViewTime.setText("");
                        //textViewGoodTime.setText("");
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


    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null) {
                List<Location> locations = locationResult.getLocations();
                if (!locations.isEmpty()) {
                    for (Location location : locations) {
                        mLocation2 = location;
                        if (Utils.isMockLocationOn(location, myContext)) {
                            disconnect();
                            Toast.makeText(myContext, "Se ha detectado ubicacion de prueba, por lo que no se puede enviar registros", Toast.LENGTH_LONG).show();
                        } else {
                            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            if (marker != null) {
                                marker.remove();
                            }

                            // MapImage mapImage = MapImageFactory.fromResource(getResources(), R.drawable.here_car);
                            marker = hmap.addMarker(new MarkerOptions().position(new com.huawei.hms.maps.model.LatLng(location.getLatitude(), location.getLongitude()))
                                    .title("Tu posicion")
                                    .icon(Utils.bitmapDescriptorFromVector(myContext,R.drawable.icon_employee_48)).anchorMarker(0.5f,1f));

                            if(!moveLocation && !viewRout){
                                CameraUpdate cameraPosition = CameraUpdateFactory.newLatLngZoom(new com.huawei.hms.maps.model.LatLng(location.getLatitude(), location.getLongitude()), 18f);
                                hmap.animateCamera(cameraPosition);
                            }
                        }
                        if (myContext.geoRadio != 0 && updateGeocerca == false) {
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
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            if (locationAvailability != null) {
                boolean flag = locationAvailability.isLocationAvailable();
            }
        }
    };

    public MapHuaweiFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        myContext = (HomeTW) context;
        super.onAttach(context);
    }

    public static MapHuaweiFragment newInstance(String param1, String param2) {
        MapHuaweiFragment fragment = new MapHuaweiFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MapsInitializer.initialize(myContext);
        MapsInitializer.setApiKey(APY_KEY);
        mView = inflater.inflate(R.layout.fragment_map_huawei, container, false);
        authProvider = new AuthProvider();
        checksProvider = new ChecksProvider();
        bitacoraProvider = new BitacoraProvider();
        employeeProvider = new EmployeeProvider();
        dbChecks = new DbChecks(myContext);
        dbGeocercas = new DbGeocercas(myContext);
        dbBitacoras = new DbBitacoras(myContext);
        dbEmployees = new DbEmployees(myContext);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        firstReviewGeoface = false;

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
        frameLayoutGoToPetalMaps = mView.findViewById(R.id.frameLayoutGoToPetalMaps);
        imageViewMoveLocation = mView.findViewById(R.id.imageViewMoveLocation);
        viewMoveLocation = mView.findViewById(R.id.viewMoveLocation);
        frameLayoutViewRout = mView.findViewById(R.id.frameLayoutViewRout);
        imageViewRout = mView.findViewById(R.id.imageViewRout);
        viewViewRout = mView.findViewById(R.id.viewRout);

        if (employee == null) {
            employee = dbEmployees.getEmployee(authProvider.getId());
        }

        textViewState.setText("!Conectate para enviar¡");
        sdf1 = new SimpleDateFormat("HH:mm:ss");

        //checkPermisos();

        mMapView = mView.findViewById(R.id.mapview_mapviewdemo);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle("MapViewBundleKey");
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnect) {
                    disconnect();
                } else {
                    if (hmap == null) {
                        Toast.makeText(myContext, "No se puede conectar", Toast.LENGTH_SHORT).show();
                    } else if (!secondsIsOver) {
                        startLocation();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
                        builder.setMessage("Por favor activa la fecha y hora proporcionadas por la red")
                                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        startActivityForResult(new Intent(Settings.ACTION_DATE_SETTINGS), 0);
                                    }
                                }).create().show();
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


        frameLayoutGoToPetalMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myContext.geoRadio == 0) {
                    Toast.makeText(myContext, "No se puede realizar la busqueda en mapa", Toast.LENGTH_SHORT).show();
                }else{
                    String uriString = "petalmaps://poidetail?center="+myContext.geoLat+","+myContext.geoLong+"&marker="+myContext.geoLat+","+myContext.geoLong+"&utm_source="+dbGeocercas.getGeocerca(myContext.idGeocerca).getGeoNombre();
                    //String uriString = "geo:<" + myContext.geoLat + ">,<" + myContext.geoLong + ">?q=<" + myContext.geoLat + ">,<" + myContext.geoLong + ">(" + dbGeocercas.getGeocerca(myContext.idGeocerca).getGeoNombre() + ")";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
                    startActivity(intent);
                }
            }
        });

        frameLayoutViewRout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isConnect){
                    //reviewRoutLocation();
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

        runnable.run();
        return mView;
    }

    private void selectMoveLocation(){
        if(moveLocation){
            moveLocation = false;
            imageViewMoveLocation.setImageResource(R.drawable.icon_dont_move);
            viewMoveLocation.setBackground(ContextCompat.getDrawable(myContext, R.drawable.circular_view_blue));
            hmap.getUiSettings().setScrollGesturesEnabled(false);
            hmap.getUiSettings().setZoomControlsEnabled(false);
            hmap.getUiSettings().setAllGesturesEnabled(false);
            if(mLocation2!=null && !viewRout){
                CameraUpdate cameraPosition = CameraUpdateFactory.newLatLngZoom(new com.huawei.hms.maps.model.LatLng(mLocation2.getLatitude(), mLocation2.getLongitude()), 18f);
                hmap.animateCamera(cameraPosition);
            }
        } else {
            moveLocation = true;
            imageViewMoveLocation.setImageResource(R.drawable.icon_move_location);
            viewMoveLocation.setBackground(ContextCompat.getDrawable(myContext, R.drawable.circular_view));
            hmap.getUiSettings().setScrollGesturesEnabled(true);
            hmap.getUiSettings().setZoomControlsEnabled(true);
            hmap.getUiSettings().setAllGesturesEnabled(true);
        }
    }

    private void takePhoto() {
        ((HomeTW) getActivity()).checkPermissionStorage();
    }

    public void setGreeting(){
        textViewName.setText(employee.getName());
        timezoneID = TimeZone.getDefault().getID();
        calendar = Calendar.getInstance(TimeZone.getTimeZone(timezoneID), Locale.getDefault());
        Date date = calendar.getTime();
        updateInfo(date);
    }

    private void updateInfo(Date Date) {
        if (Date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH");
            String dateTime = sdf.format(Date.getTime());
            String texto = "";
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
                textViewGoodTime.setText(texto);
            }
        }
    }

    public void setImageDefault() {
        if (employee.isStateCamera()) {
            if(myContext.imagenBitmap == null)
                circleImageViewMap.setImageResource(R.drawable.icon_image);
        }
    }

    private void reviewTakePhoto() {
        //SharedPreferences prefe = myContext.getSharedPreferences("datos", Context.MODE_PRIVATE);
        takePhoto = employee.isStateCamera();
        if (takePhoto) {
            imageViewPhotoMap.setVisibility(View.VISIBLE);
            circleImageViewMap.setClickable(true);
            setImageDefault();
        } else {
            imageViewPhotoMap.setVisibility(View.GONE);
            circleImageViewMap.setClickable(false);
            circleImageViewMap.setImageResource(R.drawable.ic_time_orange);
        }
    }

    private void disconnect() {
        if (fusedLocationProviderClient != null && marker != null && mLocationCallback != null) {
            buttonConnect.setText("Conectarse");
            textViewState.setText("!Conectate para enviar¡");
            isConnect = false;
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
            marker.remove();
        } else {
            Toast.makeText(myContext, "No se puede desconectar", Toast.LENGTH_SHORT).show();
        }
    }

    private void seendCheck(String tipe) {
        if (checkIfLocationOpened()) {
            if (ContextCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new com.huawei.hmf.tasks.OnSuccessListener<Location>() {
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
                            String textdistance ="Te encuentras a " + getNumByDecimal(distance) +" m de distancia de la geocerca" ;
                            Toast.makeText(myContext, textdistance+"", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
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
            }
        }

        if(myContext.idGeocerca != null && !myContext.idGeocerca.equals("")){
            check.setIdGeocerca(myContext.idGeocerca);
            String geoName = dbGeocercas.getGeocerca(myContext.idGeocerca).getGeoNombre();
            if(geoName != null && !geoName.equals("")){
                check.setNameGeocerca(geoName);
            }
        }

        myContext.numberChecksSendLate++;

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
                        enviarToast(true);
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
                enviarToast(false);
            }
        } else {
            myContext.constraintLayoutProgress.setVisibility(View.GONE);
            //loadin(false);
            Toast.makeText(myContext, "Error al registrar", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarToast(boolean b) {
        CharSequence text;
        int duration = Toast.LENGTH_SHORT;
        if (b) {
            if (myContext.numberChecksSendLate == 1) {
                text = "Registro enviado";
                //myContext.sendNotification(text+"", "");
            } else {
                text = "Se enviaron " + myContext.numberChecksSendLate + " registros";
                //myContext.sendNotification(text+"", "");
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
                    myContext.askNotificationPermission(text+"", "");
                //myContext.sendNotification(text+"", "");
            }
        } else {
            if (mToast != null) mToast.cancel();
            mToast = Toast.makeText(myContext, text, duration);
            mToast.show();
            if(b)
                myContext.askNotificationPermission(text+"", "");
            //myContext.sendNotification(text+"", "");
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

    private String getNumByDecimal(float valor){
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(1); //Define decimales.
        return format.format(valor);
    }

    private void checkPermisos() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ActivityCompat.checkSelfPermission(myContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(myContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] strings = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
                ActivityCompat.requestPermissions(myContext, strings, 1);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(myContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(myContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(myContext, "android.permission.ACCESS_BACKGROUND_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                String[] strings = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, "android.permission.ACCESS_BACKGROUND_LOCATION"};
                ActivityCompat.requestPermissions(myContext, strings, 2);
            }
        }
    }


    public void checkLocationPermissions() {
        //myContext.pdRevieData.dismiss();

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

        AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();


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
                    fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    hmap.setMyLocationEnabled(false);

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
                fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                hmap.setMyLocationEnabled(false);
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
                    fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    hmap.setMyLocationEnabled(false);
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
                fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                hmap.setMyLocationEnabled(false);
            } else {
                showAlertDialogNOGPS();
            }
        }
    }

    private void reviewGeocerca() {
        SharedPreferences sharedPref = myContext.getSharedPreferences("geocerca", Context.MODE_PRIVATE);
        myContext.geoLat = sharedPref.getFloat("geoLat",0);
        myContext.geoLong = sharedPref.getFloat("geoLong",0);
        myContext.geoRadio = sharedPref.getFloat("geoRadio",0);
        myContext.idGeocerca = sharedPref.getString("idGeocerca","");
        if(myContext.geoRadio == 0){
            if(dbBitacoras.getBitacorasByIdUser(authProvider.getId()).size() == 0){
                /*bitacoraProvider.getBitacorasByUser(authProvider.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        if(!querySnapshot.isEmpty()){
                            if(querySnapshot.size() != 0){
                                myContext.updateData = true;
                                Intent i = new Intent(myContext, GeocercasActivity.class);
                                i.putExtra("notComeBack", true);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                            } else {
                                updateGeocerca = true;
                            }
                        } else {
                            myContext.removeGeocerca();
                            updateGeocerca = true;
                        }
                    }
                });*/
                myContext.removeGeocerca();
                updateGeocerca = true;
            }
            /*else {
                myContext.updateData = true;
                Intent i = new Intent(myContext, GeocercasActivity.class);
                i.putExtra("notComeBack", true);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }*/
        } else {
            /*if(!firstReviewGeoface && !myContext.getIntent().getBooleanExtra("notRevie",false)){
                firstReviewGeoface = true;
                bitacoraProvider.getBitacorasByUser(authProvider.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        if(!querySnapshot.isEmpty()){
                            if(querySnapshot.size() != 0){
                                myContext.updateData = true;
                                Intent i = new Intent(myContext, GeocercasActivity.class);
                                i.putExtra("notComeBack", true);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                            } else {
                                updateGeocerca = true;
                            }
                        } else {
                            myContext.removeGeocerca();
                            dbBitacoras.deleteAllBitacoras();
                            updateGeocerca = true;
                        }
                    }
                });
            }*/
            if(dbBitacoras.getBitacorasByIdUser(authProvider.getId()).size() == 0){
                myContext.removeGeocerca();
            } else {
                updateGeocerca = true;
                createGeofencig();
            }
            //updateGeocerca = false;
        }
    }

    private void createGeofencig() {
        if(hmap!=null){
            latLngGeoFence = new com.huawei.hms.maps.model.LatLng(myContext.geoLat, myContext.geoLong);
            com.huawei.hms.maps.model.CircleOptions circleOptions = new com.huawei.hms.maps.model.CircleOptions();
            circleOptions.center(latLngGeoFence);
            circleOptions.radius(myContext.geoRadio);
            circleOptions.strokeColor(R.color.colorGris);
            circleOptions.fillColor(R.color.colorHomeTw2);
            circleOptions.strokeWidth(4);
            frameLayoutGoToPetalMaps.setVisibility(View.VISIBLE);
            //if(isConnect)
                //frameLayoutViewRout.setVisibility(View.VISIBLE);

            if(mapCircle!=null){
                mapCircle.remove();
                mapCircle = hmap.addCircle(circleOptions);
            } else {
                mapCircle = hmap.addCircle(circleOptions);
            }
        } else {
            updateGeocerca = false;
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

    @Override
    public void onMapReady(HuaweiMap huaweiMap) {
        hmap = huaweiMap;
        if (hmap != null) {
            hmap.setMapType(HuaweiMap.MAP_TYPE_NORMAL);
            hmap.setMyLocationEnabled(false);
            hmap.getUiSettings().setScrollGesturesEnabled(false);
            hmap.getUiSettings().setZoomControlsEnabled(false);
            hmap.getUiSettings().setAllGesturesEnabled(false);
            hmap.getUiSettings().setIndoorLevelPickerEnabled(false);

            //initLocation();

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(myContext);
            settingsClient = LocationServices.getSettingsClient(myContext);
            mLocationRequest = new com.huawei.hms.location.LocationRequest();
            mLocationRequest.setInterval(1000);
            mLocationRequest.setFastestInterval(500);
            //mLocationRequest.setSmallestDisplacement(2);
            mLocationRequest.setPriority(com.huawei.hms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);

            generateToken();

            if(dbBitacoras.getBitacorasByIdUser(authProvider.getId()).size() == 0){
                startLocation();
            } else if(myContext.geoRadio != 0){
                startLocation();
            }
            //startLocation();
        }
    }

    private void generateToken(){
        if(employee!=null){
            employeeProvider.updateTokenHMS(employee.getIdUser(), myContext);
            employee = dbEmployees.getEmployee(authProvider.getId());
        } else {
            employee = dbEmployees.getEmployee(authProvider.getId());
            if(employee!=null){
                employeeProvider.updateTokenHMS(employee.getIdUser(), myContext);
                employee = dbEmployees.getEmployee(authProvider.getId());
            }
        }
    }

    public void removeSecondProces(boolean remove) {
        if (remove) {
            if(isConnect){
                if (mLocationCallback != null && fusedLocationProviderClient != null) {
                    fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
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





    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }*/

   @Override
    public void onStart() {
        if(myContext.updateData == false)
        myContext.checkUpdateSend();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        //mMapView.onResume();
        context = myContext;
        resources = context.getResources();
        timezoneID = TimeZone.getDefault().getID();
        employee = dbEmployees.getEmployee(authProvider.getId());
        updateGeocerca = false;
        //if(myContext.geoRadio == 0){
        reviewGeocerca();
        if(employee!=null){
            setGreeting();
            reviewTakePhoto();
            biometria = employee.isStateBiometrics();
            if(biometria){
                reviewBiometrics();
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();

        /*if(marker!=null){
            marker.remove();
        }*/
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
        removeSecondProces(true);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}