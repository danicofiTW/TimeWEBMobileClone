package com.dan.timewebclone.activitys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
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
import com.dan.timewebclone.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.ListenerRegistration;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ProfileActivity extends AppCompatActivity {

    private FloatingActionButton mFabSelectImage;
    private BottomSheetSelectImage mBottonSelectedImage;
    private BottomSheetUserName bottomSheetUserName;
    private BottomSheetPhone bottomSheetPhone;
    private BottomSheetCompany bottomSheetCompany;
    private TextView mUserName, mCompany, mPhone, mEmail, mToken;
    private CircleImageView circleImageProfile;
    private ImageView imageViewEditUserName,imageViewEditPhone, mImageBack;

    private EmployeeProvider employeeProvider;
    private AuthProvider authProvider;
    private ImageProvider mImageProvider;
    private Employee employee;
    private DbEmployees dbEmployees;

    private String imagetoBase64;

    private Options mOptions;
    private ArrayList<String> returnValues = new ArrayList<>();
    private File mImageFile;
    //private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setStatusBarColor();

        mImageProvider = new ImageProvider();
        employeeProvider = new EmployeeProvider();
        authProvider = new AuthProvider();
        dbEmployees = new DbEmployees(ProfileActivity.this);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if (Build.VERSION.SDK_INT > 23) {
            builder.detectFileUriExposure();
        }

        mUserName = findViewById(R.id.textViewUserName);
        imageViewEditUserName = findViewById(R.id.imageEditUserName);
        mCompany = findViewById(R.id.textViewCompany);
        mPhone = findViewById(R.id.textViewPhone);
        imageViewEditPhone= findViewById(R.id.imageEditPhone);
        mEmail = findViewById(R.id.textViewEmail);
        mToken= findViewById(R.id.textViewToken);
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

    //Obtener informacion del usuario
    public void getUserInfo() {
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
            if(employee.getToken() != null){
                mToken.setText(employee.getToken());
                mToken.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        String text = mToken.getText().toString();
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("text",  text);
                        clipboard.setPrimaryClip(clip);
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                            Toast.makeText(ProfileActivity.this, "Texto copiado", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                });
            }
        }
    }
    //Editar telefono
    private void openBottonSheetEditPhone() {
        if(employee != null){
            bottomSheetPhone = BottomSheetPhone.newInstance(employee.getPhone(), this);
            bottomSheetPhone.show(getSupportFragmentManager(),bottomSheetPhone.getTag());
        } else {
            Toast.makeText(this, "La informacion no se pudo cargar", Toast.LENGTH_SHORT).show();
        }
    }

    //Editar nombre
    private void openBottonSheetUserName() {
        if(employee != null){
            bottomSheetUserName = BottomSheetUserName.newInstance(employee.getName(), this);
            bottomSheetUserName.show(getSupportFragmentManager(),bottomSheetUserName.getTag());
        } else {
            Toast.makeText(this, "La informacion no se pudo cargar", Toast.LENGTH_SHORT).show();
        }
    }

    //Cambiar imagen
    private void openBottonSheetSelectImage() {
        if(employee != null){
            mBottonSelectedImage = BottomSheetSelectImage.newInstance(employee.getImage(), ProfileActivity.this);
            mBottonSelectedImage.show(getSupportFragmentManager(), mBottonSelectedImage.getTag());
        } else {
            Toast.makeText(this, "La informacion no se pudo cargar", Toast.LENGTH_SHORT).show();
        }
    }

    //Imagen por defecto
    public void setImageDefault(){
        circleImageProfile.setImageResource(R.drawable.icon_user2);
    }

    //Abrir camara y galeria
    public void startPix() {
        Pix.start(ProfileActivity.this, mOptions);
    }

    //Solicitar permisos de camara y storage
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

    //Recibir la imagen
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            returnValues = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            mImageFile = new File(returnValues.get(0));
            Bitmap mImage = BitmapFactory.decodeFile(mImageFile.getAbsolutePath());
            Bitmap mImageReview = reviewOrientationImage(mImage);

            try {
                mImageReview = new Compressor(this)
                        .setMaxWidth(500)
                        .setMaxHeight(500)
                        .setQuality(85)
                        .compressToBitmap(mImageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mImageReview.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            byte[] image = baos.toByteArray();
            imagetoBase64 = Base64.encodeToString(image,Base64.DEFAULT);
            saveImage(imagetoBase64, mImageReview);
        }
    }

    //Revisar orientacion de la imagen o foto
    private Bitmap reviewOrientationImage(Bitmap compressImage) {
        ExifInterface ei = null;
        Bitmap rotatedBitmap = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                InputStream fi = getContentResolver().openInputStream(Uri.fromFile(mImageFile));
                ei = new ExifInterface(fi);
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
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
            } catch (IOException e) {
                rotatedBitmap = compressImage;
                e.printStackTrace();
            }
        } else {
            rotatedBitmap = compressImage;
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

    //Guardar imagen de perfil en firebase y base de datos
    private void saveImage(String imageB64, Bitmap imageBitmap) {
        employeeProvider.updateImage(authProvider.getId(), imageB64).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if(mBottonSelectedImage != null && mBottonSelectedImage.isVisible()){
                    mBottonSelectedImage.dismiss();
                }
                if(circleImageProfile != null && circleImageProfile.getVisibility() == View.VISIBLE){
                    circleImageProfile.setImageBitmap(imageBitmap);
                }
                dbEmployees.saveImage(authProvider.getId(),imageB64);
                Toast.makeText(ProfileActivity.this, "La foto de perfil se actualizo correctamente", Toast.LENGTH_SHORT).show();
            }
        });
      if(!Utils.isOnlineNet(ProfileActivity.this)){
          if(mBottonSelectedImage != null && mBottonSelectedImage.isVisible()){
              mBottonSelectedImage.dismiss();
          }
          Toast.makeText(ProfileActivity.this, "Conectate a internet para actualizar correctamente tu forto de perfil", Toast.LENGTH_SHORT).show();
      }
    }

    //Se utiliza para evitar problemas con los fragment
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    //Destruir listener
    @Override
    protected void onDestroy() {
        super.onDestroy();
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