package com.dan.timewebclone.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dan.timewebclone.R;
import com.dan.timewebclone.activitys.ProfileActivity;
import com.dan.timewebclone.db.DbEmployees;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.dan.timewebclone.providers.ImageProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetSelectImage extends BottomSheetDialogFragment {

    LinearLayout deleteImage, selectImage;
    ImageProvider mImageProvider;
    EmployeeProvider employeeProvider;
    //FirebaseAuth mAuth;
    AuthProvider mAuth;

    private static Context myContext;
    String image;

    public static BottomSheetSelectImage newInstance(String url, Context context){
        BottomSheetSelectImage bottomSheetSelectImage = new BottomSheetSelectImage();
        Bundle args = new Bundle();
        args.putString("image", url);
        myContext = context;
        bottomSheetSelectImage.setArguments(args);
        return bottomSheetSelectImage;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        image = getArguments().getString("image");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_select_image, container, false);
        deleteImage = view.findViewById(R.id.layoutDeleteImage);
        selectImage = view.findViewById(R.id.layoutSelecImage);

        mImageProvider = new ImageProvider();
        employeeProvider = new EmployeeProvider();
        //mAuth = FirebaseAuth.getInstance();
        mAuth = new AuthProvider();

        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteImage();
            }
        });

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateImage();
            }
        });


        return view;
    }

    private void updateImage() {
        ((ProfileActivity)getActivity()).startPix();
    }

    private void deleteImage() {
        if(!image.equals("")){
                    employeeProvider.updateImage(mAuth.getId(), null).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task2) {
                            if(task2.isSuccessful()){
                                ((ProfileActivity)getActivity()).setImageDefault();
                                DbEmployees dbEmployees = new DbEmployees(myContext);
                                dbEmployees.saveImage(mAuth.getId(), "");
                                dismiss();
                                Toast.makeText(getContext(), "La imagen se elimino correctamente", Toast.LENGTH_LONG).show();
                            } else {
                                dismiss();
                                Toast.makeText(getContext(), "No se pudo eliminar la imagen", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
            dismiss();
            Toast.makeText(getContext(), "No cuentas con foto de perfil", Toast.LENGTH_LONG).show();
        }
    }
}