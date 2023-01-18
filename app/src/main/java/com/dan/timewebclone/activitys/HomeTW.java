package com.dan.timewebclone.activitys;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static com.dan.timewebclone.fragments.MapFragment.circleImageViewMap;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.dan.timewebclone.BuildConfig;
import com.dan.timewebclone.R;
import com.dan.timewebclone.adapters.ViewPagerAdapter;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.fragments.HistoryChecksSendOkFragment;
import com.dan.timewebclone.fragments.MapFragment;
import com.dan.timewebclone.fragments.HistoryChecksLateSendFragment;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.ChecksProvider;
import com.dan.timewebclone.providers.EmployeeProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.BitmapCompat;
import androidx.viewpager.widget.ViewPager;

import com.dan.timewebclone.providers.ImageProvider;
import com.fxn.adapters.InstantImageAdapter;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.ImageQuality;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.UploadTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class HomeTW extends AppCompatActivity{

    private AuthProvider authHome;
    private ChecksProvider checksProvider;
    private ImageProvider mImageProvider;
    private EmployeeProvider employeeProvider;
    private DbChecks dbChecks;
    private Employee employee;

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

    public File mImageFile;
    public String imageFileStr;
    public byte[] image;
    public Date time1;
    public Bitmap imagenBitmap;
    public Uri fotoUri;
    public String imagetoBase64 = "";

    public ArrayList<String> idChecksDelete;
    public ArrayList<String> idChecksLateDelete;
    public LinearLayout linearLayoutLoadingHome;

    public ListenerRegistration listenerRegistration;
    //OnBackPressedCallback callback;
    private AlertDialog.Builder builderDialogExit;
    private AlertDialog.Builder builderDialogUpdateChecks;
    public ProgressDialog pdRevieData;

    private static final int REQUEST_PERMISSION_CAMERA = 100;
    private static final int TAKE_PICTURE = 101;
    private static final int REQUEST_PERMISSION_WRITE_STORAGE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_tw);
        setStatusBarColor();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("timeWEBMobile");

        linearLayoutLoadingHome = findViewById(R.id.linearLayoutLoadingHome);
        linearLayoutLoadingHome.setVisibility(View.VISIBLE);

        mTabLayout = findViewById(R.id.tabLayout);
        mViewPager = findViewById(R.id.viewPager);

        pdRevieData = new ProgressDialog(this);
        pdRevieData.setTitle("Revisando informacion");
        pdRevieData.setMessage("Espere un momento ...");
        pdRevieData.setCancelable(false);

        authHome = new AuthProvider();
        checksProvider = new ChecksProvider();
        idChecksDelete = new ArrayList<>();
        idChecksLateDelete = new ArrayList<>();
        mImageProvider = new ImageProvider();
        employeeProvider = new EmployeeProvider();
        dbChecks = new DbChecks(HomeTW.this);

        builderDialogExit = new AlertDialog.Builder(this);
        builderDialogUpdateChecks = new AlertDialog.Builder(HomeTW.this);

        mViewPager.setOffscreenPageLimit(3);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        mapFragment = new MapFragment();
        historyChecksSendOkFragment = new HistoryChecksSendOkFragment();
        historyChecksLateSendFragment = new HistoryChecksLateSendFragment();

        viewPagerAdapter.addFragment(mapFragment,"");
        viewPagerAdapter.addFragment(historyChecksSendOkFragment,"ENVIADOS");
        viewPagerAdapter.addFragment(historyChecksLateSendFragment,"PENDIENTES");

        mViewPager.setAdapter(viewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(tabSelected);



        //Actualizar los checks a eliminar
        dbChecks.updateChecksDelete(false, authHome.getId());
        idChecksDelete.clear();
        idChecksLateDelete.clear();

        //Revisar cambios en la vista
        listenerChangeViewPager();
        mViewPager.addOnPageChangeListener(pageChangeListener);

        //Revisar el empleado
        reviewEmployee();

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
                    goToSetings();
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
                        Intent in = new Intent(HomeTW.this, MainActivity.class);
                        in.putExtra("ChangePassword", "true");
                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(in);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builderDialogExit.show();
    }

    //Ir a configuracion
    private void goToSetings() {
        Intent i = new Intent(HomeTW.this, SettingsActivity.class);
        startActivity(i);
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
        List<Integer> statusSend = Arrays.asList(0, 2);
        ArrayList<Check> checksSend = dbChecks.getChecksSendSucces();
        ArrayList<Check> checksLateSend = dbChecks.getChecksNotSendSucces(statusSend);
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
                    String location = null;
                    try {
                        Geocoder geocoder = new Geocoder(HomeTW.this);
                        List<Address> addressList = geocoder.getFromLocation(checksSend.get(i).getCheckLat(), checksSend.get(i).getCheckLong(), 1);
                        String city = addressList.get(0).getLocality();
                        String address = addressList.get(0).getAddressLine(0);
                        location = address+" "+city;
                    }  catch (IOException e) {
                        Log.d("Error:", "Mensaje de error: " + e.getMessage());
                    }
                    if(location!= null){
                        if(location.toLowerCase().contains(textSearch.toLowerCase())){
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
                    String location1 = null;
                    try {
                        Geocoder geocoder = new Geocoder(HomeTW.this);
                        List<Address> addressList = geocoder.getFromLocation(checksLateSend.get(i).getCheckLat(), checksLateSend.get(i).getCheckLong(), 1);
                        String city = addressList.get(0).getLocality();
                        String address = addressList.get(0).getAddressLine(0);
                        location1 = address+" "+city;
                    }  catch (IOException e) {
                        Log.d("Error:", "Mensaje de error: " + e.getMessage());
                    }
                    if(location1 != null){
                        if(location1.toLowerCase().contains(textSearch.toLowerCase())){
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
        } else if(requestCode == REQUEST_PERMISSION_WRITE_STORAGE){
            if(permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                takePhoto();
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
                   ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
               }
           } else {
               takePhoto();
           }
    }

    //Tomar la foto
    public void takePhoto() {
    fotoUri=null;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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

                try {
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
        }
    }

    //Resultado de la camara
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == TAKE_PICTURE) {
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
        }
    }

    //Revisar la orientacion de la foto
    private Bitmap reviewOrientationImage(Bitmap compressImage) {
        ExifInterface ei = null;
        Bitmap rotatedBitmap = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                InputStream fi = getContentResolver().openInputStream(fotoUri);
                ei = new ExifInterface(fi);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
    public void updateChecks(String idCheck, int tipeSend, long date) {
        dbChecks.updateCheck(idCheck, tipeSend, date);
        if(tipeSend==1){
            historyChecksSendOkFragment.notifyChangeAdapter();
            historyChecksLateSendFragment.notifyChangeAdapter();
        } else{
            historyChecksLateSendFragment.notifyChangeAdapter();
        }
    }

    //Revisar checks pendientes, con mas de 30 dias y si no cuentas con checks pero se encuentran en firebase
    public void checkUpdateSend() {
        Check ch= new Check();
        ch.setIdUser(authHome.getId()+"100");
        checksProvider.createCheck(ch).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (linearLayoutLoadingHome.getVisibility() == View.GONE) {
                    pdRevieData.show();
                }
                time1 = Calendar.getInstance().getTime();
                //Eliminar mensajes de prueba internet
                checksProvider.getChecksByUser(authHome.getId() + "100").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            for (DocumentSnapshot document : value.getDocuments()) {
                                Check check = document.toObject(Check.class);
                                checksProvider.deleteCheck(check);
                            }
                        }
                    }
                });
                //Actualizar enviados
                dbChecks.reviewChecks(2);
                historyChecksLateSendFragment.updateChecks(2);

                historyChecksSendOkFragment.reviewData();
                historyChecksLateSendFragment.reviewData(historyChecksSendOkFragment.listChecks, historyChecksSendOkFragment.idDeleteChecks);
                historyChecksSendOkFragment.notifyChangeAdapter();
                historyChecksLateSendFragment.notifyChangeAdapter();
                checksProvider.deleteCheck(ch);
            }
        });
        if(!mapFragment.isOnlineNet()){
            if (linearLayoutLoadingHome.getVisibility() == View.VISIBLE) {
                linearLayoutLoadingHome.setVisibility(View.GONE);
            }
        }
    }

    //Mostrar mensaje de actualizaion de informacion
    public void mostrarUpdateChecks(ArrayList<Check> listChecksLate, ArrayList<Check> listChecksSendOk){
        builderDialogUpdateChecks.setMessage("Se encontraron registros en la red, ¿Deseas actualizar los registros?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        pdRevieData.show();
                        if(dbChecks.getChecksSendSucces().size() == 0){
                            for(int j = 0; j<listChecksSendOk.size(); j++){
                                dbChecks.insertCheck(listChecksSendOk.get(j));
                            }
                            historyChecksSendOkFragment.notifyChangeAdapter();
                        }

                        List<Integer> statusSend = Arrays.asList(0,2);
                        if(dbChecks.getChecksNotSendSucces(statusSend).size() == 0){
                            for(int j = 0; j<listChecksLate.size(); j++){
                                dbChecks.insertCheck(listChecksLate.get(j));
                            }
                            historyChecksLateSendFragment.notifyChangeAdapter();
                        }
                        pdRevieData.dismiss();
                        dialogInterface.dismiss();
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

    //Revisar el empleado que inicio sesion
    private void reviewEmployee() {
        DbEmployees dbEmployees = new DbEmployees(HomeTW.this);
        employee = dbEmployees.getEmployee(authHome.getId());
        if(employee == null){
            listenerRegistration = employeeProvider.getUserInfo(authHome.getId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                    if (documentSnapshot != null) {
                        if (documentSnapshot.exists()) {
                            employee = documentSnapshot.toObject(Employee.class);

                            //Si el usuario logeado no existe en db se actualiza la informacion
                            Employee employee1 = dbEmployees.getEmployee(employee.getIdUser());
                            if(employee1 == null){
                                if(dbEmployees.deleteAllEmployees() && dbChecks.deleteAllChecks()){
                                    dbEmployees.insertEmployye(employee);
                                    SharedPreferences sharedPref = getSharedPreferences("datos", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putBoolean("takePhoto", false);
                                    editor.commit();
                                    mapFragment.setInfoMap();
                                }
                            }

                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listenerRegistration != null)
            listenerRegistration.remove();

        dbChecks.updateChecksDelete(false, authHome.getId());
        idChecksDelete.clear();
            //if(callback != null)
        //  callback.remove();
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

    //Mensaje de salida de la app
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
                });
        builderDialogExit.show();
    }

    //Cambiar el color de la barra de notificaciones
    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black, this.getTheme()));
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
    }
}