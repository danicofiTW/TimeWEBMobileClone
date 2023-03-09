package com.dan.timewebclone.activitys;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.RenderMode;
import com.bumptech.glide.Glide;
import com.dan.timewebclone.R;
import com.dan.timewebclone.adapters.ViewPagerAdapter;
import com.dan.timewebclone.channel.NotificationMessage;
import com.dan.timewebclone.db.DbBitacoras;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.db.DbGeocercas;
import com.dan.timewebclone.fragments.HistoryChecksSendOkFragment;
import com.dan.timewebclone.fragments.MapFragment;
import com.dan.timewebclone.fragments.HistoryChecksLateSendFragment;
import com.dan.timewebclone.fragments.MapHuaweiFragment;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.models.FCMBody;
import com.dan.timewebclone.models.FCMResponse;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.BitacoraProvider;
import com.dan.timewebclone.providers.ChecksProvider;
import com.dan.timewebclone.providers.EmployeeProvider;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import com.dan.timewebclone.providers.ImageProvider;
import com.dan.timewebclone.providers.NotificationProvider;
import com.dan.timewebclone.retrofit.HMSApi;
import com.dan.timewebclone.retrofit.RetrofitClient;
import com.dan.timewebclone.services.HmsMessageService;
import com.dan.timewebclone.services.MyFirebaseMessagingClient;
import com.dan.timewebclone.utils.RelativeTime;
import com.dan.timewebclone.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import id.zelory.compressor.Compressor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeTW extends AppCompatActivity{

    private AuthProvider authHome;
    private ChecksProvider checksProvider;
    private ImageProvider mImageProvider;
    private EmployeeProvider employeeProvider;
    private DbChecks dbChecks;
    private DbEmployees dbEmployees;
    private DbGeocercas dbGeocercas;
    private DbBitacoras dbBitacoras;
    private Employee employee;
    private BitacoraProvider bitacoraProvider;
    private NotificationProvider notificationProvider;
    private static boolean isNotification;

    private SearchView searchView;
    private EditText editTextSearch;
    private LottieAnimationView animation, animationLoad;
    public MenuItem menuItemSearch;

    public Toolbar toolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private int tabSelected = 0;

    private MapFragment mapFragment;
    private MapHuaweiFragment mapHuaweiFragment;
    private HistoryChecksSendOkFragment historyChecksSendOkFragment;
    private HistoryChecksLateSendFragment historyChecksLateSendFragment;
    private ViewPager.OnPageChangeListener pageChangeListener;

    private String currentPhotoPath;
    private Uri photoURI;
    public File mImageFile;
    //public String imageFileStr;
    public byte[] image;
    public Date time1;
    public Bitmap imagenBitmap;
    //public Uri fotoUri;
    public String imagetoBase64 = "";
    public String image90 = "";
    public boolean revieUpdateRegisters;
    private boolean showReviewChecks = false;
    private boolean isMapHuawei = false;
    private int updateNet;
    public int review2Tipe;
    public int semanasSendOk;
    public int semanasSendLate;
    public float geoLat;
    public float geoLong;
    public float geoRadio;
    public String idGeocerca;
    public int numberChecksSendLate = 0;
    public long timeReal;

    public ArrayList<String> idChecksDelete;
    public ArrayList<String> idChecksLateDelete;
    public LinearLayout linearLayoutLoadingHome;
    public ConstraintLayout constraintLayoutProgress;
    //OnBackPressedCallback callback;
    private AlertDialog.Builder builderDialogExit;
    public AlertDialog.Builder builderDialogUpdateChecks;
    public ProgressDialog pdRevieData;
    public boolean updateData;
    public boolean reviewSettings;
    public boolean updateChecksNotSend;

    private static final int REQUEST_PERMISSION_CAMERA = 100;
    private static final int TAKE_PICTURE = 101;
    private static final int REQUEST_PERMISSION_WRITE_STORAGE = 200;

    AsyncTask<Void, Void, Long> runningTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_tw);
        isNotification = false;
        ShowNotificationActivity.updateActivity(this);
        ShowLocationActivity.updateActivity(this);

        linearLayoutLoadingHome = findViewById(R.id.linearLayoutLoadingHome);
        linearLayoutLoadingHome.setVisibility(View.VISIBLE);
        constraintLayoutProgress = findViewById(R.id.progressLayout);
        animation = findViewById(R.id.animationH);
        animationLoad = findViewById(R.id.animationHLoad);
        animation.isHardwareAccelerated();
        animation.setRenderMode(RenderMode.HARDWARE);
        animationLoad.isHardwareAccelerated();
        animationLoad.setRenderMode(RenderMode.HARDWARE);

        pdRevieData = new ProgressDialog(this);
        pdRevieData.setTitle("Revisando informacion");
        pdRevieData.setMessage("Espere un momento ...");
        pdRevieData.setCancelable(false);

        dbGeocercas = new DbGeocercas(this);
        dbBitacoras = new DbBitacoras(this);

        setStatusBarColor();

        updateData = false;
        updateChecksNotSend = false;

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("timeWEBMobile");

        mTabLayout = findViewById(R.id.tabLayout);
        mViewPager = findViewById(R.id.viewPager);

        authHome = new AuthProvider();
        checksProvider = new ChecksProvider();
        idChecksDelete = new ArrayList<>();
        idChecksLateDelete = new ArrayList<>();
        mImageProvider = new ImageProvider();
        employeeProvider = new EmployeeProvider();
        bitacoraProvider = new BitacoraProvider();
        dbChecks = new DbChecks(HomeTW.this);
        dbEmployees = new DbEmployees(HomeTW.this);
        notificationProvider = new NotificationProvider();

        builderDialogExit = new AlertDialog.Builder(this);
        builderDialogUpdateChecks = new AlertDialog.Builder(HomeTW.this);
        builderDialogUpdateChecks.setCancelable(false);

        mViewPager.setOffscreenPageLimit(3);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        geoRadio = getIntent().getFloatExtra("geoRadio",0);

        if(geoRadio == 0){
           // reviewEmployee();
        }
        setCrashlytics();
        historyChecksSendOkFragment = new HistoryChecksSendOkFragment();
        historyChecksLateSendFragment = new HistoryChecksLateSendFragment();

        if(Utils.isGMS(this)){
            mapFragment = new MapFragment();
            viewPagerAdapter.addFragment(mapFragment,"");
            stopService(new Intent(HomeTW.this, HmsMessageService.class));
        } else {
            isMapHuawei = true;
            mapHuaweiFragment = new MapHuaweiFragment();
            viewPagerAdapter.addFragment(mapHuaweiFragment,"");
            stopService(new Intent(HomeTW.this, MyFirebaseMessagingClient.class));
        }
        viewPagerAdapter.addFragment(historyChecksSendOkFragment,"ENVIADOS");
        viewPagerAdapter.addFragment(historyChecksLateSendFragment,"PENDIENTES");

        mViewPager.setAdapter(viewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(tabSelected);

        //Revisar cambios en la vista
        listenerChangeViewPager();
        mViewPager.addOnPageChangeListener(pageChangeListener);

        //Menu del toolbar
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.itemSignOut){
                    mostrarLogOut();
                } else if(item.getItemId() == R.id.itemProfile){
                    goToProfile();
                }else if(item.getItemId() == R.id.itemChangePassword){
                    goToChangePassword();
                } else if(item.getItemId() == R.id.itemSettings){
                    goToSettings();
                } else if(item.getItemId() == R.id.itemGeocercas){
                    goToGeocercas();
                }
                return true;
            }
        });

        //Cambiar la imagen del mapa
        setupTabIcon(true);
        //generateToken();
    }

    //Revisar cambios en la vista
    private void listenerChangeViewPager() {
        pageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) { }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) { }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        setupTabIcon(true);
                        viewSearchView(false);
                        /*if(isMapHuawei){
                            mapHuaweiFragment.removeSecondProces(false);
                        } else {
                            mapFragment.removeSecondProces(false);
                        }*/
                        break;
                    case 1:
                        setupTabIcon(false);
                        viewSearchView(true);
                        /*if(isMapHuawei){
                            mapHuaweiFragment.removeSecondProces(true);
                        } else {
                            mapFragment.removeSecondProces(true);
                        }*/
                        break;
                    default:
                        break;
                }
            }
        };
    }

    //Cambiar la imagen del mapa
    private void setupTabIcon(boolean mapOrange) {
        if(mapOrange){
            mTabLayout.getTabAt(0).setIcon(R.drawable.icon_maploc);
        } else {
            mTabLayout.getTabAt(0).setIcon(R.drawable.ic_map_w);
        }
        LinearLayout linearLayout = ((LinearLayout)((LinearLayout) mTabLayout.getChildAt(0)).getChildAt(0) );
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
        layoutParams.weight = 1f;
        linearLayout.setLayoutParams(layoutParams);
    }

    //Al momento de crear el menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        //Ajustes SearchView
        menuItemSearch = menu.findItem(R.id.action_search);
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

        //Ocultar vista
        viewSearchView(false);
        return true;
    }

    //Mostrar mensage de cerrar sesion
    public void mostrarLogOut(){
        builderDialogExit.setMessage("¿Deseas cerrar sesión?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(Utils.isGMS(HomeTW.this)){
                            employeeProvider.deleteToken(authHome.getId(), HomeTW.this);
                        } else {
                            employeeProvider.deleteTokenHMS(authHome.getId(), HomeTW.this);
                        }
                        authHome.signOut();
                        dialogInterface.dismiss();
                        removeGeocerca();
                        Intent in = new Intent(HomeTW.this, MainActivity.class);
                        in.putExtra("ChangePassword", "true");
                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(in);
                    }
                })
                .setNegativeButton("Olvidar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Intent in = new Intent(HomeTW.this, MainActivity.class);
                        if(dbChecks.deleteAllChecks() && dbEmployees.deleteAllEmployees() && dbGeocercas.deleteAllGeocercas() && dbBitacoras.deleteAllBitacoras()){
                            //employeeProvider.deleteToken(authHome.getId(), HomeTW.this);
                            if(Utils.isGMS(HomeTW.this)){
                                employeeProvider.deleteToken(authHome.getId(), HomeTW.this);
                            } else {
                                employeeProvider.deleteTokenHMS(authHome.getId(), HomeTW.this);
                            }
                            authHome.signOut();
                            removeGeocerca();
                            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(in);
                        }
                    }
                }).setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builderDialogExit.show();
    }

    //Ir a configuracion
    public void goToSettings() {
        Intent i = new Intent(HomeTW.this, SettingsActivity.class);
        startActivity(i);
    }

    //Ir a geocercas
    private void goToGeocercas() {
        if(dbBitacoras.getBitacorasByIdUser(authHome.getId()).size()!=0){

            if(isMapHuawei){
                if(mapHuaweiFragment.isVisible())
                    mapHuaweiFragment.firstReviewGeoface=true;
            } else {
                if(mapFragment.isVisible())
                    mapFragment.firstReviewGeoface=true;
            }
            //mViewPager.setCurrentItem(0);
            Intent i = new Intent(HomeTW.this, GeocercasActivity.class);
            startActivity(i);
        } else {
            if(Utils.isOnlineNet(this)){
                bitacoraProvider.getBitacorasByUser(authHome.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                          if(task.getResult().size()!=0){
                              if(isMapHuawei){
                                  mapHuaweiFragment.firstReviewGeoface=true;
                              } else {
                                  mapFragment.firstReviewGeoface=true;
                              }
                              //mapFragment.firstReviewGeoface=true;
                              //mViewPager.setCurrentItem(0);
                              Intent i = new Intent(HomeTW.this, GeocercasActivity.class);
                              startActivity(i);
                          } else {
                              removeGeocerca();
                              Toast.makeText(HomeTW.this, "No cuentas con geocercas asignadas", Toast.LENGTH_SHORT).show();
                          }
                        }
                    }
                });
            } else {
               removeGeocerca();
                Toast.makeText(this, "No cuentas con geocercas asignadas", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void removeGeocerca(){
        if(isMapHuawei){
            if(mapHuaweiFragment.isVisible()){
                if(mapHuaweiFragment.mapCircle!=null){
                    mapHuaweiFragment.mapCircle.remove();
                }
                mapHuaweiFragment.frameLayoutGoToPetalMaps.setVisibility(View.GONE);
                mapHuaweiFragment.frameLayoutViewRout.setVisibility(View.GONE);
            }
        } else {
            if(mapFragment!=null){
                if(mapFragment.isVisible()){
                    if(mapFragment.mapCircle!=null){
                        mapFragment.mapCircle.remove();
                    }
                    if(mapFragment.frameLayoutGoToGoogleMaps!=null)
                    mapFragment.frameLayoutGoToGoogleMaps.setVisibility(View.GONE);
                    if(mapFragment.frameLayoutViewRout!=null)
                    mapFragment.frameLayoutViewRout.setVisibility(View.GONE);
                }
            }
        }
        SharedPreferences sharedPref = getSharedPreferences("geocerca", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat("geoLat", 0);
        editor.putFloat("geoLong", 0);
        editor.putFloat("geoRadio", 0);
        editor.putString("idGeocerca", "");
        editor.apply();
        editor.commit();
        geoRadio=0;
        idGeocerca = "";
    }
    //Ir a cambiar el password
    private void goToChangePassword() {
        Intent i = new Intent(HomeTW.this, ChangePasswordActivity.class);
        startActivity(i);
    }

    //Ir a tu perfil
    private void goToProfile() {
        Intent i = new Intent(HomeTW.this, ProfileActivity.class);
        startActivity(i);
    }

    //Funcion para filtrar
    private void filterTextSearchView(String textSearch) {
        //List<Integer> statusSend = Arrays.asList(0, 2);
        ArrayList<Check> checksSend = dbChecks.getChecksSendSucces(authHome.getId());
        ArrayList<Check> checksLateSend = dbChecks.getChecksNotSendSucces(authHome.getId());
        ArrayList<Check> checksFilterSend = new ArrayList<>();
        ArrayList<Check> checksFilterLateSend = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        for (int i = 0; i < checksSend.size(); i++) {
            Date aux = new Date(checksSend.get(i).getTime());
            String date = sdf.format(aux);
            if (date.toLowerCase().contains(textSearch.toLowerCase())) {
                checksFilterSend.add(checksSend.get(i));
            } else {
                String tipeCheck = getTipeCheck(checksSend.get(i));
                if(tipeCheck.toLowerCase().contains(textSearch.toLowerCase())){
                    checksFilterSend.add(checksSend.get(i));
                } else {
                    if(checksSend.get(i).getNameGeocerca() != null){
                        if(checksSend.get(i).getNameGeocerca().toLowerCase().contains(textSearch.toLowerCase())){
                            checksFilterSend.add(checksSend.get(i));
                        }
                    } else {
                        String sinGeo = "Sin geocerca asignada";
                        if(sinGeo.toLowerCase().contains(textSearch.toLowerCase())){
                            checksFilterSend.add(checksSend.get(i));
                        }
                    }
                }
            }
        }

        historyChecksSendOkFragment.filterSendOk(checksFilterSend);

        for (int i = 0; i < checksLateSend.size(); i++) {
            Date aux1 = new Date(checksLateSend.get(i).getTime());
            String date1 = sdf.format(aux1);
            if (date1.toLowerCase().contains(textSearch.toLowerCase())) {
                checksFilterLateSend.add(checksLateSend.get(i));
            } else {
                String tipeCheck1 = getTipeCheck(checksLateSend.get(i));
                if(tipeCheck1.toLowerCase().contains(textSearch.toLowerCase())){
                    checksFilterLateSend.add(checksLateSend.get(i));
                } else {
                    if(checksLateSend.get(i).getNameGeocerca() != null){
                        if(checksLateSend.get(i).getNameGeocerca().toLowerCase().contains(textSearch.toLowerCase())){
                            checksFilterLateSend.add(checksLateSend.get(i));
                        }
                    } else {
                        String sinGeo = "Sin geocerca asignada";
                        if(sinGeo.toLowerCase().contains(textSearch.toLowerCase())){
                            checksFilterLateSend.add(checksLateSend.get(i));
                        }
                    }
                }
            }
        }

        historyChecksLateSendFragment.filterSendLate(checksFilterLateSend);
    }

    //Obtener el tipo en español
    private String getTipeCheck(Check check) {
        if(check.getTipeCheck().equals("startWork")){
            return "Registro de Entrada";
        } else if(check.getTipeCheck().equals("startEating")){
            return "Registro de Comida";
        } else if(check.getTipeCheck().equals("finishEating")){
            return "Registro de Fin Comida";
        } else if(check.getTipeCheck().equals("finishWork")){
            return "Registro de Salida";
        } else {
            return "";
        }
    }

    //Ver el SearchView
    public void viewSearchView(boolean viewSearch){
        if(menuItemSearch!=null){
            if(viewSearch){
                menuItemSearch.setEnabled(true);
                menuItemSearch.setVisible(true);
            } else {
                toolbar.collapseActionView();
                menuItemSearch.setEnabled(false);
                menuItemSearch.setVisible(false);
            }
        }
    }

    /*private void setupTabIcon() {
        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_camera_white);
        LinearLayout linearLayout = ((LinearLayout)((LinearLayout) mTabLayout.getChildAt(0)).getChildAt(0) );
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
        layoutParams.weight = 0.5f;
        linearLayout.setLayoutParams(layoutParams);
    }*/


    //Resultado al pedir permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_PERMISSION_CAMERA){
            if(permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                takePhoto();
            }
        } else {
            if(isMapHuawei){
               if (requestCode == mapHuaweiFragment.LOCATION_REQUEST_CODE) {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            if (mapHuaweiFragment.gpsActived()) {
                                mapHuaweiFragment.startLocation2();
                            } else {
                                mapHuaweiFragment.showAlertDialogNOGPS();
                            }
                        } else {
                            mapHuaweiFragment.checkLocationPermissions();
                        }
                    } else {
                        mapHuaweiFragment.checkLocationPermissions();
                    }
                }
            } else {
                if (requestCode == mapFragment.LOCATION_REQUEST_CODE) {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            if (mapFragment.gpsActived()) {
                                mapFragment.startLocation2();
                            } else {
                                mapFragment.showAlertDialogNOGPS();
                            }
                        } else {
                            mapFragment.checkLocationPermissions();
                        }
                    } else {
                        mapFragment.checkLocationPermissions();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //Revisar permisos de escritura y camara
    public void checkPermissionStorage(){
           if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
               if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
               && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
               ){
                   takePhoto();
               } else {
                   ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
               }
           } else {
               takePhoto();
           }
    }

    ActivityResultLauncher<Uri> someActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(),
        result -> {
            try {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                InputStream imageStream = getContentResolver().openInputStream(photoURI);
                imagenBitmap = BitmapFactory.decodeStream(imageStream, null, options);
                imageStream.close();
                //Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);
                if(imagenBitmap != null){
                    Bitmap mB = reviewOrientationImage(imagenBitmap);
                    if(mB != null){
                        imagenBitmap = mB;
                    }
                    if(mImageFile != null)

                    try {
                        imagenBitmap = new Compressor(HomeTW.this).setQuality(80).compressToBitmap(mImageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    imagenBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                    byte[] image1 = stream.toByteArray();
                    image90 = Base64.encodeToString(image1, Base64.DEFAULT);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Bitmap compressImage = createImageScaleBitmap(imagenBitmap);
                    compressImage.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    image = baos.toByteArray();
                    imagetoBase64 = Base64.encodeToString(image, Base64.DEFAULT);

                    if(isMapHuawei){
                        Glide.with(HomeTW.this).load(image1).into(mapHuaweiFragment.circleImageViewMap);
                    } else {
                        Glide.with(HomeTW.this).load(image1).into(mapFragment.circleImageViewMap);
                    }
                }
                //circleImageViewMap.setImageBitmap(imagenBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    );

    /*ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //Intent data = result.getData();
                        //imagenBitmap = (Bitmap) data.getExtras().get("data");
                        try {

                            Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);
                            Bitmap mB = reviewOrientationImage(photo);
                            if(mB != null){
                                photo = mB;
                            }
                            if(mImageFile != null)

                            try {
                                photo = new Compressor(HomeTW.this).setQuality(80).compressToBitmap(mImageFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            photo.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                            byte[] image1 = stream.toByteArray();
                            image90 = Base64.encodeToString(image1, Base64.DEFAULT);

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            Bitmap compressImage = createImageScaleBitmap(photo);
                            compressImage.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                            image = baos.toByteArray();
                            imagetoBase64 = Base64.encodeToString(image, Base64.DEFAULT);

                            if(isMapHuawei){
                                Glide.with(HomeTW.this).load(image1).into(mapHuaweiFragment.circleImageViewMap);
                            } else {
                                Glide.with(HomeTW.this).load(image1).into(mapFragment.circleImageViewMap);
                            }
                            //circleImageViewMap.setImageBitmap(imagenBitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            });*/

    //Tomar la foto
    public void takePhoto() {
        File photoFile = new File(getFilesDir(), "my_images");
        // Continue only if the File was successfully created
        if (photoFile != null) {
            mImageFile = photoFile;
            photoURI = FileProvider.getUriForFile(Objects.requireNonNull(this),
                    "com.dan.timewebclone.fileprovider",
                    photoFile);
            if(photoURI != null){
                someActivityResultLauncher.launch(photoURI);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //Revisar la orientacion de la foto
    private Bitmap reviewOrientationImage(Bitmap compressImage) {
        ExifInterface ei = null;
        Bitmap rotatedBitmap = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                if(photoURI != null){
                    ei = new ExifInterface(getContentResolver().openInputStream(photoURI));
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotatedBitmap = rotateImage(compressImage, 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotatedBitmap = rotateImage(compressImage, 180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotatedBitmap = rotateImage(compressImage, 270);
                            break;
                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            rotatedBitmap = compressImage;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rotatedBitmap;
    }

    //Rotar imagen
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    //Crear imagen a escala
    private Bitmap createImageScaleBitmap(Bitmap imagenBitmap) {
        final int maxSize = 200;
        int outWidth;
        int outHeight;
        int inWidth = imagenBitmap.getWidth();
        int inHeight = imagenBitmap.getHeight();
        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }

        return Bitmap.createScaledBitmap(imagenBitmap, outWidth, outHeight, false);
    }

    //Notificar cambio en checks pendientes
    public void updateViewLateCheck() {
        historyChecksLateSendFragment.notifyChangeAdapter();
    }

    //Notificar cambio checks enviados
    public void updateChecks(String idCheck, int tipeSend, long date, long dateSend) {
        dbChecks.updateCheck(idCheck, tipeSend, date, dateSend);
        if(tipeSend==1){
            historyChecksSendOkFragment.notifyChangeAdapter();
            historyChecksLateSendFragment.notifyChangeAdapter();
        } else{
            historyChecksLateSendFragment.notifyChangeAdapter();
        }
    }

    //Revisar checks pendientes, con mas de 30 dias y si no cuentas con checks pero se encuentran en firebase
    public void checkUpdateSend() {
            updateChecksNotSend = true;
            updateData = true;
            Check ch = new Check();
            ch.setIdUser(authHome.getId() + "100");
            checksProvider.createCheck(ch).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    if (linearLayoutLoadingHome.getVisibility() == View.GONE) {
                        if(isMapHuawei){
                            mapHuaweiFragment.loadin(false);
                        } else {
                            mapFragment.loadin(false);
                        }
                        pdRevieData.show();
                    }
                    time1 = Calendar.getInstance().getTime();
                    //Eliminar mensajes de prueba internet
                    checksProvider.getChecksByUser(authHome.getId() + "100").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    Check check = document.toObject(Check.class);
                                    checksProvider.deleteCheck(check);
                                }
                            }
                        }
                    });

                    //Actualizar enviados
                    checksProvider.deleteCheck(ch);
                    dbChecks.reviewChecks(2);
                    historyChecksLateSendFragment.updateChecks(2);
                    historyChecksLateSendFragment.notifyChangeAdapter();

                    checksProvider.getChecksByUser(authHome.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                int numberChecksUser = task.getResult().size();
                                if (numberChecksUser != 0) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                                    Date mDateD = Calendar.getInstance(TimeZone.getTimeZone(TimeZone.getDefault().getID()), Locale.getDefault()).getTime();
                                    String date = sdf.format(mDateD);

                                    SharedPreferences sharedPref = getSharedPreferences("datos", MODE_PRIVATE);
                                    String dateRevie = sharedPref.getString("dateReview", "");
                                    dateRevie = "18/01/2023";

                                    if (dateRevie.equals("")) {
                                        reviewChecksOlderThan31Days(task.getResult());
                                        savePreferenceReviewChecks(date);
                                    } else {
                                        long dateSave = 0;
                                        long dateThisMoment = 0;
                                        try {
                                            Date d = sdf.parse(dateRevie);
                                            dateSave = d.getTime();
                                            Date d1 = sdf.parse(date);
                                            dateThisMoment = d1.getTime();
                                            long diferencia = dateThisMoment - dateSave;
                                            double diasD = Math.floor(diferencia / (1000 * 60 * 60 * 24));
                                            int dias = (int) diasD;
                                            if (dias != 0) {
                                                reviewChecksOlderThan31Days(task.getResult());
                                                savePreferenceReviewChecks(date);
                                            } else {
                                                /*if (linearLayoutLoadingHome.getVisibility() == View.VISIBLE) {
                                                    linearLayoutLoadingHome.setVisibility(View.GONE);
                                                }*/
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    /*if (linearLayoutLoadingHome.getVisibility() == View.VISIBLE) {
                                        linearLayoutLoadingHome.setVisibility(View.GONE);
                                    } else {*/
                                        if (pdRevieData.isShowing()) {
                                            pdRevieData.dismiss();
                                        }
                                    //}

                                    revieUpdateRegisters = false;
                                    if (dbChecks.getChecksNotSendSucces(authHome.getId()).size() == 0 ) {
                                        checksProvider.getChecksByUserAndStatusSend(authHome.getId(),2).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if(task.isSuccessful()){
                                                    if(task.getResult().size() != 0){
                                                        if(revieUpdateRegisters == false){
                                                            //showReviewChecks = true;
                                                            review2Tipe++;
                                                            revieUpdateRegisters = true;
                                                        } else {
                                                            review2Tipe++;
                                                        }
                                                    } else {
                                                        review2Tipe++;
                                                    }
                                                } else {
                                                    review2Tipe++;
                                                }
                                            }
                                        });
                                    } else {
                                        review2Tipe++;
                                    }
                                    if (dbChecks.getChecksSendSucces(authHome.getId()).size() == 0) {
                                        checksProvider.getChecksByUserAndStatusSend(authHome.getId(), 1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if(task.isSuccessful()){
                                                    if(task.getResult().size() != 0){
                                                        if(revieUpdateRegisters == false){

                                                            //showReviewChecks = true;

                                                            review2Tipe++;
                                                            revieUpdateRegisters = true;
                                                        } else {
                                                            review2Tipe++;
                                                        }
                                                    } else {
                                                        review2Tipe++;
                                                    }
                                                } else {
                                                    review2Tipe++;
                                                }
                                            }
                                        });
                                    } else {
                                        review2Tipe++;
                                    }

                                    } else {
                                        if (pdRevieData.isShowing()) {
                                            pdRevieData.dismiss();
                                        }
                                }
                            } else {
                                if (pdRevieData.isShowing()) {
                                    pdRevieData.dismiss();
                                }
                            }
                        }
                    });
                }
            });
    }

    public void reviewChecksOlderThan31Days(QuerySnapshot result) {
        Check check;
        ArrayList<String> idDeleteChecks = new ArrayList<>();
        RelativeTime relativeTime = new RelativeTime();
        int numberDelete = 0;
        for (DocumentSnapshot document : result) {
            check = document.toObject(Check.class);
            int dias = 31;
            if(check.getTime() != null){
                dias = relativeTime.compareToDate(check.getTime());
            }
            if(dias > 31){
                numberDelete ++;
                idDeleteChecks.add(check.getIdCheck());
            } else {
                break;
            }
        }

        if(numberDelete!=0) {
            checksProvider.deleteChecksForId(idDeleteChecks);
            dbChecks.delete(idDeleteChecks);
            if(numberDelete==1){
                Toast.makeText(HomeTW.this, "Se elimino un registro enviado hace más de 31 días", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HomeTW.this, "Se eliminaron " + numberDelete + " registros enviados hace más de 31 días", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void savePreferenceReviewChecks(String dateReview){
        SharedPreferences sharedPref = getSharedPreferences("datos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("dateReview", dateReview);
        editor.commit();
    }

    //Mostrar mensaje de actualizaion de informacion
    public void mostrarUpdateChecks(){
            builderDialogUpdateChecks.setMessage("Se encontraron registros en la red, ¿Deseas actualizar los registros?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(Utils.isOnlineNet(HomeTW.this)){
                                dialogInterface.dismiss();
                                constraintLayoutProgress.setVisibility(View.VISIBLE);
                                updateDataNet();
                            } else {
                                //revieUpdateRegisters = false;
                                Toast.makeText(HomeTW.this, "Conectate a internet para actualizar información", Toast.LENGTH_SHORT).show();
                                mostrarUpdateChecks();
                            }
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            builderDialogUpdateChecks.show();
    }

    private void updateDataNet() {
        updateNet = 2;
            checksProvider.getChecksByUserAndStatusSend(authHome.getId(), 1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        ArrayList<Check> checks = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            dbChecks.insertCheck(document.toObject(Check.class));
                        }
                        updateNet--;
                        if(updateNet == 0){
                            constraintLayoutProgress.setVisibility(View.GONE);
                        }
                        historyChecksSendOkFragment.notifyChangeAdapter();
                    }
                }
            });

        checksProvider.getChecksByUserAndStatusSend(authHome.getId(), 2).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (DocumentSnapshot document : task.getResult()) {
                        dbChecks.insertCheck(document.toObject(Check.class));
                    }
                    historyChecksLateSendFragment.notifyChangeAdapter();
                    updateNet--;
                    if(updateNet == 0){
                        constraintLayoutProgress.setVisibility(View.GONE);
                    }
                }
            }
        });
    }


        //Actualizar la vista al eliminar
    public void updateDeleteAllChecksSeendOk(boolean b) {
        if(historyChecksSendOkFragment.deleteAllChecks != null){
            if(b){
                historyChecksSendOkFragment.deleteAllChecks.setChecked(true);
            } else{
                historyChecksSendOkFragment.deleteAllChecks.setChecked(false);
                if(idChecksDelete.size()==0){
                    historyChecksSendOkFragment.showImageDelete(false);
                }
            }
        }
    }


    //Actualizar la vista al eliminar
    public void updateDeleteAllChecksLate(boolean b) {
        if(historyChecksLateSendFragment.deleteAllChecks != null){
            if(b){
                historyChecksLateSendFragment.deleteAllChecks.setChecked(true);
            } else{
                historyChecksLateSendFragment.deleteAllChecks.setChecked(false);
                if(idChecksLateDelete.size()==0){
                    historyChecksLateSendFragment.showImageDelete(false);
                }
            }
        }
    }

    public int isViewDeleteSendOk(){
        return historyChecksSendOkFragment.deleteChecks.getVisibility();
    }

    public int isViewDeleteSendLate() {
        return historyChecksLateSendFragment.deleteChecks.getVisibility();
    }

    public void showNumberDelete(){
        if(idChecksDelete.size() != 0)
            historyChecksSendOkFragment.textViewNumberChecksDelete.setText(""+idChecksDelete.size());
        if(idChecksLateDelete.size() != 0)
            historyChecksLateSendFragment.textViewNumberChecksDelete.setText(""+idChecksLateDelete.size());
    }

    //Revisar el empleado que inicio sesion
    private void reviewEmployee() {
        employee = dbEmployees.getEmployee(authHome.getId());
        //Si el usuario logeado no existe en db se actualiza la informacion
        if(employee == null){
            employeeProvider.getUserInfo(authHome.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if (task.getResult() != null) {
                            if (task.getResult().exists()) {
                                employee = task.getResult().toObject(Employee.class);
                                if(dbEmployees.deleteAllEmployees() && dbChecks.deleteAllChecks() && dbBitacoras.deleteAllBitacoras() && dbGeocercas.deleteAllGeocercas()){
                                    removeGeocerca();
                                    dbEmployees.insertEmployye(employee);
                                }
                            }
                        }
                    }
                }
            });
        }
    }

   /* @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Clear the Activity's bundle of the subsidiary fragments' bundles.
        outState.clear();
    }*/

    //Accion hacia atras del dispositivo
    @Override
    public void onBackPressed() {
        mostrarSalida();
        //super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!isNotification)
        ShowNotificationActivity.updateStatusActivity(false);
        if(historyChecksLateSendFragment.isVisible()){
            if(historyChecksLateSendFragment.deleteChecks.getVisibility() == View.VISIBLE){
                historyChecksLateSendFragment.updateDelete();
            }
        }
        if(historyChecksSendOkFragment.isVisible()){
            if(historyChecksSendOkFragment.deleteChecks.getVisibility() == View.VISIBLE){
                historyChecksSendOkFragment.updateDelete();
            }
        }
    }

    @Override
    protected void onStart() {
        ShowNotificationActivity.updateStatusActivity(true);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeGeocerca();
    }

    public static void updateStatusActivity(boolean b) {
        isNotification = b;
    }

    @Override
        protected void onPause() {
        super.onPause();
        if (runningTask != null)
            runningTask.cancel(true);
    }

    @Override
    protected void onResume(){
        super.onResume();
        setCrashlytics();
        checkTime();
        if(employee != null){
            if(authHome.getId() == null){
                authHome.loginEmail(employee.getEmail(), employee.getPassword());
            }
        }
    }

    public void checkTime() {
        if(!Utils.isTimeAutomaticEnabled(this)){
            showGoToChangeTimeAutomatic();
        } else {
            SharedPreferences sharedPref = getSharedPreferences("TIME", Context.MODE_PRIVATE);
            timeReal = sharedPref.getLong("timeReal", 0);
            if(timeReal < Utils.getTime().getTime()){
                if(Utils.isOnlineNet(HomeTW.this)){
                    if (runningTask != null)
                        runningTask.cancel(true);
                    runningTask = new GetTimeNet();
                    runningTask.execute();
                }
            } else {
                if(Utils.isOnlineNet(HomeTW.this)){
                    showGoToChangeTimeAutomatic();
                } else {
                    showGoToConectInternet();
                }
            }
        }
    }

    private void saveTimePreference(){}




    public void showGoToConectInternet(){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(HomeTW.this);
        builder.setMessage("La hora no parece ser la correcta conéctate a internet para continuar.")
                .setPositiveButton("Red Mobil", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                    }
                }).setNeutralButton("Wi-Fi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                }).setCancelable(false).create().show();
    }

    public void showGoToChangeTimeAutomatic() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage("Por favor activa la fecha y hora proporcionadas por la red")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_DATE_SETTINGS), 0);
                    }
                }).setCancelable(false).create().show();
    }


    //Mensaje de salida de la app
        public void mostrarSalida(){
            builderDialogExit.setMessage("¿Deseas salir de TimeWEBMobile?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            if(historyChecksLateSendFragment.isVisible()){
                                if(historyChecksLateSendFragment.deleteChecks.getVisibility() == View.VISIBLE){
                                    historyChecksLateSendFragment.updateDelete();
                                }
                            }
                            if(historyChecksSendOkFragment.isVisible()){
                                if(historyChecksSendOkFragment.deleteChecks.getVisibility() == View.VISIBLE){
                                    historyChecksSendOkFragment.updateDelete();
                                }
                            }
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



        private void generateToken(){
            if(employee!=null){
                employeeProvider.updateToken(employee.getIdUser(), this);
                employee = dbEmployees.getEmployee(authHome.getId());
            } else {
                employee = dbEmployees.getEmployee(authHome.getId());
                if(employee!=null){
                    employeeProvider.updateToken(employee.getIdUser(), this);
                    employee = dbEmployees.getEmployee(authHome.getId());
                }
            }
        }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {

                    //Toast.makeText(this, "Permiso otorgado para enviar notificaciones", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(this, "No cuentas con permiso para enviar notificaciones", Toast.LENGTH_SHORT).show();
                }
            });

    public void askNotificationPermission(String body, String idCheck) {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                sendNotification(body,idCheck);
                // FCM SDK (and your app) can post notifications.
            } else {
                shouldShowRequestPermissionRationale(POST_NOTIFICATIONS);
                requestPermissionLauncher.launch(POST_NOTIFICATIONS);
            }
        } else {
            sendNotification(body,idCheck);
        }
    }

        public void sendNotification(String body, String idCheck){
            employee = dbEmployees.getEmployee(authHome.getId());
            if(Utils.isGMS(HomeTW.this)){
                if(employee.getToken() != null && !employee.getToken().equals("")){
                    Map<String, String> map = new HashMap<>();
                    map.put("title", "REGISTROS");
                    map.put("body", body);
                    map.put("idCheck", idCheck);
                    map.put("idUser", employee.getIdUser());
                    FCMBody fcmBody = new FCMBody(employee.getToken(), "high","REGISTROS", map);
                    notificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if(response.body()!=null){
                                if(response.body().getSuccess() == 1){
                                    //Toast.makeText(HomeTW.this, "La notificacion se envio correctamente", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(HomeTW.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(HomeTW.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error" + t.getMessage());
                        }
                    });
                } else {
                    Toast.makeText(HomeTW.this, "Token Nullo", Toast.LENGTH_SHORT).show();
                }
            } else {
                if(employee.getToken() != null && !employee.getToken().equals("")){
                    sendNotificationHMS(employee.getToken(), body, idCheck);
                } else {
                    Toast.makeText(HomeTW.this, "Token Nullo", Toast.LENGTH_SHORT).show();
                }
            }

        }

    public void sendNotificationHMS(String token, String body, String idCheck){
        HMSApi services = RetrofitClient.getInstanceHMS().create(HMSApi.class);;
        //HMSApi apiInterface = retrofit.create(WebService.class);
        NotificationMessage notificationMessage = new NotificationMessage.Builder(
                "REGISTROS", body+"", token)
                .build();

        Call<Void> call = services.createNotification(
                "Bearer " + token,
                notificationMessage
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.code() == 200){
                    if (response.body() != null){
                        Toast.makeText(HomeTW.this, response.body()+"", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(HomeTW.this, "Sin respuesta", Toast.LENGTH_SHORT).show();//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("Error", "Error" + t.getMessage());
            }
        });
    }

    private void setCrashlytics(){
        FirebaseCrashlytics.getInstance().setUserId(authHome.getId());
        FirebaseCrashlytics.getInstance().setCustomKey("geoRadio", geoRadio);
        if(employee != null)
            FirebaseCrashlytics.getInstance().setCustomKey("nameUser", employee.getName());
        FirebaseCrashlytics.getInstance().log("Se ah generado una falla");
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

    private final class GetTimeNet extends AsyncTask<Void, Void, Long> {

        @Override
        protected Long doInBackground(Void... voids) {
            return Utils.getTimeLongNet();
        }

        @Override
        protected void onPostExecute(Long result) {
            if(result != 0){
                long differenceNet = Math.abs(System.currentTimeMillis() - result);
                if(differenceNet > 300000){
                    showGoToChangeTimeAutomatic();
                } else {
                    SharedPreferences sharedPref = getSharedPreferences("TIME", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putLong("timeReal", result);
                    editor.apply();
                    editor.commit();
                }
            }
        }
    }


}
