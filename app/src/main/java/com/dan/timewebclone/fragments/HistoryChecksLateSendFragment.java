package com.dan.timewebclone.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.dan.timewebclone.R;
import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.adapters.ChecksDbAdapterLateSend;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.ChecksProvider;
import com.dan.timewebclone.utils.RelativeTime;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class HistoryChecksLateSendFragment extends Fragment {

    private View mView;
    private RecyclerView mReciclerView;
    public ChecksDbAdapterLateSend checksDbAdapterLateSend;

    //private CheckBox checkboxSendSuccesful, checkboxSendLate, checkboxSendAdd;

    private AuthProvider authProvider;
    private ChecksProvider checksProvider;
    private DbChecks dbChecks;

    private HomeTW myContext;

    private LinearLayoutManager linearLayoutManager;
    public CheckBox deleteAllChecks;
    public ImageView deleteChecks, imageViewCancelDelete;
    private LottieAnimationView animation;
    private TextView textViewNumberChecksDelete, textViewTitleHistory, textViewNoChecks;
    private FrameLayout frameLayoutNumberChecksDelete;
    private LinearLayout linearLayoutDeleteChecks;

    private ArrayList<Check> listChecks;

    private ArrayList<String> listChecksDelete;
    private List<Integer> statusSend;
    private RelativeTime relativeTime;

    public HistoryChecksLateSendFragment() {
    }

    public static HistoryChecksLateSendFragment newInstance(String param1, String param2) {
        HistoryChecksLateSendFragment fragment = new HistoryChecksLateSendFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_historial_sendlate, container, false);

        mReciclerView = mView.findViewById(R.id.rvChecksLateSend);
        deleteChecks = mView.findViewById(R.id.imageViewDeleteChecks);
        animation = mView.findViewById(R.id.animation);
        deleteAllChecks = mView.findViewById(R.id.checkboxDeleteAll);
        textViewNumberChecksDelete = mView.findViewById(R.id.textViewNumberChecksDelete);
        frameLayoutNumberChecksDelete = mView.findViewById(R.id.frameLayoutNumberChecksDelete);
        linearLayoutDeleteChecks = mView.findViewById(R.id.linearLayoutDeleteHistory);
        textViewTitleHistory = mView.findViewById(R.id.textViewTitleHistoryChecks);
        imageViewCancelDelete = mView.findViewById(R.id.imageViewCancelDelete);
        textViewNoChecks = mView.findViewById(R.id.textViewNoChecks);
        linearLayoutManager = new LinearLayoutManager(myContext);
        mReciclerView.setLayoutManager(linearLayoutManager);
        mReciclerView.setHasFixedSize(true);
        authProvider = new AuthProvider();
        checksProvider = new ChecksProvider();
        dbChecks = new DbChecks(myContext);
        relativeTime = new RelativeTime();
        listChecks = new ArrayList<>();
        statusSend = Arrays.asList(0,2);

        getChecksById();
        //checksDbAdapterLateSend.updateChecks(dbChecks.getChecksNotSendSucces(authProvider.getId()));
        //updateChecks();
        if(checksDbAdapterLateSend.checks.size() != 0){
            textViewNoChecks.setVisibility(View.GONE);
        }
        //Actualizar los checks a eliminar

        return mView;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        myContext = (HomeTW) context;
        super.onAttach(context);
    }


    public void notifyChangeAdapter() {
       // checksDbAdapter.notifyDataSetChanged();
        checksDbAdapterLateSend.updateChecks(dbChecks.getChecksNotSendSucces(authProvider.getId()));
        mReciclerView.scrollToPosition(0);
    }

    public void getChecksById() {
        if(checksProvider!=null){
        if(myContext!=null){
            ArrayList<Check> checks = dbChecks.getChecksNotSendSucces(authProvider.getId());
            checksDbAdapterLateSend = new ChecksDbAdapterLateSend(checks, myContext);
            mReciclerView.setAdapter(checksDbAdapterLateSend);
            checksDbAdapterLateSend.notifyDataSetChanged();

            checksDbAdapterLateSend.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    if(myContext.idChecksLateDelete.size() != 0){
                        showImageDelete(true);
                    } else {
                        showImageDelete(false);
                    }

                    if(checksDbAdapterLateSend.checks.size() == 0){
                        textViewNoChecks.setVisibility(View.VISIBLE);
                    } else {
                        textViewNoChecks.setVisibility(View.GONE);
                    }
                    }
                });
            }
        }
    }

    public void showImageDelete(boolean visible){
        if(visible){
            animation.setVisibility(View.GONE);
            textViewTitleHistory.setVisibility(View.GONE);
            deleteChecks.setVisibility(View.VISIBLE);
            linearLayoutDeleteChecks.setVisibility(View.VISIBLE);
            /*deleteAllChecks.setVisibility(View.VISIBLE);
            frameLayoutNumberChecksDelete.setVisibility(View.VISIBLE);*/
            textViewNumberChecksDelete.setText(""+myContext.idChecksLateDelete.size());
            deleteChecks();
            allChecks();
            cancelDeleteChecks();
        } else {
            animation.setVisibility(View.VISIBLE);
            textViewTitleHistory.setVisibility(View.VISIBLE);
            deleteChecks.setVisibility(View.GONE);
            linearLayoutDeleteChecks.setVisibility(View.GONE);
            /*deleteAllChecks.setVisibility(View.GONE);
            frameLayoutNumberChecksDelete.setVisibility(View.GONE);*/
        }
    }

    private void cancelDeleteChecks() {
        imageViewCancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               updateDelete();
            }
        });
    }

    public void updateDelete() {
        myContext.idChecksLateDelete.clear();
        dbChecks.updateChecksDelete(false, authProvider.getId(), false);
        showImageDelete(false);
        if(deleteAllChecks.isChecked()){
            deleteAllChecks.setChecked(false);
        }
        notifyChangeAdapter();
    }

    private void allChecks() {
        deleteAllChecks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ArrayList<Check> checks = dbChecks.getChecksNotSendSucces(statusSend);
                if(deleteAllChecks.isChecked()){
                    ArrayList<Check> checksD= dbChecks.getChecksNotSendSucces(authProvider.getId());
                    myContext.idChecksLateDelete.clear();
                    for(int i = 0; i < checksD.size(); i++){
                        myContext.idChecksLateDelete.add(checksD.get(i).getIdCheck());
                    }
                    dbChecks.updateChecksDelete(true, authProvider.getId(), false);
                    notifyChangeAdapter();
                } else {
                    myContext.idChecksLateDelete.clear();
                    dbChecks.updateChecksDelete(false, authProvider.getId(), false);
                    notifyChangeAdapter();
                    showImageDelete(true);
                }
            }
        });
    }

    private void deleteChecks() {

        deleteChecks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myContext.idChecksLateDelete.size() != 0) {
                    String message;
                    if (deleteAllChecks.isChecked()) {
                        message = "¿Deseas eliminar todos los registros pendientes?";
                    } else {
                        message = "¿Deseas eliminar los " + myContext.idChecksLateDelete.size() + " registros seleccionados?";
                    }
                    new AlertDialog.Builder(myContext)
                            .setTitle("ELIMINAR REGISTROS")
                            .setMessage(message)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    boolean delete = dbChecks.delete(myContext.idChecksLateDelete);
                                    if (delete) {
                                        myContext.idChecksLateDelete.clear();
                                        notifyChangeAdapter();
                                        showImageDelete(false);
                                        if (myContext.menuItemSearch.isActionViewExpanded()) {
                                            myContext.toolbar.collapseActionView();
                                        }
                                        Toast.makeText(myContext, "Los registros se eliminaron correctamente", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(myContext, "Error al eliminar", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            }).create().show();
                } else {
                    showImageDelete(false);
                }
            }
        });
    }

    /*public void reviewData(ArrayList<Check> listChecksSendOk, ArrayList<String> idDeleteChecksOk) {
     if(checksProvider!=null) {
         if (myContext != null) {
                 checksProvider.getChecksByUserAndStatusSend(authProvider.getId(), 2).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                     @Override
                     public void onSuccess(QuerySnapshot querySnapshot) {
                         Check check;
                         ArrayList<String> idDeleteChecks = new ArrayList<>();
                         for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                             check = document.toObject(Check.class);
                             if(check!=null){
                                 int dias = 31;
                                 if(check.getTime() != null){
                                     dias = relativeTime.compareToDate(check.getTime());
                                 }
                                 if(dias > 30){
                                     idDeleteChecks.add(check.getIdCheck());
                                     checksProvider.deleteCheck(check);
                                    //listChecksDelete.add(check.getIdCheck());
                                 } else{
                                    listChecks.add(check);
                                 }
                             }
                         }

                         if(idDeleteChecks.size() != 0 || idDeleteChecksOk.size() != 0){
                             if(idDeleteChecksOk.size() != 0){
                                 dbChecks.delete(idDeleteChecksOk);
                             }
                             if(idDeleteChecks.size() != 0) {
                                 dbChecks.delete(idDeleteChecks);
                             }
                             Toast.makeText(myContext, "Se eliminaron algunos checks con más de 30 días", Toast.LENGTH_SHORT).show();
                         }

                         Collections.reverse(listChecks);
                         if(myContext.linearLayoutLoadingHome.getVisibility() == View.VISIBLE) {
                             myContext.linearLayoutLoadingHome.setVisibility(View.GONE);
                         }

                         if(myContext.pdRevieData.isShowing()){
                             myContext.pdRevieData.dismiss();
                         }

                         if(dbChecks.getChecksNotSendSucces(authProvider.getId()).size() == 0  && listChecks.size() != 0){
                             myContext.mostrarUpdateChecks(listChecks, listChecksSendOk);
                         } else if(listChecksSendOk.size() != 0 && dbChecks.getChecksSendSucces(authProvider.getId()).size() == 0){
                             myContext.mostrarUpdateChecks(listChecks, listChecksSendOk);
                         }

                     }
                 });
         } else {

             myContext.pdRevieData.dismiss();
         }
     }
     }*/

    public void updateChecks(int tipeCheck) {
        ChecksProvider checksProvider = new ChecksProvider();
        if(authProvider!=null){
            checksProvider.getChecksNotSend(authProvider.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if(queryDocumentSnapshots.getDocuments().size()!=0){
                        Check check;
                        int i = 0;
                        String timezoneID = TimeZone.getDefault().getID();
                        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timezoneID), Locale.getDefault());
                        Date time2 = calendar.getTime();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            check = document.toObject(Check.class);
                            i++;
                            checksProvider.updateStatus(check.getIdCheck(), tipeCheck, time2.getTime());
                        }
                        if(myContext.numberChecksSendLate != 0 && myContext.numberChecksSendLate != i){
                            i=i+myContext.numberChecksSendLate;
                        }
                        if (i == 1) {
                                myContext.updateChecksNotSend = false;
                                Toast.makeText(myContext, "Registro enviado", Toast.LENGTH_SHORT).show();
                        } else {
                                myContext.updateChecksNotSend = false;
                                Toast.makeText(myContext, "Se enviaron " + i + " registros", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        myContext.updateChecksNotSend = false;
                    }
                }
            });
        }
    }

    @Override
    public void onStop() {
        dbChecks.updateChecksDelete(false, authProvider.getId(), false);
        myContext.idChecksLateDelete.clear();
        checksDbAdapterLateSend.notifyDataSetChanged();
        super.onStop();
    }



    /*public  int compareToDate(Long dateCheck) {
        int dias = 0;
        if(dateCheck!=null){
            Date fechaInicial = new Date(dateCheck);
            Long n = new Date().getTime();
            Date fechaFinal = new Date(n);
            dias = (int) ((fechaFinal.getTime() - fechaInicial.getTime()) / 86400000);
        }
        return dias;
    }*/

    public void filterSendLate(ArrayList<Check> checks){
        checksDbAdapterLateSend.filtrar(checks);
    }


}