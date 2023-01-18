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
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.dan.timewebclone.R;
import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.adapters.ChecksDbAdapter;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.ChecksProvider;
import com.dan.timewebclone.utils.RelativeTime;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class HistoryChecksSendOkFragment extends Fragment {


    private View mView;
    private RecyclerView mReciclerView;
    private ImageView deleteChecks;
    private LottieAnimationView animation;
    public CheckBox deleteAllChecks;
    private TextView textViewNumberChecksDelete;
    private FrameLayout frameLayoutNumberChecksDelete;

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
    public ArrayList<String> idDeleteChecks;

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
        mView = inflater.inflate(R.layout.fragment_historial, container, false);

        mReciclerView = mView.findViewById(R.id.rvChecks);
        deleteChecks = mView.findViewById(R.id.imageViewDeleteChecks);
        animation = mView.findViewById(R.id.animation);
        deleteAllChecks = mView.findViewById(R.id.checkboxDeleteAll);
        textViewNumberChecksDelete = mView.findViewById(R.id.textViewNumberChecksDelete);
        frameLayoutNumberChecksDelete = mView.findViewById(R.id.frameLayoutNumberChecksDelete);
        linearLayoutManager = new LinearLayoutManager(myContext);
        mReciclerView.setLayoutManager(linearLayoutManager);
        builderDialogUpdateChecks = new AlertDialog.Builder(myContext);
        authProvider = new AuthProvider();
        checksProvider = new ChecksProvider();
        relativeTime = new RelativeTime();
        listChecks = new ArrayList<>();
        idDeleteChecks = new ArrayList<>();
        ch=null;
        //deleteChecks.setVisibility(View.VISIBLE);
        getChecksById();

        return mView;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        myContext = (HomeTW) context;
        super.onAttach(context);
    }

    public void showImageDelete(boolean visible){
        if(visible){
            animation.setVisibility(View.GONE);
            deleteChecks.setVisibility(View.VISIBLE);
            deleteAllChecks.setVisibility(View.VISIBLE);
            frameLayoutNumberChecksDelete.setVisibility(View.VISIBLE);
            textViewNumberChecksDelete.setText(""+myContext.idChecksDelete.size());
            deleteChecks();
            allChecks();
        } else {
            animation.setVisibility(View.VISIBLE);
            deleteChecks.setVisibility(View.GONE);
            deleteAllChecks.setVisibility(View.GONE);
            frameLayoutNumberChecksDelete.setVisibility(View.GONE);
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
                    notifyChangeAdapter();
                } else {
                    myContext.idChecksDelete.clear();
                    dbChecks.updateChecksDelete(false, authProvider.getId());
                    showImageDelete(false);
                    notifyChangeAdapter();
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
                                    notifyChangeAdapter();
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

    public void notifyChangeAdapter() {
        DbChecks dbChecks = new DbChecks(myContext);
        checksDbAdapter.updateChecks(dbChecks.getChecksSendSucces());
        mReciclerView.scrollToPosition(0);
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
                public void onChanged() {
                    if(myContext.idChecksDelete.size() != 0){
                        showImageDelete(true);
                    } else {
                        showImageDelete(false);
                    }
                    super.onChanged();
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
                            Collections.reverse(listChecks);

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




}