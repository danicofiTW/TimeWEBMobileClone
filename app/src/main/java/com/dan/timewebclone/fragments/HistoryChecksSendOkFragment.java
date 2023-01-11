package com.dan.timewebclone.fragments;

import static com.google.firebase.firestore.FieldValue.delete;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.dan.timewebclone.R;
import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.adapters.ChecksAdapter;
import com.dan.timewebclone.adapters.ChecksDbAdapter;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.ChecksProvider;
import com.dan.timewebclone.utils.RelativeTime;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class HistoryChecksSendOkFragment extends Fragment {

    private RecyclerView mReciclerView;
    private ImageView deleteChecks;
    private LottieAnimationView animation;
    public CheckBox deleteAllChecks;

    //private ChecksAdapter mAdapter;
    private AuthProvider authProvider;
    private ChecksProvider checksProvider;
    private ChecksDbAdapter checksDbAdapter;
    private RelativeTime relativeTime;
    AlertDialog.Builder builderDialogUpdateChecks;
    HomeTW myContext;
    LinearLayoutManager linearLayoutManager;
    //public int numberChecksNotSend;
    Check ch;
    public ArrayList<Check> listChecks;

    public HistoryChecksSendOkFragment() {
        // Required empty public constructor
    }


    public static HistoryChecksSendOkFragment newInstance(String param1, String param2) {
        HistoryChecksSendOkFragment fragment = new HistoryChecksSendOkFragment();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_historial, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*numberChecksNotSend = 0;
        SharedPreferences preferences = myContext.getSharedPreferences("checksNotSend", Context.MODE_PRIVATE);
        numberChecksNotSend = preferences.getInt("numberChecksNotSend",0);*/

        mReciclerView = view.findViewById(R.id.rvChecks);
        deleteChecks = view.findViewById(R.id.imageViewDeleteChecks);
        animation = view.findViewById(R.id.animation);
        deleteAllChecks = view.findViewById(R.id.checkboxDeleteAll);
        linearLayoutManager = new LinearLayoutManager(myContext);
        mReciclerView.setLayoutManager(linearLayoutManager);
        builderDialogUpdateChecks = new AlertDialog.Builder(myContext);
        authProvider = new AuthProvider();
        checksProvider = new ChecksProvider();
        relativeTime = new RelativeTime();
        listChecks = new ArrayList<>();
        ch=null;
        //deleteChecks.setVisibility(View.VISIBLE);
        getChecksById();
        //updateStatusChecks();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        myContext = (HomeTW) context;
        super.onAttach(context);
    }

    public void notifyInsertCheck() {
        if(checksDbAdapter!=null){
            checksDbAdapter.notifyDataSetChanged();
        }
    }

    public void showImageDelete(boolean visible){
        if(visible){
            animation.setVisibility(View.GONE);
            deleteChecks.setVisibility(View.VISIBLE);
            deleteAllChecks.setVisibility(View.VISIBLE);
            deleteChecks();
            allChecks();
        } else {
            animation.setVisibility(View.VISIBLE);
            deleteChecks.setVisibility(View.GONE);
            deleteAllChecks.setVisibility(View.GONE);
        }
    }

    private void allChecks() {
        deleteAllChecks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DbChecks dbChecks = new DbChecks(myContext);
                if(deleteAllChecks.isChecked()){
                    ArrayList<Check> checksD= dbChecks.getChecksSendSucces();
                    myContext.idChecksDelete.clear();
                    for(int i = 0; i < checksD.size(); i++){
                        myContext.idChecksDelete.add(checksD.get(i).getIdCheck());
                    }
                    dbChecks.updateChecksDelete(true, authProvider.getId());
                    getChecksById();
                } else {
                    myContext.idChecksDelete.clear();
                    dbChecks.updateChecksDelete(false, authProvider.getId());
                    showImageDelete(false);
                    getChecksById();
                }
            }
        });
    }

    private void deleteChecks() {
        deleteChecks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(myContext)
                        .setTitle("ELIMINAR REGISTROS")
                        .setMessage("¿Deseas eliminar los registros seleccionados?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DbChecks dbChecks = new DbChecks(myContext);
                                boolean delete = dbChecks.delete(myContext.idChecksDelete);
                                if(delete){
                                    showImageDelete(false);
                                    myContext.idChecksDelete.clear();
                                    getChecksById();
                                    if(myContext.menuItemSearch.isActionViewExpanded()){
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
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (checksDbAdapter != null) {
            mReciclerView.getRecycledViewPool().clear();
            checksDbAdapter.notifyDataSetChanged();
            //checksDbAdapter.startListening();
            mReciclerView.scrollToPosition(0);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        /*sharedPreferences = myContext.getSharedPreferences("checksNotSend", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt("numberChecksNotSend",numberChecksNotSend);
        editor.commit();*/
    }

    public void getChecksById() {

        if(checksProvider!=null){
        if(myContext!=null){

            DbChecks dbChecks = new DbChecks(myContext);
            checksDbAdapter = new ChecksDbAdapter(dbChecks.getChecksSendSucces(), myContext);
            mReciclerView.setAdapter(checksDbAdapter);
            mReciclerView.scrollToPosition(0);



            checksDbAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    if(positionStart==0)
                        mReciclerView.scrollToPosition(positionStart);
                }
            });


            checksDbAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    if(myContext.idChecksDelete.size() != 0){
                        showImageDelete(true);
                    } else {
                        showImageDelete(false);
                    }
                }
            });

        }
        }
    }

    public void filterSendOk(ArrayList<Check> checks){
        checksDbAdapter.filtrar(checks);
    }

    public void reviewData() {
        if(checksProvider!=null){
            if(myContext!=null){
                if(authProvider!=null){
                    checksProvider.getChecksByUserAndStatusSend(authProvider.getId(),1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot querySnapshot) {
                            Check check;
                            DbChecks dbChecks = new DbChecks(myContext);
                            ArrayList<String> idDeleteChecks = new ArrayList<>();
                            for (DocumentSnapshot document: querySnapshot.getDocuments()) {
                                check = document.toObject(Check.class);
                                int dias = 31;
                                if(check.getTime() != null){
                                    dias = relativeTime.compareToDate(check.getTime());
                                }
                                if(dias > 30){
                                    idDeleteChecks.add(check.getIdCheck());
                                    checksProvider.deleteCheck(check);
                                } else{
                                    listChecks.add(check);
                                }
                            }
                            if(idDeleteChecks.size() != 0){
                                if(dbChecks.delete(idDeleteChecks)){
                                    Toast.makeText(myContext, "Se eliminaron algunos checks con más de 30 días", Toast.LENGTH_SHORT).show();
                                }
                            }
                            Collections.reverse(listChecks);
                            getChecksById();
                            }
                        });
                    }
                }
            }
        }



    /*public  int compareToDate(Long dateCheck) {
        Date fechaInicial = new Date(dateCheck);
        Long n = new Date().getTime();
        Date fechaFinal = new Date(n);
        int dias = (int) ((fechaFinal.getTime() - fechaInicial.getTime()) / 86400000);
        return dias;
    }*/


    public void updateStatusChecks(int tipeCheck) {
                getChecksById();
                //checksDbAdapter.notifyDataSetChanged();
                mReciclerView.scrollToPosition(0);
    }

    public void updateChecks(int tipeCheck) {
        ChecksProvider checksProvider = new ChecksProvider();
        if(authProvider!=null){
        checksProvider.getChecksNotSend(authProvider.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Check check;
                for (DocumentSnapshot document: queryDocumentSnapshots.getDocuments()) {
                    check = document.toObject(Check.class);
                   // check.setCheckLat(document.getData().g);
                    checksProvider.updateStatus(check.getIdCheck(), tipeCheck);
                }
            }
        });
        }
    }


}