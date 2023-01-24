package com.dan.timewebclone.utils;

import android.app.Application;
import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class RelativeTime extends Application {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public RelativeTime(){

    }

    //Obtener fecha aproximidad hace unos momentos
    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return "Hace un momento";
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "Hace un momento";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "Hace un minuto";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return "Hace " + diff / MINUTE_MILLIS + " minutos";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "Hace una hora";
        } else if (diff < 24 * HOUR_MILLIS) {
            return "Hace " + diff / HOUR_MILLIS + " horas";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "Ayer";
        } else {
            return "Hace " + diff / DAY_MILLIS + " dias";
        }
    }


    //Obtener fecha por dia
    public static String timeFormatAMPM(long time) {

        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm");


        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            String dateString = formatter.format(new Date(time));
            return dateString;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < 24 * HOUR_MILLIS) {
            String dateString = formatter.format(new Date(time));
            return dateString;
        } else if (diff < 48 * HOUR_MILLIS) {
            return "Ayer";
        } else {
            return "Hace " + diff / DAY_MILLIS + " dias";
        }

    }

    //Obtener fecha por dia
    public String timeFormatDay(long time) {
        int diasI = compareToDate(time);

        if (diasI == 0) {
                return "Hoy";
        } else if (diasI == 1) {
                return "Ayer";
        } else {
            return "Hace " + diasI + " dias";
        }
    }

    //Obtener cuantos días hay de diferencia desde el dia de hoy
    public int compareToDate(long time){
        int dias;
        long dateThisMoment = new Date().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        Date aux = new Date(time);
        String date = sdf.format(aux);
        long dateCheckLong = 0;
        try {
            Date d = sdf.parse(date);
            dateCheckLong = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date aux1 = new Date(dateThisMoment);
        String date1 = sdf.format(aux1);
        long dateThisMomentLong = 0;
        try {
            Date d = sdf.parse(date1);
            dateThisMomentLong = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diferencia = dateThisMomentLong - dateCheckLong;
        double diasD = Math.floor(diferencia / (1000 * 60 * 60 * 24));
        dias = (int) diasD;

        return dias;
    }
}
