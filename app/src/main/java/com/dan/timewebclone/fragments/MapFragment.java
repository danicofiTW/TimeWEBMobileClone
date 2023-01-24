package com.dan.timewebclone.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.R;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.ChecksProvider;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private AuthProvider authProvider;
    private ChecksProvider checksProvider;
    private EmployeeProvider employeeProvider;
    private DbChecks dbChecks;

    public FloatingActionsMenu floatingActionsMenu;
    private FloatingActionButton sendStartWork, sendStartEating, sendFinishEating, sendFinishWork;

    private GoogleMap map;
    private View mView;
    private TextView textViewGoodTime, textViewTime, textViewName, textViewState;
    private SupportMapFragment mapFragment;
    public FrameLayout frameLayoutLoading;
    private Toast mToast = null;
    int numberChecksSendLate = 0;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private Marker marker;
    private Button buttonConnect;
    private boolean isConnect = false;
    private boolean withoutInternet = true;
    public static CircleImageView circleImageViewMap;
    private ImageView imageViewPhotoMap;

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
    private String timezoneID;
    private SimpleDateFormat sdf1;

    public boolean secondsIsOver = false;
    public boolean takePhoto = false;
    Handler handler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (employee != null) {
                if (isTimeAutomaticEnabled(myContext)) {
                    secondsIsOver = false;
                    timezoneID = TimeZone.getDefault().getID();
                    calendar = Calendar.getInstance(TimeZone.getTimeZone(timezoneID), Locale.getDefault());
                    mDateD = calendar.getTime();
                    String date1 = sdf1.format(mDateD.getTime());
                    if (textViewTime != null) {
                        textViewTime.setText(date1);
                    }
                    //updateInfo(mDateD);
                    /*if (myContext.time1 != null) {
                        if (!evaluarLimite(mDateD, myContext.time1)) {
                            if (myContext.pdRevieData.isShowing()) {
                                myContext.pdRevieData.cancel();
                                time1 = null;
                            }
                        }
                    }*/
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
                if (employee != null){
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
                //mLocation = location;
                //Log.d("UBICACION", "Location: " + mLocation.getLatitude()+ ", " + mLocation.getLongitude());
                if (isMockLocationOn(location, myContext)) {
                    disconnect();
                    Toast.makeText(myContext, "Se ha detectado ubicacion de prueba, por lo que no se puede enviar registros", Toast.LENGTH_LONG).show();
                } else {
                    currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    //date = location.getTime();

                    if (marker != null) {
                        marker.remove();
                    }
                    //icono del conductor
                    marker = map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("Tu posicion")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_employee_48)));

                    map.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(17f)
                                    .build()
                    ));
                }

                if (myContext.updateData == false) {
                    myContext.updateData = true;
                    myContext.checkUpdateSend();
                } else {
                    loadin(false);
                }
                //updateInfo(date);
                //updateLocation();
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
        employeeProvider = new EmployeeProvider();
        dbChecks = new DbChecks(myContext);

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


        dbEmployees = new DbEmployees(myContext);


        //employee = dbEmployees.getEmployee(authProvider.getId());
        //employee = dbEmployees.getEmployee(authProvider.getId());
        //textViewName.setText(employee.getName());
        textViewState.setText("!Conectate para enviar¡");
        sdf1 = new SimpleDateFormat("HH:mm:ss");

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
                        Toast.makeText(myContext, "No se cuenta con la hora correcta para enviar registros", Toast.LENGTH_SHORT).show();
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
                        if (!myContext.imagetoBase64.equals("")) {
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
        runnable.run();

        return mView;
    }

    private void reviewTakePhoto() {
        //SharedPreferences prefe = myContext.getSharedPreferences("datos", Context.MODE_PRIVATE);
        takePhoto = employee.isStateCamera();
        if (takePhoto) {
            imageViewPhotoMap.setVisibility(View.VISIBLE);
            setImageDefault();
        } else {
            imageViewPhotoMap.setVisibility(View.GONE);
            circleImageViewMap.setImageResource(R.drawable.ic_time_orange);
        }
    }

    public void loadin(boolean b) {
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
            //if (provider.contains("gps") || provider.contains("network")){
            return true;
            //}
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
            circleImageViewMap.setImageResource(R.drawable.icon_image);
        }
    }

    private void seendCheck(String tipe) {
        //loadin(true);
        myContext.constraintLayoutProgress.setVisibility(View.VISIBLE);
        if (checkIfLocationOpened()) {
            if (ContextCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocation.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        String timezoneID = TimeZone.getDefault().getID();
                        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timezoneID), Locale.getDefault());
                        time1 = calendar.getTime();

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
                        if (myContext.fotoUri != null) {
                            check.setUrlImage(myContext.fotoUri.toString());
                            myContext.fotoUri = null;
                        }

                        numberChecksSendLate++;

                        checksProvider.createCheck(check).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if(myContext.constraintLayoutProgress.getVisibility() == View.GONE){
                                    myContext.constraintLayoutProgress.setVisibility(View.VISIBLE);
                                }
                                int tipeSend;
                                String timezoneID = TimeZone.getDefault().getID();
                                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timezoneID), Locale.getDefault());
                                Date time2 = calendar.getTime();

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
                                if (numberChecksSendLate != 0) {
                                    enviarToast(true);
                                    numberChecksSendLate = 0;
                                    withoutInternet = true;
                                }
                            }
                        });
                        long id = dbChecks.insertCheck(check);
                        myContext.image = null;
                        if (id > 0) {
                            myContext.updateViewLateCheck();
                            setImageDefault();

                            if (!isOnlineNet()) {
                                //loadin(false);
                                withoutInternet = false;
                                myContext.constraintLayoutProgress.setVisibility(View.GONE);
                                enviarToast(false);
                            }
                        } else {
                            loadin(false);
                            Toast.makeText(myContext, "Error al registrar", Toast.LENGTH_SHORT).show();
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


    private void enviarToast(boolean b) {
        CharSequence text;
        int duration = Toast.LENGTH_SHORT;
        if (b) {
            if (numberChecksSendLate == 1) {
                text = "Registro enviado";
            } else {
                text = "Se enviaron " + numberChecksSendLate + " registros";
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
            }
        } else {
            if (mToast != null) mToast.cancel();
            mToast = Toast.makeText(myContext, text, duration);
            mToast.show();
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

    private void startLocation2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    //if(!isOnlineNet()){

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
                fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                map.setMyLocationEnabled(false);
            } else {
                showAlertDialogNOGPS();
            }
        }
    }

    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) myContext.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    private void showAlertDialogNOGPS() {
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

    private void checkLocationPermissions() {
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

    public static boolean isMockLocationOn(Location location, Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (location != null) {
                return location.isFromMockProvider();
            } else {
                return false;
            }
        } else {
            String mockLocation = "0";
            try {
                mockLocation = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return !mockLocation.equals("0");
        }
    }

   /*PREGUNTAR DE APP CON SOBREPOSICION
   public static boolean areThereMockPermissionApps(Context context) {
        int count = 0;

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages =
                pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName,
                        PackageManager.GET_PERMISSIONS);

                // Get Permissions
                String[] requestedPermissions = packageInfo.requestedPermissions;

                if (requestedPermissions != null) {
                    for (int i = 0; i < requestedPermissions.length; i++) {
                        if (requestedPermissions[i]
                                .equals("android.permission.ACCESS_MOCK_LOCATION")
                                && !applicationInfo.packageName.equals(context.getPackageName())) {
                            count++;
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("Got exception " , e.getMessage());
            }
        }

        if (count > 0)
            return true;
        return false;
    }*/

    public static boolean isTimeAutomaticEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
        } else {
            //Menor a Android 4.2
            return android.provider.Settings.System.getInt(context.getContentResolver(), android.provider.Settings.System.AUTO_TIME, 0) == 1;
        }
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


    /*private void updateLocation() {
        if (authProvider.existSesion() && currentLatLng != null) {
            employeeProvider.saveLocation(authProvider.getId(), currentLatLng);
        }
    }*/

    @Override
    public void onAttach(@NonNull Context context) {
        myContext = (HomeTW) context;
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        timezoneID = TimeZone.getDefault().getID();
        employee = dbEmployees.getEmployee(authProvider.getId());
        if(employee!=null){
            setGreeting();
            reviewTakePhoto();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationCallback != null && fusedLocation != null) {
            fusedLocation.removeLocationUpdates(locationCallback);
        }
        handler.removeCallbacks(runnable);
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setScrollGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setIndoorLevelPickerEnabled(false);

        locationRequest = new com.google.android.gms.location.LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(3);

        startLocation();
    }

    public void setGreeting(){
        textViewName.setText(employee.getName());
        timezoneID = TimeZone.getDefault().getID();
        calendar = Calendar.getInstance(TimeZone.getTimeZone(timezoneID), Locale.getDefault());
        Date date = calendar.getTime();
        updateInfo(date);
    }

    public void removeWach(boolean remove) {
        if (remove) {
            if(isConnect){
                fusedLocation.removeLocationUpdates(locationCallback);
            }
            handler.removeCallbacks(runnable);
        } else {
            if(isConnect){
                startLocation2();
            }
            runnable.run();
        }
    }
}