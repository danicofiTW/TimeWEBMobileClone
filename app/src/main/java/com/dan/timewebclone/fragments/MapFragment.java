package com.dan.timewebclone.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.R;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.ChecksProvider;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.dan.timewebclone.providers.ImageProvider;
import com.dan.timewebclone.utils.SNTPClient;
import com.fxn.pix.Options;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.net.MediaType;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.type.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private AuthProvider authProvider;
    private ChecksProvider checksProvider;
    private EmployeeProvider employeeProvider;

    public FloatingActionsMenu floatingActionsMenu;
    private FloatingActionButton sendStartWork, sendStartEating, sendFinishEating, sendFinishWork;

    private GoogleMap map;
    private View mView;
    private TextView textViewGoodTime, textViewTime, textViewName, textViewState;
    private SupportMapFragment mapFragment;

    public byte[] mImagen;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private Marker marker;
    private Button buttonConnect;
    private Boolean isConnect = false ;
    public static CircleImageView circleImageViewMap;
    private ImageView imageViewPhotoMap;

    private LatLng currentLatLng;
    private HomeTW myContext;
    //private Long date;
    private Long mDateL;
    private String mDateS;
    private Date mDateD;
    private Location mLocation;
    public ProgressDialog pdSendCheck;
    public Date time1;
    private String zone = "America/Mexico_City";

    private FusedLocationProviderClient fusedLocation;
    private ListenerRegistration listenerRegistration;

    private LocationRequest locationRequest;

    String urlImage;
    double seconds = 0;
    public boolean secondsIsOver = false;
    Handler handler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            DbEmployees dbEmployees = new DbEmployees(myContext);
            Employee employee = dbEmployees.getEmployee(authProvider.getId());
            if(employee!=null){
                if(isTimeAutomaticEnabled(myContext)){
                    secondsIsOver = false;
                    mDateD = new Date();
                    updateInfo(mDateD, employee.getName());
                    if(time1!=null){
                        if(!evaluarLimite(mDateD, time1)){
                            if(pdSendCheck.isShowing()){
                                pdSendCheck.cancel();
                            }
                        }
                    }
                    if(myContext.time1 != null){
                        if(!evaluarLimite(mDateD, myContext.time1)){
                            if(myContext.pdRevieData.isShowing()){
                                myContext.pdRevieData.cancel();
                                time1 = null;
                            }
                        }
                    }
                } else {
                    disconnect();
                    secondsIsOver = true;
                    if(textViewName!=null && textViewState != null && textViewTime != null && textViewGoodTime != null){
                        if(employee!=null){
                            textViewName.setText(employee.getName());
                            textViewTime.setText("");
                            textViewGoodTime.setText("");
                            if(isConnect){
                                textViewState.setText("¡Listo para enviar registros!");
                            } else {
                                textViewState.setText("!Conectate para enviar¡");
                            }
                        }
                    }
                }
            }
            handler.postDelayed(runnable,1000);
        }
    };

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                mLocation = location;
                if(isMockLocationOn(location, myContext)){
                    disconnect();
                    Toast.makeText(myContext, "Se ha detectado ubicacion de prueba, por lo que no se puede enviar registros", Toast.LENGTH_LONG).show();
                } else{
                    currentLatLng= new LatLng(location.getLatitude(),location.getLongitude());
                //date = location.getTime();

                    if(marker!=null){
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

                if(myContext.pdRevieData.isShowing()){
                    myContext.pdRevieData.dismiss();
                }
                    //updateInfo(date);
                    //updateLocation();
                }
            }
    };

    private void getTimeZone(){
        SNTPClient.getDate(TimeZone.getTimeZone(zone), new SNTPClient.Listener() {
            @Override
            public void onTimeResponse(String rawDate, Date date, Exception ex) {
                mDateD = date;
            }
        });
    }

    private void updateInfo(Date Date, String name) {
        if(Date != null){
            SimpleDateFormat sdf = new SimpleDateFormat("HH");
            String dateTime = sdf.format(Date.getTime());
            SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
            String date1 = sdf1.format(Date.getTime());
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String dateTS = sdf2.format(Date.getTime());
            String texto = "";
            mDateL = Date.getTime();

            int hour = Integer.parseInt(dateTime);
            if(hour>=6 && hour<12){
                texto="Buenos días";
            }
            if(hour>=12 && hour<19){
                texto="Buenas tardes";
            }
            if (hour>=19 || hour<6){
                texto="Buenas noches";
            }
            if(textViewName!=null && textViewState != null && textViewTime != null && textViewGoodTime != null){
                textViewName.setText(name);
                textViewTime.setText(date1);
                textViewGoodTime.setText(texto);
                if(isConnect){
                    textViewState.setText("¡Listo para enviar registros!");
                } else {
                    textViewState.setText("!Conectate para enviar¡");
                }
            }
        }
    }

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
        if(mapFragment != null){
            mapFragment.getMapAsync(this);}
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authProvider = new AuthProvider();
        checksProvider = new ChecksProvider();
        employeeProvider = new EmployeeProvider();

        fusedLocation = LocationServices.getFusedLocationProviderClient(view.getContext());

        floatingActionsMenu = view.findViewById(R.id.groupButton);
        buttonConnect = view.findViewById(R.id.btnConectDriver);
        sendStartWork = view.findViewById(R.id.sendStartWork);
        sendStartEating = view.findViewById(R.id.sendStartEating);
        sendFinishEating = view.findViewById(R.id.sendFinishEating);
        sendFinishWork = view.findViewById(R.id.sendFinishWork);
        circleImageViewMap = view.findViewById(R.id.circleImageMap);
        imageViewPhotoMap = view.findViewById(R.id.mapSelectImage);
        textViewGoodTime = view.findViewById(R.id.textViewGoodTime);
        textViewTime = view.findViewById(R.id.textViewTime);
        textViewName = view.findViewById(R.id.textViewName);
        textViewState = view.findViewById(R.id.textViewStatus);

        pdSendCheck = new ProgressDialog(myContext);
        pdSendCheck.setTitle("Enviando");
        pdSendCheck.setMessage("Espere un momento");
        pdSendCheck.setCancelable(false);


        if(!checkIfLocationOpened()){
            Toast.makeText(myContext, "Activa tu ubicacion", Toast.LENGTH_SHORT).show();
        }

        runnable.run();
        updateLocation();


        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isConnect){
                    disconnect();
                } else {
                    if(map==null) {
                        Toast.makeText(myContext, "No se puede conectar", Toast.LENGTH_SHORT).show();
                    } else if (!secondsIsOver){
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
                pdSendCheck.show();
                if(authProvider.existSesion() && currentLatLng!=null && isConnect){
                    seendCheck("startWork");
                } else {
                    pdSendCheck.dismiss();
                    Toast.makeText(myContext, "Conectate para enviar el registro", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sendStartEating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdSendCheck.show();
                if(authProvider.existSesion() && currentLatLng!=null && isConnect) {
                    seendCheck("startEating");
                } else {
                    pdSendCheck.dismiss();
                    Toast.makeText(myContext, "Conectate para enviar el registro", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sendFinishEating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdSendCheck.show();
                if(authProvider.existSesion() && currentLatLng!=null && isConnect) {
                    seendCheck("finishEating");
                } else {
                    pdSendCheck.dismiss();
                    Toast.makeText(myContext, "Conectate para enviar el registro", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sendFinishWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdSendCheck.show();
                if(authProvider.existSesion() && currentLatLng!=null && isConnect ){
                    seendCheck("finishWork");
                 } else {
                    pdSendCheck.dismiss();
                    Toast.makeText(myContext, "Conectate para enviar el registro", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imageViewPhotoMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //((HomeTW)getActivity()).startPix();
                takePhoto();
            }
        });

    }

    private boolean checkIfLocationOpened() {
        //boolean provider =  LocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        //System.out.println("Provider contains=> " + provider);
        LocationManager lm = (LocationManager)myContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(gps_enabled && network_enabled) {
        //if (provider.contains("gps") || provider.contains("network")){
            return true;
        //}
        } else{
            new AlertDialog.Builder(myContext)
                    .setMessage("Activa tu ubicacion para continuar")
                    .setPositiveButton("Ajustes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            myContext.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancelar",null)
                    .show();
        }
        return false;
    }

    private void takePhoto() {
        ((HomeTW)getActivity()).checkPermissionStorage();
    }


    public void setImageDefault(){
        circleImageViewMap.setImageResource(R.drawable.icon_image);
    }

    private void seendCheck(String tipe) {
        if(checkIfLocationOpened()){
            Check check = new Check();
            check.setTipeCheck(tipe);
            check.setIdUser(authProvider.getId());
            check.setTime(mDateL);
            check.setCheckLat(currentLatLng.latitude);
            check.setCheckLong(currentLatLng.longitude);
            check.setStatusSend(0);

            time1 = Calendar.getInstance().getTime();

            if (myContext.imagetoBase64 != null) {
                if (myContext.imagetoBase64 != "") {
                    check.setImage(myContext.imagetoBase64);
                    if(myContext.fotoUri != null){
                        check.setUrlImage(myContext.fotoUri.toString());
                        myContext.fotoUri = null;
                    }
                    myContext.imagetoBase64 = "";
                }
            }

            checksProvider.createCheck(check).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    int tipeSend;
                    Date time2 = Calendar.getInstance().getTime();
                    if(time1!=null){
                        if (evaluarLimite(time2, time1)) {
                            tipeSend = 1;
                        } else {
                            tipeSend = 2;
                        }
                    } else{
                        tipeSend = 2;
                    }
                    time1 = null;
                    check.setStatusSend(tipeSend);
                    checksProvider.updateStatus(check.getIdCheck(), tipeSend);
                    myContext.updateChecks(check.getIdCheck(), tipeSend, check.getTime());
                    if( pdSendCheck.isShowing()){
                        pdSendCheck.dismiss();
                    }
                    Toast.makeText(myContext, "Registro enviado", Toast.LENGTH_SHORT).show();
                }
            });

            DbChecks dbChecks = new DbChecks(myContext);
            long id = dbChecks.insertCheck(check);
            myContext.image = null;
            if (id > 0) {
                myContext.updateViewLateCheck();
                setImageDefault();
                pdSendCheck.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Toast.makeText(myContext, "Registros no enviado, conectate a internet !!", Toast.LENGTH_SHORT).show();
                    }
                });
                //myContext.setUrlImage();
            } else {
                Toast.makeText(myContext, "Error al registrar", Toast.LENGTH_SHORT).show();
            }
        } else {
            pdSendCheck.dismiss();
        }
    }

    public static boolean evaluarLimite(Date date1, Date date2) {
        boolean correcto = false;
        long diferencia = (Math.abs(date1.getTime() - date2.getTime())) / 1000;
        long limit = (5 * 1000) / 1000L;//limite de tiempo

        if (diferencia <= limit) {
            correcto= true;
        }
        return correcto;
    }

    private void disconnect() {
        if(fusedLocation!=null && marker!=null){
            buttonConnect.setText("Conectarse");
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
                    if(!isOnlineNet()){
                        myContext.pdRevieData.show();
                        myContext.time1 = Calendar.getInstance().getTime();
                    }
                    buttonConnect.setText("Desconectarse");
                    isConnect = true;
                    fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    map.setMyLocationEnabled(false);
                }
                else {
                    showAlertDialogNOGPS();
                }
            }
            else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                map.setMyLocationEnabled(false);
            }
            else {
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
        myContext.pdRevieData.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();

    }

    private void checkLocationPermissions(){
        myContext.pdRevieData.dismiss();
        if(ContextCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(myContext,Manifest.permission.ACCESS_FINE_LOCATION)){
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
            if(location!=null){
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



    private void updateLocation() {
       if(authProvider.existSesion() && currentLatLng != null){
            employeeProvider.saveLocation(authProvider.getId(), currentLatLng);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        myContext = (HomeTW) context;
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationCallback != null && fusedLocation != null) {
            fusedLocation.removeLocationUpdates(locationCallback);
        }
        if(listenerRegistration != null){
            listenerRegistration.remove();
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
        locationRequest.setSmallestDisplacement(2);

        startLocation();
    }

}