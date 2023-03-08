package com.dan.timewebclone.adapters;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.RenderMode;
import com.dan.timewebclone.R;
import com.dan.timewebclone.activitys.GeocercasActivity;
import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.activitys.MainActivity;
import com.dan.timewebclone.activitys.ShowLocationActivity;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.db.DbGeocercas;
import com.dan.timewebclone.fragments.HistoryChecksSendOkFragment;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.models.Geocerca;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.ChecksProvider;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.dan.timewebclone.utils.RelativeTime;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class GeocercasDbAdapter extends RecyclerView.Adapter<GeocercasDbAdapter.GeocercaViewHolder> {

    GeocercasActivity context;
    //private final FirebaseAuth authP;
    private AuthProvider authProvider;
    public ArrayList<Geocerca> geocercas;

    private SimpleDateFormat sdfDate;
    private DbGeocercas dbGeocercas;
    private AlertDialog.Builder builderDialogGeocerca;




    public GeocercasDbAdapter(ArrayList<Geocerca> listGeocerca, GeocercasActivity context) {
        this.context = context;
        geocercas = listGeocerca;
        builderDialogGeocerca = new AlertDialog.Builder(context);
        authProvider = new AuthProvider();
    }


    @Override
    public GeocercasDbAdapter.GeocercaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_geocerca, parent, false);
        return new GeocercasDbAdapter.GeocercaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GeocercaViewHolder holder, int position) {
        holder.textViewAdrressGeocerca.setText(geocercas.get(position).getDireccion());
        holder.textViewNameGeocerca.setText(geocercas.get(position).getGeoNombre());
        
        selectGeofencing(holder, geocercas.get(position));
    }

    private void selectGeofencing(GeocercasDbAdapter.GeocercaViewHolder holder, Geocerca geocerca) {
        holder.myView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.lottieAnimationViewGeo.addAnimatorListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {}
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        context.goToHome(geocerca.getGeoLat(), geocerca.getGeoLong(), geocerca.getRadio(), geocerca.getIdGeocerca());
                        holder.lottieAnimationViewGeo.removeAllAnimatorListeners();
                    }
                    @Override
                    public void onAnimationCancel(Animator animator) {}
                    @Override
                    public void onAnimationRepeat(Animator animator) {}
                });
                //LatLng latLng = new LatLng(geocerca.getGeoLat(), geocerca.getGeoLong());
                holder.lottieAnimationViewGeo.playAnimation();
                
            }
        });
    }

    public void updateGeocercas(ArrayList<Geocerca> geocercas) {
        Collections.sort(geocercas, new SortByDate());
        this.geocercas = geocercas;
        notifyDataSetChanged();
    }

    public void filtrar(ArrayList<Geocerca> geocercas) {
        this.geocercas = geocercas;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return geocercas.size();
    }

    public class GeocercaViewHolder extends RecyclerView.ViewHolder{
        TextView textViewAdrressGeocerca, textViewNameGeocerca;
        View myView;
        LottieAnimationView lottieAnimationViewGeo;


        public GeocercaViewHolder(View view){
            super(view);
            myView = view;
            textViewNameGeocerca = view.findViewById(R.id.textViewGeoName);
            textViewAdrressGeocerca = view.findViewById(R.id.textViewDireccion);
            lottieAnimationViewGeo = view.findViewById(R.id.animationGeo);
            lottieAnimationViewGeo.isHardwareAccelerated();
            lottieAnimationViewGeo.setRenderMode(RenderMode.HARDWARE);
        }
    }

    static class SortByDate implements Comparator<Geocerca> {

        @Override
        public int compare(Geocerca a, Geocerca b) {

            SimpleDateFormat sdfLongDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date aux1 = new Date(a.getAlta());
            Date aux2 = new Date(b.getAlta());
            String date1 = sdfLongDate.format(aux1);
            String date2 = sdfLongDate.format(aux2);

            return date1.compareTo(date2);
        }
    }
}
