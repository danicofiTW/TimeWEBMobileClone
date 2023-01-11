package com.dan.timewebclone.activitys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dan.timewebclone.R;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.fragments.BottomSheetCompany;
import com.dan.timewebclone.fragments.BottomSheetPhone;
import com.dan.timewebclone.fragments.BottomSheetSelectImage;
import com.dan.timewebclone.fragments.BottomSheetUserName;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.dan.timewebclone.providers.ImageProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private FloatingActionButton mFabSelectImage;
    private BottomSheetSelectImage mBottonSelectedImage;
    private BottomSheetUserName bottomSheetUserName;
    private BottomSheetPhone bottomSheetPhone;
    private BottomSheetCompany bottomSheetCompany;
    private TextView mUserName, mCompany, mPhone, mEmail;
    private CircleImageView circleImageProfile;
    private ImageView imageViewEditUserName, imageViewEditCompany,imageViewEditPhone, mImageBack;

    EmployeeProvider employeeProvider;
    AuthProvider authProvider;
    ImageProvider mImageProvider;
    Employee employee;

    String eName, eCompany, ePhone, eEmail, eImage, idUser;
    String imagetoBase64;

    Options mOptions;
    ArrayList<String> returnValues = new ArrayList<>();
    File mImageFile;
    ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setStatusBarColor();

        //MyToolBar.sho0 w(this,"Perfil", true);
        mImageProvider = new ImageProvider();
        employeeProvider = new EmployeeProvider();
        //authP = FirebaseAuth.getInstance();
        authProvider = new AuthProvider();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if (Build.VERSION.SDK_INT > 23) {
            builder.detectFileUriExposure();
        }

        mUserName = findViewById(R.id.textViewUserName);
        imageViewEditUserName = findViewById(R.id.imageEditUserName);
        mCompany = findViewById(R.id.textViewCompany);
        imageViewEditCompany = findViewById(R.id.imageEditCompany);
        mPhone = findViewById(R.id.textViewPhone);
        imageViewEditPhone= findViewById(R.id.imageEditPhone);
        mEmail = findViewById(R.id.textViewEmail);
        circleImageProfile = findViewById(R.id.circleImageProfile);
        mFabSelectImage = findViewById(R.id.fabSelectImage);

        mImageBack = findViewById(R.id.circleImageBack);

        mOptions = Options.init()
                .setRequestCode(100)                                           //Request code for activity results
                .setCount(1)                                                   //Number of images to restict selection count
                .setFrontfacing(false)                                         //Front Facing camera on start
                .setPreSelectedUrls(returnValues)                               //Pre selected Image Urls
                .setMode(Options.Mode.Picture)               //Option to exclude videos
                .setVideoDurationLimitinSeconds(0)                            //Duration for video recording
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                .setPath("/pix/images");


        mFabSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBottonSheetSelectImage();
            }
        });

        imageViewEditUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBottonSheetUserName();
            }
        });

        /*imageViewEditCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBottonSheetEditCompany();
            }
        });*/

        imageViewEditPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBottonSheetEditPhone();
            }
        });

        mImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        getUserInfo();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listenerRegistration != null)
            listenerRegistration.remove();
    }

    public void getUserInfo() {
        DbEmployees dbEmployees = new DbEmployees(ProfileActivity.this);
        employee = dbEmployees.getEmployee(authProvider.getId());
        if(employee!=null){
            mUserName.setText(employee.getName());
            mPhone.setText(employee.getPhone());
            mCompany.setText(employee.getCompany());
            mEmail.setText(employee.getEmail());
            if(employee.getImage() != null){
                if(!employee.getImage().equals("")){
                    try {
                        byte[] decodedString = Base64.decode(employee.getImage(), Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        circleImageProfile.setImageBitmap(decodedByte);
                    } catch (Exception e){
                        Bitmap decodedByte = BitmapFactory.decodeFile(employee.getImage());
                        circleImageProfile.setImageBitmap(decodedByte);
                    }
                }
                else{
                    setImageDefault();
                }
            }
            else{
                setImageDefault();
            }
        }

/*
        listenerRegistration = employeeProvider.getUserInfo(authProvider.getId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if(documentSnapshot!= null){
                    if(documentSnapshot.exists()){
                        employee = documentSnapshot.toObject(Employee.class);
                        mUserName.setText(employee.getName());
                        mPhone.setText(employee.getPhone());
                        mCompany.setText(employee.getCompany());
                        mEmail.setText(employee.getEmail());
                        if(employee.getImage() != null){
                            if(!employee.getImage().equals("")){
                                Picasso.with(ProfileActivity.this).load(employee.getImage()).into(circleImageProfile);
                            }
                            else{
                                setImageDefault();
                            }
                        }
                        else{
                            setImageDefault();
                        }
                    }
                }
            }
        });*/
    }

    private void openBottonSheetEditCompany() {
        if(employee != null ){
            bottomSheetCompany = BottomSheetCompany.newInstance(employee.getCompany(),ProfileActivity.this);
            bottomSheetCompany.show(getSupportFragmentManager(),bottomSheetCompany.getTag());
        } else {
            Toast.makeText(this, "La informacion no se pudo cargar", Toast.LENGTH_SHORT).show();
        }
    }

    private void openBottonSheetEditPhone() {
        if(employee != null){
            bottomSheetPhone = BottomSheetPhone.newInstance(employee.getPhone(), this);
            bottomSheetPhone.show(getSupportFragmentManager(),bottomSheetPhone.getTag());
        } else {
            Toast.makeText(this, "La informacion no se pudo cargar", Toast.LENGTH_SHORT).show();
        }
    }

    private void openBottonSheetSelectImage() {
        if(employee != null){
           mBottonSelectedImage = BottomSheetSelectImage.newInstance(employee.getImage(), ProfileActivity.this);
           mBottonSelectedImage.show(getSupportFragmentManager(), mBottonSelectedImage.getTag());
        } else {
            Toast.makeText(this, "La informacion no se pudo cargar", Toast.LENGTH_SHORT).show();
        }
    }

    private void openBottonSheetUserName() {
        if(employee != null){
            bottomSheetUserName = BottomSheetUserName.newInstance(employee.getName(), this);
            bottomSheetUserName.show(getSupportFragmentManager(),bottomSheetUserName.getTag());
        } else {
            Toast.makeText(this, "La informacion no se pudo cargar", Toast.LENGTH_SHORT).show();
        }
    }

    public void setImageDefault(){
        circleImageProfile.setImageResource(R.drawable.icon_user2);
    }

    public void startPix() {
        Pix.start(ProfileActivity.this, mOptions);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Clear the Activity's bundle of the subsidiary fragments' bundles.
        outState.clear();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            returnValues = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            mImageFile = new File(returnValues.get(0));
            Bitmap mImage = BitmapFactory.decodeFile(mImageFile.getAbsolutePath());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mImage.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] image = baos.toByteArray();
            imagetoBase64 = Base64.encodeToString(image,Base64.DEFAULT);
            DbEmployees dbEmployees = new DbEmployees(ProfileActivity.this);
            dbEmployees.saveImage(authProvider.getId(),imagetoBase64);
            circleImageProfile.setImageBitmap(mImage);
            saveImage(imagetoBase64);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "No tiene permisos" , Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS}, 0);
                    } else
                    {
                        Pix.start(ProfileActivity.this, mOptions);
                    }

                } else {
                    Toast.makeText(ProfileActivity.this, "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }

    }

    private void saveImage(String mImageFile) {
      employeeProvider.updateImage(authProvider.getId(), mImageFile).addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
         if(task!=null){
             if(task.isSuccessful()){
                 mBottonSelectedImage.dismiss();
                 Toast.makeText(ProfileActivity.this, "La foto de perfil se actualizo correctamente", Toast.LENGTH_SHORT).show();
             } else {
                 Toast.makeText(ProfileActivity.this, "La foto de perfil no logro almacenarse correctamente", Toast.LENGTH_SHORT).show();
             }
         }
          }
      });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black, this.getTheme()));
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
    }
}