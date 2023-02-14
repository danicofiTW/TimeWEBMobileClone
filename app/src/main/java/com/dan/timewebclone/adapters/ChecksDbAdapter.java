package com.dan.timewebclone.adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.dan.timewebclone.R;
import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.activitys.ShowLocationActivity;
import com.dan.timewebclone.db.DbChecks;
import com.dan.timewebclone.fragments.HistoryChecksSendOkFragment;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.providers.AuthProvider;
import com.dan.timewebclone.providers.ChecksProvider;
import com.dan.timewebclone.providers.EmployeeProvider;
import com.dan.timewebclone.utils.RelativeTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChecksDbAdapter extends RecyclerView.Adapter<ChecksDbAdapter.CheckViewHolder> {

    HomeTW context;
    //private final FirebaseAuth authP;
    private AuthProvider authP;
    EmployeeProvider employeeProvider;
    ChecksProvider checksProvider;
    private RelativeTime relativeTime;
    Employee employee;

    public ArrayList<ChecksDbAdapter.  CheckViewHolder> viewHolders;
    public ArrayList<Check> checks;
    public ArrayList<String> idChecksDelete;
    private SimpleDateFormat sdfLongDate;

    private SimpleDateFormat sdfDate;
    private DbChecks dbChecks;
    private boolean noReviewDelete;
    //private Animation.AnimationListener animLis;


    HistoryChecksSendOkFragment historyChecksSendOkFragment;



    public ChecksDbAdapter(ArrayList<Check> listChecks, HomeTW context) {
        this.context = context;
        authP = new AuthProvider();
        employeeProvider = new EmployeeProvider();
        checksProvider = new ChecksProvider();
        employee = new Employee();
        viewHolders = new ArrayList<>();
        relativeTime = new RelativeTime();
        sdfLongDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        dbChecks = new DbChecks(context);
        noReviewDelete = false;
        historyChecksSendOkFragment = new HistoryChecksSendOkFragment();
        //context.semanasSendOk=0;
        checks = listChecks;
        idChecksDelete = new ArrayList<>();
    }


    @Override
    public ChecksDbAdapter.CheckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_history_checks, parent, false);
        return new ChecksDbAdapter.CheckViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull CheckViewHolder holder, int position) {

        Date aux = new Date(checks.get(position).getTime());
        String date = sdfLongDate.format(aux);
        holder.textViewFecha.setText(date);

        if(checks.get(position).getStatusSend() == 1){
            holder.imageViewSendCheck.setImageResource(R.drawable.icon_double_check);
        }

        if(checks.get(position).getTipeCheck().equals("startWork")){
            //holder.imageViewHistoryCheck.setImageResource(R.drawable.icon_entrar);

            holder.imageViewHistoryCheck.setImageResource(R.drawable.icon_int);
            holder.textViewTipeCheck.setText("Registro de Entrada");
            holder.textViewTipeCheck.setTextColor(context.getColor(R.color.colorGreenLigth));
        } else if(checks.get(position).getTipeCheck().equals("startEating")){
            //holder.imageViewHistoryCheck.setImageResource(R.drawable.icon_entrarcomer);

            holder.imageViewHistoryCheck.setImageResource(R.drawable.icon_comer);
            holder.textViewTipeCheck.setText("Registro de Comida");
            holder.textViewTipeCheck.setTextColor(context.getColor(R.color.colorBlueLigth));
        } else if(checks.get(position).getTipeCheck().equals("finishEating")){
            //holder.imageViewHistoryCheck.setImageResource(R.drawable.icon_terminarcomer);

            holder.imageViewHistoryCheck.setImageResource(R.drawable.icon_termincomer);
            holder.textViewTipeCheck.setText("Registro de Fin Comida");
            holder.textViewTipeCheck.setTextColor(context.getColor(R.color.colorYellowLigth));
        } else if(checks.get(position).getTipeCheck().equals("finishWork")){
            //holder.imageViewHistoryCheck.setImageResource(R.drawable.icon_salir);

            holder.imageViewHistoryCheck.setImageResource(R.drawable.icon_out);
            holder.textViewTipeCheck.setText("Registro de Salida");
            holder.textViewTipeCheck.setTextColor(context.getColor(R.color.colorRedLigth));
        }

        if(checks.get(position).getIdGeocerca() == null){
            holder.textViewGeocerca.setText("Sin geocerca asignada");
        } else {
            holder.textViewGeocerca.setText(checks.get(position).getNameGeocerca());
        }

        /*holder.imageViewDelete.animate().setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}
            @Override
            public void onAnimationEnd(Animator animator) {
                //holder.imageViewDelete.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationCancel(Animator animator) {}
            @Override
            public void onAnimationRepeat(Animator animator) {}
        });*/

        //if(context.idChecksDelete.size() > 0){
            if(checks.get(position).isDelete()){
                holder.viewDelete.setVisibility(View.VISIBLE);
                holder.imageViewDelete.setVisibility(View.VISIBLE);
            } else {
                    holder.viewDelete.setVisibility(View.GONE);
                    holder.imageViewDelete.setVisibility(View.GONE);
                }
       /* } else {
            if(!noReviewDelete){
                checks.get(position).setDelete(false);
                holder.viewDelete.setVisibility(View.GONE);
                holder.imageViewDelete.setVisibility(View.GONE);
                if(position == checks.size()-1){
                    noReviewDelete = true;
                }
            }
        }*/


        setImage(holder, position);
        openLocation(holder, checks.get(position));
        longCLickCheck(holder,checks.get(position));
        reviewDate(holder, checks, position);
    }

    private void setImage(CheckViewHolder holder, int position){
            if(checks.get(position).getImage() != null){
                try{
                    byte[] decodedString = Base64.decode(checks.get(position).getImage(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    //RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), decodedByte);
                    if(decodedByte != null){
                        holder.imageViewHistoryCheck.setImageBitmap(decodedByte);
                    } else {
                        holder.imageViewHistoryCheck.setImageResource(R.drawable.ic_broken_image_white);
                    }
                }
                catch(Exception e){
                    e.getMessage();
                }
            }
    }

    private void longCLickCheck(CheckViewHolder holder, Check check){
        holder.myView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!check.isDelete()){
                    holder.viewDelete.setVisibility(View.VISIBLE);
                    holder.imageViewDelete.setVisibility(View.VISIBLE);
                    //holder.imageViewDelete.animate().alpha(1.0f);
                    context.idChecksDelete.add(check.getIdCheck());
                    check.setDelete(true);
                    dbChecks.updateCheckDelete(true, check.getIdCheck());
                    if(context.idChecksDelete.size() == checks.size()){
                        context.updateDeleteAllChecksSeendOk(true);
                        notifyDataSetChanged();
                    } else if(context.idChecksDelete.size() == 1){
                        notifyDataSetChanged();
                    }
                    context.showNumberDelete();
                } else {
                    holder.viewDelete.setVisibility(View.GONE);
                    holder.imageViewDelete.setVisibility(View.GONE);
                    //holder.imageViewDelete.animate().alpha(0.0f).setDuration(1000);
                    check.setDelete(false);
                    dbChecks.updateCheckDelete(false, check.getIdCheck());
                    context.updateDeleteAllChecksSeendOk(false);

                    for(int i = 0; i<context.idChecksDelete.size(); i++){
                        if (context.idChecksDelete.get(i).equals(check.getIdCheck())) {
                            context.idChecksDelete.remove(i);
                            break;
                        }
                    }
                    if(context.idChecksDelete.size() == 0)
                    notifyDataSetChanged();

                    context.showNumberDelete();
                }
                return false;
            }
        });
    }

    private void openLocation(ChecksDbAdapter.CheckViewHolder holder, Check check) {

        holder.myView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(context.idChecksDelete.size()==0){
                    if(context.isViewDeleteSendOk() == View.GONE){
                        Intent i = new Intent(context, ShowLocationActivity.class);
                        i.putExtra("lat", check.getCheckLat());
                        i.putExtra("lng", check.getCheckLong());
                        i.putExtra("date", check.getTime());
                        i.putExtra("tipe", check.getTipeCheck());
                        i.putExtra("idCheck", check.getIdCheck());
                        context.startActivity(i);
                    } else {
                        notifyDataSetChanged();
                    }
                } else {
                   // if(!longClick){
                    if(!check.isDelete()) {
                        holder.viewDelete.setVisibility(View.VISIBLE);
                        holder.imageViewDelete.setVisibility(View.VISIBLE);
                        //holder.imageViewDelete.animate().alpha(1.0f);
                        context.idChecksDelete.add(check.getIdCheck());
                        check.setDelete(true);
                        dbChecks.updateCheckDelete(true, check.getIdCheck());
                        if(context.idChecksDelete.size() == checks.size()){
                            context.updateDeleteAllChecksSeendOk(true);
                            notifyDataSetChanged();
                        }

                        context.showNumberDelete();
                    } else {
                        holder.viewDelete.setVisibility(View.GONE);
                        holder.imageViewDelete.setVisibility(View.GONE);
                        //holder.imageViewDelete.animate().alpha(0.0f).setDuration(500);
                        check.setDelete(false);
                        dbChecks.updateCheckDelete(false, check.getIdCheck());
                        context.updateDeleteAllChecksSeendOk(false);
                        for (int i = 0; i < context.idChecksDelete.size(); i++) {
                            if (context.idChecksDelete.get(i).equals(check.getIdCheck())) {
                                context.idChecksDelete.remove(i);
                                break;
                            }
                        }
                        if(context.idChecksDelete.size() == 0)
                        notifyDataSetChanged();


                        context.showNumberDelete();
                    }
                    }
                }
               // longClick = false;
           // }
        });
    }

    private void reviewDate(CheckViewHolder holder, ArrayList<Check> checks, int position) {
        Date aux = new Date(checks.get(position).getTime());
        String date1S = sdfDate.format(aux);
        Date date1 = null;
        int days = relativeTime.compareToDate(checks.get(position).getTime());
        checks.get(position).setSemana(Math.abs(days/7));
        try {
            date1 = sdfDate.parse(date1S);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(position!=0){
            Date aux2 = new Date(checks.get(position-1).getTime());
            String date2S = sdfDate.format(aux2);
            Date date2 = null;
            try {
                date2 = sdfDate.parse(date2S);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(date2.after(date1)){
                if(checks.get(position).getSemana() == 0){
                    holder.linearLayoutLineDate.setVisibility(View.VISIBLE);
                    holder.textViewLineDate.setText(relativeTime.timeFormatDay(checks.get(position).getTime()));
                    holder.linearLayoutCheck.setPadding(0,35,0,0);
                } else {
                    if (checks.get(position).getSemana() == 1 && checks.get(position-1).getSemana() != checks.get(position).getSemana()) {
                        context.semanasSendOk=1;
                        holder.linearLayoutLineDate.setVisibility(View.VISIBLE);
                        holder.textViewLineDate.setText("Hace 1 semana");
                        holder.linearLayoutCheck.setPadding(0, 35, 0, 0);
                    } else {
                        if (checks.get(position).getSemana() > 1) {
                            if (checks.get(position-1).getSemana() != checks.get(position).getSemana()) {
                                //context.semanasSendOk=checks.get(position).getSemana();
                                holder.linearLayoutLineDate.setVisibility(View.VISIBLE);
                                holder.textViewLineDate.setText("Hace " + checks.get(position).getSemana() + " semanas");
                                holder.linearLayoutCheck.setPadding(0, 35, 0, 0);
                            } else {
                                holder.linearLayoutLineDate.setVisibility(View.GONE);
                                holder.linearLayoutCheck.setPadding(0,0,0,0);
                            }
                        } else {
                            holder.linearLayoutLineDate.setVisibility(View.GONE);
                            holder.linearLayoutCheck.setPadding(0,0,0,0);
                        }
                    }
                }
            } else {
                holder.linearLayoutLineDate.setVisibility(View.GONE);
                holder.linearLayoutCheck.setPadding(0, 0, 0, 0);
            }
        } else {
            holder.linearLayoutLineDate.setVisibility(View.VISIBLE);
            holder.textViewLineDate.setText(relativeTime.timeFormatDay(checks.get(position).getTime()));
            holder.linearLayoutLineDate.setPadding(0,8,0,0);
            holder.linearLayoutCheck.setPadding(0,44,0,0);
        }
    }


    public void filtrar(ArrayList<Check> checks) {
        this.checks = checks;
        notifyDataSetChanged();
    }
    public void updateChecks(ArrayList<Check> checks) {
        this.checks = checks;
        notifyDataSetChanged();
    }

    public void updateDeleteChecks() {
        for(int i = 0; i<this.checks.size(); i++){
            this.checks.get(i).setDelete(false);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return checks.size();
    }

    public class CheckViewHolder extends RecyclerView.ViewHolder{
        TextView textViewGeocerca, textViewTipeCheck, textViewFecha, textViewLineDate;
        ImageView imageViewSendCheck, imageViewHistoryCheck, imageViewDelete;
        LinearLayout linearLayoutOpenLocation, linearLayoutLineDate, linearLayoutCheck;
        LottieAnimationView lottieAnimationViewDeleteCheck;
        CardView cardViewCheck;
        View myView, viewDelete;


        public CheckViewHolder(View view){
            super(view);
            myView = view;
            linearLayoutCheck = view.findViewById(R.id.linearLayoutCheck);
            cardViewCheck = view.findViewById(R.id.cardViewCheck);
            linearLayoutOpenLocation = view.findViewById(R.id.linearLayoutInfo);
            textViewGeocerca = view.findViewById(R.id.textViewGeocerca);
            textViewTipeCheck = view.findViewById(R.id.textViewTipeCheck);
            textViewFecha = view.findViewById(R.id.textViewFecha);
            imageViewSendCheck = view.findViewById(R.id.imageViewSendCheck);
            imageViewHistoryCheck = view.findViewById(R.id.imageViewHistoryCheck);
            imageViewDelete = view.findViewById(R.id.imageViewDelete);
            textViewLineDate = view.findViewById(R.id.textViewLineDate);
            linearLayoutLineDate = view.findViewById(R.id.lineDate);
            viewDelete = view.findViewById(R.id.viewDelete);
            lottieAnimationViewDeleteCheck = view.findViewById(R.id.animationDeleteCheck);
        }
    }
}
