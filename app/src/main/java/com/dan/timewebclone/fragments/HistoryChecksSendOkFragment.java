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
import com.dan.timewebclone.adapters.ChecksDbAdapter;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.ChecksProvider;
import com.dan.timewebclone.utils.RelativeTime;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class HistoryChecksSendOkFragment extends Fragment  {


    private View mView;
    private RecyclerView mReciclerView;
    public ImageView deleteChecks, imageViewCancelDelete;
    private LottieAnimationView animation;
    public CheckBox deleteAllChecks;
    private TextView textViewNumberChecksDelete, textViewTitleHistory, textViewNoChecks;
    private FrameLayout frameLayoutNumberChecksDelete;
    private LinearLayout linearLayoutDeleteChecks;

    private AuthProvider authProvider;
    private ChecksProvider checksProvider;
    public ChecksDbAdapter checksDbAdapter;
    private DbChecks dbChecks;

    private RelativeTime relativeTime;
    private AlertDialog.Builder builderDialogUpdateChecks;
    private HomeTW myContext;
    private LinearLayoutManager linearLayoutManager;
    //public int numberChecksNotSend;
    private Check ch;
    public ArrayList<Check> listChecks;
    public ArrayList<String> idDeleteChecks;
    private Check check;
    private int post;

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
        linearLayoutDeleteChecks = mView.findViewById(R.id.linearLayoutDeleteHistory);
        textViewTitleHistory = mView.findViewById(R.id.textViewTitleHistoryChecks);
        imageViewCancelDelete = mView.findViewById(R.id.imageViewCancelDelete);
        textViewNoChecks = mView.findViewById(R.id.textViewNoChecks);
        builderDialogUpdateChecks = new AlertDialog.Builder(myContext);

        linearLayoutManager = new LinearLayoutManager(myContext);
        mReciclerView.setLayoutManager(linearLayoutManager);
        mReciclerView.setHasFixedSize(true);

        authProvider = new AuthProvider();
        checksProvider = new ChecksProvider();
        dbChecks = new DbChecks(myContext);
        relativeTime = new RelativeTime();
        listChecks = new ArrayList<>();
        idDeleteChecks = new ArrayList<>();
        ch=null;

        //deleteChecks.setVisibility(View.VISIBLE);
        getChecksById();
        //checksDbAdapter.updateChecks(dbChecks.getChecksSendSucces(authProvider.getId()));
        if(checksDbAdapter.checks.size() != 0){
            textViewNoChecks.setVisibility(View.GONE);
        }
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
            textViewTitleHistory.setVisibility(View.GONE);
            deleteChecks.setVisibility(View.VISIBLE);
            linearLayoutDeleteChecks.setVisibility(View.VISIBLE);
            /*deleteAllChecks.setVisibility(View.VISIBLE);
            frameLayoutNumberChecksDelete.setVisibility(View.VISIBLE);*/
            textViewNumberChecksDelete.setText(""+myContext.idChecksDelete.size());
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
        myContext.idChecksDelete.clear();
        dbChecks.updateChecksDelete(false, authProvider.getId(), true);
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
                if(deleteAllChecks.isChecked()){
                    ArrayList<Check> checksD= dbChecks.getChecksSendSucces(authProvider.getId());
                    myContext.idChecksDelete.clear();
                    for(int i = 0; i < checksD.size(); i++){
                        myContext.idChecksDelete.add(checksD.get(i).getIdCheck());
                    }
                    dbChecks.updateChecksDelete(true, authProvider.getId(), true);
                    notifyChangeAdapter();
                } else {
                    myContext.idChecksDelete.clear();
                    dbChecks.updateChecksDelete(false, authProvider.getId(), true);
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
                if(myContext.idChecksDelete.size() != 0){
                    String message;
                    if(deleteAllChecks.isChecked()){
                        message = "¿Deseas eliminar todos los registros enviados?";
                    } else {
                        message = "¿Deseas eliminar los "+myContext.idChecksDelete.size()+" registros seleccionados?";
                    }
                    new AlertDialog.Builder(myContext)
                            .setTitle("ELIMINAR REGISTROS")
                            .setMessage(message)
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
                } else {
                    showImageDelete(false);
                }
            }
        });
    }

    public void notifyChangeAdapter() {
        checksDbAdapter.updateChecks(dbChecks.getChecksSendSucces(authProvider.getId()));
        mReciclerView.scrollToPosition(0);
    }

    public void getChecksById() {
        if(checksProvider!=null){
        if(myContext!=null){
            ArrayList<Check> checks = dbChecks.getChecksSendSucces(authProvider.getId());
            checksDbAdapter = new ChecksDbAdapter(checks, myContext);
            mReciclerView.setAdapter(checksDbAdapter);
           // mReciclerView.setNestedScrollingEnabled(false);
            checksDbAdapter.notifyDataSetChanged();
            //mReciclerView.scrollToPosition(checksDbAdapter.checks.size());
            //mReciclerView.scrollToPosition(0);

            checksDbAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    if(myContext.idChecksDelete.size() != 0){
                        showImageDelete(true);
                    } else {
                        showImageDelete(false);
                    }

                    if(checksDbAdapter.checks.size() == 0){
                        textViewNoChecks.setVisibility(View.VISIBLE);
                    } else {
                        textViewNoChecks.setVisibility(View.GONE);
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
                    checksProvider.getChecksByUserAndStatusSend(authProvider.getId(), 1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Check check;
                            if(task.isSuccessful()){
                                for (DocumentSnapshot document : task.getResult()) {
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
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onStop() {
        dbChecks.updateChecksDelete(false, authProvider.getId(), true);
        myContext.idChecksDelete.clear();
        checksDbAdapter.notifyDataSetChanged();
        super.onStop();
    }




    /*public  int compareToDate(Long dateCheck) {
        Date fechaInicial = new Date(dateCheck);
        Long n = new Date().getTime();
        Date fechaFinal = new Date(n);
        int dias = (int) ((fechaFinal.getTime() - fechaInicial.getTime()) / 86400000);
        return dias;
    }*/


    /*@Override
    public void onItemClickListener(Check check1, int position) {
        check = check1;
        post = position;
        if(myContext.isViewDeleteSendOk() == View.GONE){
            Intent i = new Intent(myContext, ShowLocationActivity.class);
            i.putExtra("lat", check.getCheckLat());
            i.putExtra("lng", check.getCheckLong());
            i.putExtra("date", check.getTime());
            i.putExtra("tipe", check.getTipeCheck());
            i.putExtra("idCheck", check.getIdCheck());
            myContext.startActivity(i);
        } else {
            checksDbAdapter.notifyDataSetChanged();
        }
    } else {
        // if(!longClick){
        if(!check.isDelete()) {
            checksDbAdapter.onBindViewHolder(checksDbAdapter.onCreateViewHolder(), post).viewDelete.setVisibility(View.VISIBLE);
            checksDbAdapter.viewHolders.get(post).imageViewDelete.setVisibility(View.VISIBLE);
            context.idChecksDelete.add(check.getIdCheck());
            check.setDelete(true);
            dbChecks.updateCheckDelete(true, check.getIdCheck());
            if(context.idChecksDelete.size() == checks.size()){
                context.updateDeleteAllChecksSeendOk(true);
            }
            notifyDataSetChanged();
            //check.setDelete(true);
        } else {
            holder.viewDelete.setVisibility(View.GONE);
            holder.imageViewDelete.setVisibility(View.GONE);
            check.setDelete(false);
            dbChecks.updateCheckDelete(false, check.getIdCheck());
            context.updateDeleteAllChecksSeendOk(false);
            for (int i = 0; i < context.idChecksDelete.size(); i++) {
                if (context.idChecksDelete.get(i).equals(check.getIdCheck())) {
                    context.idChecksDelete.remove(i);
                    break;
                }
            }

            notifyDataSetChanged();
        }
    }
    }*/


}