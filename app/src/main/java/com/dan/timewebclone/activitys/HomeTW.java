package com.dan.timewebclone.activitys;

import static com.dan.timewebclone.fragments.MapFragment.circleImageViewMap;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.dan.timewebclone.R;
import com.dan.timewebclone.adapters.ViewPagerAdapter;
import com.dan.timewebclone.db.DbBitacoras;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.db.DbGeocercas;
import com.dan.timewebclone.fragments.HistoryChecksSendOkFragment;
import com.dan.timewebclone.fragments.MapFragment;
import com.dan.timewebclone.fragments.HistoryChecksLateSendFragment;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.BitacoraProvider;
import com.dan.timewebclone.providers.ChecksProvider;
import com.dan.timewebclone.providers.EmployeeProvider;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
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
import androidx.core.graphics.BitmapCompat;
import androidx.viewpager.widget.ViewPager;

import com.dan.timewebclone.providers.ImageProvider;
import com.dan.timewebclone.utils.RelativeTime;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import id.zelory.compressor.Compressor;

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

    private SearchView searchView;
    private EditText editTextSearch;
    public MenuItem menuItemSearch;

    public Toolbar toolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private int tabSelected = 0;

    private MapFragment mapFragment;
    private HistoryChecksSendOkFragment historyChecksSendOkFragment;
    private HistoryChecksLateSendFragment historyChecksLateSendFragment;
    private ViewPager.OnPageChangeListener pageChangeListener;

    private String currentPhotoPath;
    private Uri photoURI;
    public File mImageFile;
    public String imageFileStr;
    public byte[] image;
    public Date time1;
    public Bitmap imagenBitmap;
    public Uri fotoUri;
    public String imagetoBase64 = "";
    public String image90 = "";
    private boolean revieUpdateRegisters;
    private int updateNet;
    public int semanasSendOk;
    public int semanasSendLate;
    public float geoLat;
    public float geoLong;
    public float geoRadio;
    public String idGeocerca;
    public int numberChecksSendLate = 0;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_tw);

        linearLayoutLoadingHome = findViewById(R.id.linearLayoutLoadingHome);
        linearLayoutLoadingHome.setVisibility(View.VISIBLE);
        constraintLayoutProgress = findViewById(R.id.progressLayout);

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

        builderDialogExit = new AlertDialog.Builder(this);
        builderDialogUpdateChecks = new AlertDialog.Builder(HomeTW.this);
        builderDialogUpdateChecks.setCancelable(false);

        mViewPager.setOffscreenPageLimit(3);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        geoRadio = getIntent().getFloatExtra("geoRadio",0);

        if(geoRadio == 0){
            reviewEmployee();
        }

        mapFragment = new MapFragment();
        historyChecksSendOkFragment = new HistoryChecksSendOkFragment();
        historyChecksLateSendFragment = new HistoryChecksLateSendFragment();

        viewPagerAdapter.addFragment(mapFragment,"");
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
                        mapFragment.removeWach(false);
                        break;
                    case 1:
                        setupTabIcon(false);
                        viewSearchView(true);
                        mapFragment.removeWach(true);
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
                        authHome.signOut();
                        dialogInterface.dismiss();
                        if(historyChecksLateSendFragment.deleteChecks.getVisibility() == View.VISIBLE){
                            historyChecksLateSendFragment.updateDelete();
                        }
                        if(historyChecksSendOkFragment.deleteChecks.getVisibility() == View.VISIBLE){
                            historyChecksSendOkFragment.updateDelete();
                        }
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
                            authHome.signOut();
                            if(historyChecksLateSendFragment.deleteChecks.getVisibility() == View.VISIBLE){
                                historyChecksLateSendFragment.updateDelete();
                            }
                            if(historyChecksSendOkFragment.deleteChecks.getVisibility() == View.VISIBLE){
                                historyChecksSendOkFragment.updateDelete();
                            }
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
        if(historyChecksLateSendFragment.deleteChecks.getVisibility() == View.VISIBLE){
            historyChecksLateSendFragment.updateDelete();
        }
        if(historyChecksSendOkFragment.deleteChecks.getVisibility() == View.VISIBLE){
            historyChecksSendOkFragment.updateDelete();
        }
        Intent i = new Intent(HomeTW.this, SettingsActivity.class);
        startActivity(i);
    }

    //Ir a geocercas
    private void goToGeocercas() {
        if(dbBitacoras.getBitacorasByIdUser(authHome.getId()).size()!=0){
            if(historyChecksLateSendFragment.deleteChecks.getVisibility() == View.VISIBLE){
                historyChecksLateSendFragment.updateDelete();
            }
            if(historyChecksSendOkFragment.deleteChecks.getVisibility() == View.VISIBLE){
                historyChecksSendOkFragment.updateDelete();
            }
            mapFragment.firstReviewGeoface=true;
            //mViewPager.setCurrentItem(0);
            Intent i = new Intent(HomeTW.this, GeocercasActivity.class);
            startActivity(i);
        } else {
            if(mapFragment.isOnlineNet()){
                bitacoraProvider.getBitacorasByUser(authHome.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                          if(task.getResult().size()!=0){
                              if(historyChecksLateSendFragment.deleteChecks.getVisibility() == View.VISIBLE){
                                  historyChecksLateSendFragment.updateDelete();
                              }
                              if(historyChecksSendOkFragment.deleteChecks.getVisibility() == View.VISIBLE){
                                  historyChecksSendOkFragment.updateDelete();
                              }
                              mapFragment.firstReviewGeoface=true;
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
        if(mapFragment.mapCircle!=null){
            mapFragment.mapCircle.remove();
        }
        mapFragment.frameLayoutGoToGoogleMaps.setVisibility(View.GONE);
        mapFragment.frameLayoutViewRout.setVisibility(View.GONE);
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
        if(historyChecksLateSendFragment.deleteChecks.getVisibility() == View.VISIBLE){
            historyChecksLateSendFragment.updateDelete();
        }
        if(historyChecksSendOkFragment.deleteChecks.getVisibility() == View.VISIBLE){
            historyChecksSendOkFragment.updateDelete();
        }
        Intent i = new Intent(HomeTW.this, ChangePasswordActivity.class);
        startActivity(i);
    }

    //Ir a tu perfil
    private void goToProfile() {

        if(historyChecksLateSendFragment.deleteChecks.getVisibility() == View.VISIBLE){
            historyChecksLateSendFragment.updateDelete();
        }
        if(historyChecksSendOkFragment.deleteChecks.getVisibility() == View.VISIBLE){
            historyChecksSendOkFragment.updateDelete();
        }
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
        } else if (requestCode == mapFragment.LOCATION_REQUEST_CODE) {
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

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //Intent data = result.getData();
                        //imagenBitmap = (Bitmap) data.getExtras().get("data");
                        try {

                            Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);
                            //imageView.setImageBitmap(photo);
                            //imagenBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fotoUri);
                            /*BitmapCompat.getAllocationByteCount(imagenBitmap);
                            int bytes = imagenBitmap.getByteCount();
                            ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
                            imagenBitmap.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
                            byte[] image1 = buffer.array();*/
                            Bitmap mB = reviewOrientationImage(photo);
                            if(mB!=null){
                                photo = mB;
                            }
                            if(mImageFile != null)
                            photo = new Compressor(HomeTW.this).setQuality(80).compressToBitmap(mImageFile);

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            photo.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                            byte[] image1 = stream.toByteArray();
                            image90 = Base64.encodeToString(image1, Base64.DEFAULT);

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            Bitmap compressImage = createImageScaleBitmap(photo);
                            compressImage.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                            image = baos.toByteArray();
                            imagetoBase64 = Base64.encodeToString(image, Base64.DEFAULT);

                            Glide.with(HomeTW.this).load(image1).into(circleImageViewMap);
                            //circleImageViewMap.setImageBitmap(imagenBitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            });

    //Tomar la foto
    public void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mImageFile = photoFile;
                photoURI = FileProvider.getUriForFile(this,
                        "com.dan.timewebclone.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                someActivityResultLauncher.launch(takePictureIntent);
            }
        }



        /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            OutputStream outputStream = null;
            mImageFile = null;
            fotoUri = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                ContentValues values = new ContentValues();
                imageFileStr = System.currentTimeMillis() + "image";
                values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileStr);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TimeWeb");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);

                Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                fotoUri = resolver.insert(collection, values);

               /* List<ResolveInfo> resolvedIntentActivities = getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                    String packageName = resolvedIntentInfo.activityInfo.packageName;
                    grantUriPermission(packageName, FileProvider.getUriForFile(HomeTW.this, BuildConfig.APPLICATION_ID + ".fileprovider", new File(fotoUri.getPath())), Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }*/

                /*try {
                    outputStream = resolver.openOutputStream(fotoUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                resolver.update(fotoUri, values, null, null);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                startActivityForResult(takePictureIntent, TAKE_PICTURE);
            } else {
                imageFileStr = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                String filename = System.currentTimeMillis() + ".jpg";
                mImageFile = new File(imageFileStr, filename);
                if (mImageFile != null) {
                    fotoUri =  Uri.fromFile(mImageFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                    startActivityForResult(takePictureIntent, TAKE_PICTURE);
                }
            }
        }*/
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

    //Resultado de la camara
    //@Override
    //public void onActivityResult(int requestCode, int resultCode, Intent data) {
      //  super.onActivityResult(requestCode, resultCode, data);
        /*if (resultCode == Activity.RESULT_OK && requestCode == TAKE_PICTURE) {
            if(fotoUri!=null){
                imagenBitmap = null;

                try {
                    imagenBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fotoUri);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    //Bitmap imageScaled =
                    Bitmap mB = reviewOrientationImage(imagenBitmap);
                    Bitmap compressImage = null;
                    if(mB!=null){
                        compressImage = createImageScaleBitmap(mB);
                    } else {
                        compressImage = createImageScaleBitmap(imagenBitmap);
                    }
                    compressImage.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                    image = baos.toByteArray();
                    imagetoBase64 = Base64.encodeToString(image, Base64.DEFAULT);
                    Glide.with(HomeTW.this).load(fotoUri).dontAnimate().into(circleImageViewMap);

                    //MapFragment.circleImageViewMap.setImageBitmap(mB);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //saveImageDirectori();
        }*/
    //}

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
                        mapFragment.loadin(false);
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
                                                if (linearLayoutLoadingHome.getVisibility() == View.VISIBLE) {
                                                    linearLayoutLoadingHome.setVisibility(View.GONE);
                                                }
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    if (linearLayoutLoadingHome.getVisibility() == View.VISIBLE) {
                                        linearLayoutLoadingHome.setVisibility(View.GONE);
                                    } else {
                                        if (pdRevieData.isShowing()) {
                                            pdRevieData.dismiss();
                                        }
                                    }


                                    revieUpdateRegisters = false;
                                    if (dbChecks.getChecksNotSendSucces(authHome.getId()).size() == 0 ) {
                                        checksProvider.getChecksByUserAndStatusSend(authHome.getId(),2).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if(task.isSuccessful()){
                                                    if(task.getResult().size() != 0){
                                                        if(revieUpdateRegisters == false){
                                                            mostrarUpdateChecks();
                                                            revieUpdateRegisters = true;
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                    }
                                    if (dbChecks.getChecksSendSucces(authHome.getId()).size() == 0) {
                                        checksProvider.getChecksByUserAndStatusSend(authHome.getId(), 1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if(task.isSuccessful()){
                                                    if(task.getResult().size() != 0){
                                                        if(revieUpdateRegisters == false){
                                                            mostrarUpdateChecks();
                                                            revieUpdateRegisters = true;
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                    }

                                    } else {
                                    if (linearLayoutLoadingHome.getVisibility() == View.VISIBLE) {
                                        linearLayoutLoadingHome.setVisibility(View.GONE);
                                    } else {
                                        if (pdRevieData.isShowing()) {
                                            pdRevieData.dismiss();
                                        }
                                    }
                                }
                            } else {
                                if (linearLayoutLoadingHome.getVisibility() == View.VISIBLE) {
                                    linearLayoutLoadingHome.setVisibility(View.GONE);
                                } else {
                                    if (pdRevieData.isShowing()) {
                                        pdRevieData.dismiss();
                                    }
                                }
                            }
                        }
                    });
                }
            });
            if (!mapFragment.isOnlineNet()) {
                if (linearLayoutLoadingHome.getVisibility() == View.VISIBLE) {
                    linearLayoutLoadingHome.setVisibility(View.GONE);
                }
            }
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
            if(linearLayoutLoadingHome.getVisibility() == View.VISIBLE) {
                linearLayoutLoadingHome.setVisibility(View.GONE);
            }
            if(numberDelete==1){
                Toast.makeText(HomeTW.this, "Se elimino un registro enviado hace más de 31 días", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HomeTW.this, "Se eliminaron " + numberDelete + " registros enviados hace más de 31 días", Toast.LENGTH_SHORT).show();
            }
        } else {
            if(linearLayoutLoadingHome.getVisibility() == View.VISIBLE) {
                linearLayoutLoadingHome.setVisibility(View.GONE);
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
                        if(mapFragment.isOnlineNet()){
                            dialogInterface.dismiss();
                            constraintLayoutProgress.setVisibility(View.VISIBLE);
                            updateDataNet();
                        }
                        else {
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

        @Override
        protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            //Clear the Activity's bundle of the subsidiary fragments' bundles.
            outState.clear();
        }

        //Accion hacia atras del dispositivo
        @Override
        public void onBackPressed() {
            mostrarSalida();
            //super.onBackPressed();
        }

        @Override
        protected void onStop() {
            super.onStop();
        }

        @Override
        protected void onPause() {
            super.onPause();
            if(historyChecksLateSendFragment.deleteChecks.getVisibility() == View.VISIBLE){
                historyChecksLateSendFragment.updateDelete();
            }
            if(historyChecksSendOkFragment.deleteChecks.getVisibility() == View.VISIBLE){
                historyChecksSendOkFragment.updateDelete();
            }
        }


    //Mensaje de salida de la app
            public void mostrarSalida(){
                builderDialogExit.setMessage("¿Deseas salir de TimeWEBMobile?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                if(historyChecksLateSendFragment.deleteChecks.getVisibility() == View.VISIBLE){
                                    historyChecksLateSendFragment.updateDelete();
                                }
                                if(historyChecksSendOkFragment.deleteChecks.getVisibility() == View.VISIBLE){
                                    historyChecksSendOkFragment.updateDelete();
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
