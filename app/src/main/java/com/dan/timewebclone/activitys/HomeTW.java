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
import android.content.pm.PackageManager;
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

    private SearchView searchView;
    private EditText editTextSearch;
    public MenuItem menuItemSearch;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdapter viewPagerAdapter;

    //private MaterialSearchBar mSearchBar;

    private MapFragment mapFragment;
    private HistoryChecksSendOkFragment historyChecksSendOkFragment;
    private HistoryChecksLateSendFragment historyChecksLateSendFragment;

    public File mImageFile;
    public String routImage, imageFileStr;
    public byte[] image;
    public int index;
    public Date time1;
    ImageProvider mImageProvider;
    EmployeeProvider employeeProvider;
    public Bitmap imagenBitmap;
    public Uri fotoUri;
    Employee employee;
    public ArrayList<String> idChecksDelete;
    public ArrayList<String> idChecksLateDelete;
    AlertDialog.Builder builderDialogUpdateChecks;

    ListenerRegistration listenerRegistration;
    //OnBackPressedCallback callback;
    AlertDialog.Builder builderDialogExit;
    public ProgressDialog pdRevieData;
    public Toolbar toolbar;
    //public ProgressDialog pdSendCheck;

    //private static final String BASE = "data:image/png;base64,";
    public String imagetoBase64 = "";
    int tabSelected = 0;

    private static final int REQUEST_PERMISSION_CAMERA = 100;
    private static final int TAKE_PICTURE = 101;
    private static final int REQUEST_PERMISSION_WRITE_STORAGE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_tw);
        setStatusBarColor();
        //checkBack();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("timeWEBMobile");

        //mSearchBar = findViewById(R.id.searchBar);
        mTabLayout = findViewById(R.id.tabLayout);
        mViewPager = findViewById(R.id.viewPager);

        pdRevieData = new ProgressDialog(this);
        pdRevieData.setTitle("Obteniendo informacion");
        pdRevieData.setMessage("Espere un momento");
        pdRevieData.setCancelable(false);


        //mSearchBar.setOnSearchActionListener(this);
        //mSearchBar.inflateMenu(R.menu.menu);

        //onCreateOptionsMenu();
        authHome = new AuthProvider();
        checksProvider = new ChecksProvider();
        idChecksDelete = new ArrayList<>();
        idChecksLateDelete = new ArrayList<>();
        builderDialogUpdateChecks = new AlertDialog.Builder(HomeTW.this);

        mViewPager.setOffscreenPageLimit(3);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mapFragment = new MapFragment();
        historyChecksSendOkFragment = new HistoryChecksSendOkFragment();
        historyChecksLateSendFragment = new HistoryChecksLateSendFragment();
        mImageProvider = new ImageProvider();
        employeeProvider = new EmployeeProvider();
        builderDialogExit = new AlertDialog.Builder(this);


        //viewPagerAdapter.addFragment(photoFragment,"");
        viewPagerAdapter.addFragment(mapFragment,"MAPA");
        viewPagerAdapter.addFragment(historyChecksSendOkFragment,"ENVIADOS CORRECTAMENTE");
        viewPagerAdapter.addFragment(historyChecksLateSendFragment,"ENVIADOS CON RETARDO");

        mViewPager.setAdapter(viewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(tabSelected);

        DbChecks dbChecks = new DbChecks(HomeTW.this);
        dbChecks.updateChecksDelete(false, authHome.getId());
        idChecksDelete.clear();
        idChecksLateDelete.clear();

        ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) { }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) { }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        viewSearchView(false);
                        break;
                    case 1:
                        //mapFragment.floatingActionsMenu.collapse();
                        viewSearchView(true);
                        break;
                    default:
                        break;
                }
            }
        };

        mViewPager.addOnPageChangeListener(pageChangeListener);

        reviewEmployee();

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.itemSignOut){
                    signOut();
                } else if(item.getItemId() == R.id.itemProfile){
                    goToProfile();
                }else if(item.getItemId() == R.id.itemChangePassword){
                    goToChangePassword();
                }
                return true;
            }
        });
        checkUpdateSend();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
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
        viewSearchView(false);
        return true;
    }

    private void goToChangePassword() {
        Intent i = new Intent(HomeTW.this, ChangePasswordActivity.class);
        startActivity(i);
    }

    private void goToProfile() {
        Intent i = new Intent(HomeTW.this, ProfileActivity.class);
        startActivity(i);
    }

    private void filterTextSearchView(String textSearch) {
        DbChecks dbChecks = new DbChecks(HomeTW.this);
        ArrayList<Check> checksSend = dbChecks.getChecksSendSucces();
        List<Integer> statusSend = Arrays.asList(0, 2);
        ArrayList<Check> checksLateSend = dbChecks.getChecksNotSendSucces(statusSend);
        ArrayList<Check> checksFilterSend = new ArrayList<>();
        ArrayList<Check> checksFilterLateSend = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        for (int i = 0; i < checksSend.size(); i++) {
            Date aux = new Date(checksSend.get(i).getTime());
            String date = sdf.format(aux);
            String location = null;
            String tipeCheck = getTipeCheck(checksSend.get(i));
            try {
                Geocoder geocoder = new Geocoder(HomeTW.this);
                List<Address> addressList = geocoder.getFromLocation(checksSend.get(i).getCheckLat(), checksSend.get(i).getCheckLong(), 1);
                String city = addressList.get(0).getLocality();
                String address = addressList.get(0).getAddressLine(0);
                location = address+" "+city;
            }  catch (IOException e) {
                Log.d("Error:", "Mensaje de error: " + e.getMessage());
            }

            if (date.toLowerCase().contains(textSearch.toLowerCase())) {
                checksFilterSend.add(checksSend.get(i));
            } else if(tipeCheck.toLowerCase().contains(textSearch.toLowerCase())){
                checksFilterSend.add(checksSend.get(i));
            } else if(location!= null){
                if(location.toLowerCase().contains(textSearch.toLowerCase())){
                    checksFilterSend.add(checksSend.get(i));
                }
            }
        }

        for (int i = 0; i < checksLateSend.size(); i++) {
            Date aux1 = new Date(checksLateSend.get(i).getTime());
            String date1 = sdf.format(aux1);
            String location1 = null;
            String tipeCheck1 = getTipeCheck(checksLateSend.get(i));
            try {
                Geocoder geocoder = new Geocoder(HomeTW.this);
                List<Address> addressList = geocoder.getFromLocation(checksLateSend.get(i).getCheckLat(), checksLateSend.get(i).getCheckLong(), 1);
                String city = addressList.get(0).getLocality();
                String address = addressList.get(0).getAddressLine(0);
                location1 = address+" "+city;
            }  catch (IOException e) {
                Log.d("Error:", "Mensaje de error: " + e.getMessage());
            }
            if (date1.toLowerCase().contains(textSearch.toLowerCase())) {
                checksFilterLateSend.add(checksLateSend.get(i));
            } else if(tipeCheck1.toLowerCase().contains(textSearch.toLowerCase())){
                checksFilterLateSend.add(checksLateSend.get(i));
            } else if(location1 != null){
                if(location1.toLowerCase().contains(textSearch.toLowerCase())){
                    checksFilterLateSend.add(checksLateSend.get(i));
                }
            }
        }

        historyChecksSendOkFragment.filterSendOk(checksFilterSend);
        historyChecksLateSendFragment.filterSendLate(checksFilterLateSend);
    }

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

    private void signOut(){
        DbChecks dbChecks = new DbChecks(HomeTW.this);
        if(dbChecks.deleteAllChecks()) {
            DbEmployees dbEmployees = new DbEmployees(HomeTW.this);
            if (dbEmployees.deleteEmployee(authHome.getId())) {
                authHome.signOut();
                Intent i = new Intent(HomeTW.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } else {
                Toast.makeText(this, "No se pudo eliminar el usuario en db", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No se pudieron eliminar los checks en db", Toast.LENGTH_SHORT).show();
        }
    }

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



    public void checkPermissionStorage(){
           if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
               if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
               && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
               ){
                   takePhoto();
               } else {
                   //PedirPermisoStorage();
                   ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
               }
           } else {
               takePhoto();
           }
    }

    private void PedirPermisoStorage() {
        //Comprobación 'Racional'
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            AlertDialog AD;
            AlertDialog.Builder ADBuilder = new AlertDialog.Builder(HomeTW.this);
            ADBuilder.setMessage("Permite que TimeWEBMobile acceda al contenido multimedia");
            ADBuilder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Solicitamos permisos
                    ActivityCompat.requestPermissions(HomeTW.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }
            });
            AD = ADBuilder.create();
            AD.show();
        } else {
            ActivityCompat.requestPermissions(HomeTW.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

    }

    public void takePhoto() {
    fotoUri=null;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
                    Bitmap compressImage = createImageScaleBitmap(mB);
                    compressImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    image = baos.toByteArray();
                    imagetoBase64 = Base64.encodeToString(image, Base64.DEFAULT);
                    Glide.with(HomeTW.this).load(fotoUri).dontAnimate().into(circleImageViewMap);

                    //MapFragment.circleImageViewMap.setImageBitmap(mB);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }



            /*if(mImageFile!=null){
                Bitmap myBitmap = BitmapFactory.decodeFile(mImageFile.getAbsolutePath());
                MapFragment.circleImageViewMap.setImageBitmap(myBitmap);
            } else if(fotoUri != null){
                MapFragment.circleImageViewMap.setImageURI(fotoUri);
            }*/
            //previsualizacionProducto.setImageBitmap(myBitmap);


            //ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            //mImageFile = new File(returnValue.get(0));
           /* routImage = mImageFile.getAbsolutePath();
            imagen = BitmapFactory.decodeFile(routImage);
            Bitmap compressImage = imagen;
            try {
                compressImage = new Compressor(this)
                        .setMaxWidth(500)
                        .setMaxHeight(500)
                        .setQuality(75)
                        .compressToBitmap(mImageFile);

                //Bitmap mImage = imagen;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressImage.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                image = baos.toByteArray();
                imagetoBase64 = Base64.encodeToString(image,Base64.DEFAULT);
                byte[] decodedString = Base64.decode(imagetoBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

               // saveImage(image);
                MapFragment.circleImageViewMap.setImageBitmap(compressImage);
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            //saveImageDirectori();
        }
    }

    private Bitmap reviewOrientationImage(Bitmap compressImage) {
        ExifInterface ei = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            InputStream fi = getContentResolver().openInputStream(fotoUri);
                ei = new ExifInterface(fi);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;

        switch(orientation) {
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
        return rotatedBitmap;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

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
        //Bitmap.createScaledBitmap(imagenBitmap, 140, 140, false);
    }

    public void updateViewLateCheck() {
        List<Integer> statusSend = Arrays.asList(0,2);
        historyChecksLateSendFragment.getChecksById(statusSend);
        //pdSendCheck.dismiss();
        //Toast.makeText(HomeTW.this, "Registro agregado", Toast.LENGTH_SHORT).show();
    }

    public void updateChecks(String idCheck, int tipeSend, long date) {
        DbChecks dbChecks = new DbChecks(HomeTW.this);
        boolean save = dbChecks.updateCheck(idCheck, tipeSend, date);
        if(save==true){
            Toast.makeText(this, "Se ah actualizado la informacionn en db", Toast.LENGTH_SHORT).show();
        }
        historyChecksSendOkFragment.updateStatusChecks(tipeSend);
        List<Integer> statusSend = Arrays.asList(tipeSend);
        historyChecksLateSendFragment.getChecksById(statusSend);
    }

    private void checkUpdateSend() {
        ChecksProvider checksProvider1 = new ChecksProvider();
        Check ch= new Check();
        ch.setIdUser(authHome.getId()+"100");
        checksProvider1.createCheck(ch).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pdRevieData.show();
                time1 = Calendar.getInstance().getTime();
                //Eliminar mensajes de prueba internet
                checksProvider1.getChecksByUser(authHome.getId()+"100").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(value!=null){
                            for (DocumentSnapshot document: value.getDocuments()) {
                                Check check = document.toObject(Check.class);
                                checksProvider.deleteCheck(check);
                            }
                        }
                    }
                });
                //Actualizar enviados
                historyChecksSendOkFragment.reviewData();
                historyChecksSendOkFragment.updateChecks(2);
                DbChecks dbChecks = new DbChecks(HomeTW.this);
                boolean save = dbChecks.reviewChecks(2);
                if(save==true){
                    //List<Integer> statusSend = Arrays.asList(2);
                    historyChecksLateSendFragment.reviewData(historyChecksSendOkFragment.listChecks);
                    //menuFragment.getChecksById(statusSend);
                    checksProvider1.deleteCheck(ch);
                } else {
                    pdRevieData.dismiss();
                }
            }
        });
    }

    public void mostrarUpdateChecks(DbChecks dbChecks, ArrayList<Check> listChecksLate, ArrayList<Check> listChecksSendOk){
        builderDialogUpdateChecks.setMessage("Se encontraron registros en la red, ¿Deseas actualizar los registros?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(dbChecks.getChecksSendSucces().size() == 0){
                            for(int j = 0; j<listChecksSendOk.size(); j++){
                                dbChecks.insertCheck(listChecksSendOk.get(j));
                            }
                            historyChecksSendOkFragment.getChecksById();
                        }

                        List<Integer> statusSend = Arrays.asList(0,2);
                        if(dbChecks.getChecksNotSendSucces(statusSend).size() == 0){
                            for(int j = 0; j<listChecksLate.size(); j++){
                                dbChecks.insertCheck(listChecksLate.get(j));
                            }
                            historyChecksLateSendFragment.getChecksById(statusSend);
                        }
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
                            Employee employee1 = dbEmployees.getEmployee(employee.getIdUser());
                            if(employee1 == null){
                                dbEmployees.insertEmployye(employee);
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listenerRegistration != null)
            listenerRegistration.remove();

        DbChecks dbChecks = new DbChecks(HomeTW.this);
        dbChecks.updateChecksDelete(false, authHome.getId());
        idChecksDelete.clear();
        //if(callback != null)
        //  callback.remove();
    }

    @Override
    public void onBackPressed() {
        mostrarSalida();
        //super.onBackPressed();
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
                });
        builderDialogExit.show();
    }

    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black, this.getTheme()));
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Clear the Activity's bundle of the subsidiary fragments' bundles.
        outState.clear();
    }


    /*public void onCheckboxClicked(View view) {
        menuFragment.onCheckboxClicked(view);
    }*/

}