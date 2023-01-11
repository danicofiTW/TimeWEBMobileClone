package com.dan.timewebclone.adapters;

import static com.dan.timewebclone.fragments.MapFragment.circleImageViewMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dan.timewebclone.R;
import com.dan.timewebclone.activitys.ShowImageActivity;
import com.dan.timewebclone.activitys.ShowLocationActivity;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.ChecksProvider;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.dan.timewebclone.utils.RelativeTime;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.ListenerRegistration;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ChecksAdapter extends FirestoreRecyclerAdapter<Check, ChecksAdapter.viewHolder> {

    Context context;
    //private final FirebaseAuth authP;
    private AuthProvider authP;
    EmployeeProvider employeeProvider;
    ChecksProvider checksProvider;
    Employee employee;

    public ArrayList<viewHolder> viewHolders;
    public ArrayList<Check> checks;

    public ChecksAdapter(FirestoreRecyclerOptions options, Context context) {
        super(options);

        this.context = context;
        authP = new AuthProvider();
        employeeProvider = new EmployeeProvider();
        checksProvider = new ChecksProvider();
        employee = new Employee();
        viewHolders = new ArrayList<>();
        checks = new ArrayList<>();

    }

    @Override
    protected void onBindViewHolder(@NonNull ChecksAdapter.viewHolder holder, int position, @NonNull Check check) {
       //holder.textViewFecha.setText(RelativeTime.timeFormatAMPM(check.getTime());

            holder.myView.setVisibility(View.VISIBLE);
            if(check.getStatusSend() == 1){
                holder.imageViewSendCheck.setImageResource(R.drawable.icon_double_check);
            } else if(check.getStatusSend() == 2){
                holder.imageViewSendCheck.setImageResource(R.drawable.ic_check_gray);
            } else if(check.getStatusSend() == 0){
                holder.imageViewSendCheck.setImageResource(R.drawable.ic_cancel_red);
            }


            if(check.getTipeCheck().equals("startWork")){
                holder.imageViewHistoryCheck.setImageResource(R.drawable.icon_check_green);
                holder.textViewTipeCheck.setText("Registro de Entrada");
                holder.textViewTipeCheck.setTextColor(context.getColor(R.color.colorGreen));
            } else if(check.getTipeCheck().equals("startEating")){
                holder.imageViewHistoryCheck.setImageResource(R.drawable.icon_check_blue);
                holder.textViewTipeCheck.setText("Registro de Comida");
                holder.textViewTipeCheck.setTextColor(Color.BLUE);
            } else if(check.getTipeCheck().equals("finishEating")){
                holder.imageViewHistoryCheck.setImageResource(R.drawable.icon_check_yellow);
                holder.textViewTipeCheck.setText("Registro de Fin Comida");
                holder.textViewTipeCheck.setTextColor(Color.YELLOW);
            } else if(check.getTipeCheck().equals("finishWork")){
                holder.imageViewHistoryCheck.setImageResource(R.drawable.icon_check_red);
                holder.textViewTipeCheck.setText("Registro de Salida");
                holder.textViewTipeCheck.setTextColor(Color.RED);
            }

        /*if(checks.get(position).getImage() != null){
            try{
                byte[] byteArray = checks.get(position).getImage().getBytes();
                Bitmap  bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                holder.imageViewHistoryCheck.setImageBitmap(bitmap);
                openImage(holder, bitmap);
            }
            catch(Exception e){
                e.getMessage();
            }
        } else {
            defaultImage(holder, checks.get(position));
        }*/


        try {
            Geocoder geocoder = new Geocoder(context);
            List<Address> addressList = geocoder.getFromLocation(check.getCheckLat(), check.getCheckLong(), 1);
            String city = addressList.get(0).getLocality();
            String country = addressList.get(0).getCountryName();
            String address = addressList.get(0).getAddressLine(0);
            holder.textViewGeocerca.setText(address + " " + city);
            openLocation(holder, check);

        } catch (IOException e) {
            Log.d("Error:", "Mensaje de error: " + e.getMessage());
        }



            /*holder.imageViewSendCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checksProvider.updateSendCheck(check.getIdCheck()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            holder.imageViewSendCheck.setImageResource(R.drawable.icon_double_check);
                            check.setStatusSend(true);
                        }
                    });
                }
            });*/

            checks.add(check);
            viewHolders.add(holder);
            int i = viewHolders.size();
    }

    public void clearView() {
        for(int i = 0; i<viewHolders.size(); i++){
            viewHolders.get(i).myView.setVisibility(View.GONE);
        }
    }


    private void openLocation(viewHolder holder, Check check) {
        holder.linearLayoutOpenLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, ShowLocationActivity.class);
                i.putExtra("lat", check.getCheckLat()+"");
                i.putExtra("lng", check.getCheckLong()+"");
                context.startActivity(i);
            }
        });
    }




    private void defaultImage(viewHolder holder, Check check){
        if(check.getTipeCheck().equals("startWork")){
            holder.imageViewHistoryCheck.setImageResource(R.drawable.icon_check_green);
        } else if(check.getTipeCheck().equals("startEating")){
            holder.imageViewHistoryCheck.setImageResource(R.drawable.icon_check_blue);
        } else if(check.getTipeCheck().equals("finishEating")){
            holder.imageViewHistoryCheck.setImageResource(R.drawable.icon_check_yellow);
        } else if(check.getTipeCheck().equals("finishWork")){
            holder.imageViewHistoryCheck.setImageResource(R.drawable.icon_check_red);
        }

    }

    private void openImage(viewHolder holder, Bitmap image) {
        holder.imageViewHistoryCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, ShowImageActivity.class);
                i.putExtra("image", image);
                context.startActivity(i);
            }
        });
    }

    @NonNull
    @Override
    public ChecksAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_history_checks, parent, false);
        return new viewHolder(view);
    }




    public class viewHolder extends RecyclerView.ViewHolder {

        public boolean isDelete;
        TextView textViewGeocerca, textViewTipeCheck, textViewFecha;
        ImageView imageViewSendCheck, imageViewHistoryCheck;
        LinearLayout linearLayoutOpenLocation;
        View myView;


        public viewHolder(View view){
            super(view);
            myView = view;
            linearLayoutOpenLocation = view.findViewById(R.id.linearLayoutInfo);
            textViewGeocerca = view.findViewById(R.id.textViewGeocerca);
            textViewTipeCheck = view.findViewById(R.id.textViewTipeCheck);
            textViewFecha = view.findViewById(R.id.textViewFecha);
            imageViewSendCheck = view.findViewById(R.id.imageViewSendCheck);
            imageViewHistoryCheck = view.findViewById(R.id.imageViewHistoryCheck);
        }
    }
}
