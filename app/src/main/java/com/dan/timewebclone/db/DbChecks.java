package com.dan.timewebclone.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.dan.timewebclone.activitys.HomeTW;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.models.Employee;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DbChecks extends DbHelper{

    Context myContext;
    String imagenString;

    public DbChecks(@Nullable Context context) {
        super(context);
        myContext = context;
    }

    //Insertar un registro en SQLite
    public long insertCheck(Check check){
        long id = 0;
        DbHelper dbHelper = new DbHelper(myContext);
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            /*yteArrayOutputStream baos = new ByteArrayOutputStream(20480);
            imagen.compress(Bitmap.CompressFormat.PNG, 0 , baos);
            byte[] blob = baos.toByteArray();*/
            values.put("idCheck", check.getIdCheck());
            values.put("idUser", check.getIdUser());
            values.put("tipeCheck", check.getTipeCheck());
            values.put("urlImage", check.getUrlImage());
            values.put("date", check.getTime());
            values.put("checkLat", check.getCheckLat());
            values.put("checkLong", check.getCheckLong());
            values.put("statusSend", check.getStatusSend());
            values.put("dateSend", check.getTimeSend());

            values.put("isDelete", String.valueOf(check.isDelete()));

            if(check.getImage()!=null){
                //File file = new File(check.getImage());
                values.put("image", check.getImage());
            }
            //values.put("idCompany", employee.getCompany());
            id = db.insert(TABLE_CHECKS, null, values);
        } catch (Exception ex){
            Toast.makeText(myContext, ex.toString(), Toast.LENGTH_SHORT).show();
        } finally {
            dbHelper.close();
        }
        return id;
    }


    //Obtener los registros enviados correctamente
    public ArrayList<Check> getChecksSendSucces(String idUser){
        DbHelper dbChecks = new DbHelper(myContext);
        SQLiteDatabase db = dbChecks.getWritableDatabase();

        ArrayList<Check> listChecks = new ArrayList<>();
        Check check = null;
        Cursor cursorChecks = null;


        cursorChecks = db.rawQuery("SELECT * FROM " +TABLE_CHECKS+ " WHERE statusSend='1' AND idUser='"+idUser+"' ", null);
        if(cursorChecks.moveToFirst()){
            do{
                Long dateL = Long.valueOf(cursorChecks.getString(7));
                double latD = Double.parseDouble(cursorChecks.getString(8));
                double longD = Double.parseDouble(cursorChecks.getString(9));
                boolean bool = Boolean.parseBoolean(cursorChecks.getString(10));

                check = new Check();
                check.setIdCheck(cursorChecks.getString(1));
                check.setIdUser(cursorChecks.getString(2));
                check.setTipeCheck(cursorChecks.getString(4));
                check.setTime(dateL);
                check.setCheckLat(latD);
                check.setCheckLong(longD);
                check.setImage(cursorChecks.getString(6));
                check.setUrlImage(cursorChecks.getString(5));
                check.setDelete(bool);
                check.setStatusSend(cursorChecks.getInt(11));
                check.setTimeSend(cursorChecks.getInt(12));
                listChecks.add(check);
            } while (cursorChecks.moveToNext());
        }
        imagenString = null;
        dbChecks.close();
        cursorChecks.close();
        Collections.reverse(listChecks);
        return listChecks;
    }


    //Obtener los registros no enviados correctamente
    public ArrayList<Check> getChecksNotSendSucces(String idUser){
        DbHelper dbChecks = new DbHelper(myContext);
        SQLiteDatabase db = dbChecks.getWritableDatabase();

        ArrayList<Check> listChecks = new ArrayList<>();
        Check check = null;
        Cursor cursorChecks = null;

        cursorChecks = db.rawQuery("SELECT * FROM " + TABLE_CHECKS + " WHERE statusSend='0' OR statusSend='2' AND idUser='"+idUser+"' ", null);

        if(cursorChecks.moveToFirst()){
            do{
                Long dateL = Long.valueOf(cursorChecks.getString(7));
                double latD = Double.parseDouble(cursorChecks.getString(8));
                double longD = Double.parseDouble(cursorChecks.getString(9));
                boolean bool = Boolean.parseBoolean(cursorChecks.getString(10));

                check = new Check();
                check.setIdCheck(cursorChecks.getString(1));
                check.setIdUser(cursorChecks.getString(2));
                check.setTipeCheck(cursorChecks.getString(4));
                check.setImage(cursorChecks.getString(6));
                check.setUrlImage(cursorChecks.getString(5));
                check.setTime(dateL);
                check.setCheckLat(latD);
                check.setCheckLong(longD);
                check.setDelete(bool);
                check.setStatusSend(cursorChecks.getInt(11));
                check.setTimeSend(cursorChecks.getInt(12));
                listChecks.add(check);
            } while (cursorChecks.moveToNext());
        }
        imagenString = null;
        cursorChecks.close();
        dbChecks.close();
        Collections.reverse(listChecks);
        return listChecks;
    }


    //Obtener un registro con id
    public Check getCheck(String idCheck){
        DbHelper dbChecks = new DbHelper(myContext);
        SQLiteDatabase db = dbChecks.getWritableDatabase();

        Check check = null;
        Cursor cursorChecks = null;

        cursorChecks = db.rawQuery("SELECT * FROM " +TABLE_CHECKS+ " WHERE idCheck='"+idCheck+"'", null);
        if(cursorChecks.moveToFirst()){
            check = new Check();
            Long dateL = Long.valueOf(cursorChecks.getString(7));
            double latD = Double.parseDouble(cursorChecks.getString(8));
            double longD = Double.parseDouble(cursorChecks.getString(9));

            check = new Check();
            check.setIdCheck(cursorChecks.getString(1));
            check.setIdUser(cursorChecks.getString(2));
            check.setTipeCheck(cursorChecks.getString(4));
            check.setImage(cursorChecks.getString(6));
            check.setUrlImage(cursorChecks.getString(5));
            check.setTime(dateL);
            check.setCheckLat(latD);
            check.setCheckLong(longD);
            check.setStatusSend(cursorChecks.getInt(11));
            check.setTimeSend(cursorChecks.getInt(12));
        }
        cursorChecks.close();
        dbChecks.close();
        //Collections.reverse(listChecks);
        return check;
    }

    //Actulizar registro enviado
    public boolean updateCheck(String idCheck, int tipeSendCheck, long date, long dateSend){
        DbHelper dbChecks = new DbHelper(myContext);
        SQLiteDatabase db = dbChecks.getWritableDatabase();
        ArrayList<Check> listChecks = new ArrayList<>();
        Check check = null;
        Cursor cursorChecks = null;
        boolean update = false;
        String dateS = String.valueOf(date);

        try{
            db.execSQL("UPDATE " + TABLE_CHECKS + " SET idCheck = '"+idCheck+"'WHERE date = '"+dateS+"'");
            db.execSQL("UPDATE " + TABLE_CHECKS + " SET statusSend = '"+tipeSendCheck+"'WHERE statusSend = '0'");
            db.execSQL("UPDATE " + TABLE_CHECKS + " SET dateSend = '"+dateSend+"'WHERE idCheck = '"+idCheck+"'");
            update = true;
        } catch (Exception ex){
            ex.toString();
            update = false;
        } finally {
            dbChecks.close();
        }

        return update;
    }

    //Revisar registros no enviados
    public boolean reviewChecks(int tipeSendCheck){
        DbHelper dbChecks = new DbHelper(myContext);
        SQLiteDatabase db = dbChecks.getWritableDatabase();
        ArrayList<Check> listChecks = new ArrayList<>();
        Check check = null;
        Cursor cursorChecks = null;
        boolean update = false;

        try{
            db.execSQL("UPDATE " + TABLE_CHECKS + " SET statusSend = '"+tipeSendCheck+"'WHERE statusSend = '0'");
            update = true;
        } catch (Exception ex){
            ex.toString();
            update = false;
        } finally {
            dbChecks.close();
        }

        return update;
    }


    //Actualizar registros a eliminar
    public boolean updateChecksDelete(boolean isDelete, String idUser, boolean sendOk){
        DbHelper dbChecks = new DbHelper(myContext);
        SQLiteDatabase db = dbChecks.getWritableDatabase();
        ArrayList<Check> listChecks = new ArrayList<>();
        Check check = null;
        Cursor cursorChecks = null;
        boolean update = false;
        String dateS = String.valueOf(isDelete);

        try{
            if (sendOk) {
                db.execSQL("UPDATE " + TABLE_CHECKS + " SET isDelete = '"+dateS+"' WHERE idUser = '"+idUser+"' AND statusSend='1'");
            } else {
                db.execSQL("UPDATE " + TABLE_CHECKS + " SET isDelete = '"+dateS+"' WHERE idUser = '"+idUser+"' AND statusSend='0' OR statusSend='2'");
            }
            //db.execSQL("UPDATE " + TABLE_CHECKS + " SET statusSend = '"+tipeSendCheck+"'WHERE statusSend = '0'");
            db.execSQL("UPDATE " + TABLE_CHECKS + " SET isDelete = '"+dateS+"'WHERE idUser = '"+idUser+"'");
            update = true;
        } catch (Exception ex){
            ex.toString();
            update = false;
        } finally {
            dbChecks.close();
        }

        return update;
    }

    //Actualizar registro a eliminar
    public boolean updateCheckDelete(boolean isDelete, String idCheck){
        DbHelper dbChecks = new DbHelper(myContext);
        SQLiteDatabase db = dbChecks.getWritableDatabase();
        ArrayList<Check> listChecks = new ArrayList<>();
        Check check = null;
        Cursor cursorChecks = null;
        boolean update = false;
        String dateS = String.valueOf(isDelete);

        try{
            //db.execSQL("UPDATE " + TABLE_CHECKS + " SET statusSend = '"+tipeSendCheck+"'WHERE statusSend = '0'");
            db.execSQL("UPDATE " + TABLE_CHECKS + " SET isDelete = '"+dateS+"'WHERE idCheck = '"+idCheck+"'");
            update = true;
        } catch (Exception ex){
            ex.toString();
            update = false;
        } finally {
            dbChecks.close();
        }

        return update;
    }


    //Eliminar registros con id
    public boolean delete(ArrayList<String> idChecksDelete) {
        DbHelper dbChecks = new DbHelper(myContext);
        SQLiteDatabase db = dbChecks.getWritableDatabase();
        ArrayList<Check> listChecks = new ArrayList<>();
        Check check = null;
        Cursor cursorChecks = null;
        boolean delete = false;

        try{
            for (int i = 0; i < idChecksDelete.size(); i++){
                db.execSQL("DELETE FROM " +TABLE_CHECKS+ " WHERE idCheck='"+idChecksDelete.get(i)+"'");
            }
             delete = true;
        } catch (Exception ex){
            ex.toString();
            delete = false;
        } finally {
            dbChecks.close();
        }

        return delete;
    }

    //Eliminar todos los registros de SQLite
    public boolean deleteAllChecks() {
        boolean delete = false;
        DbHelper dbHelper = new DbHelper(myContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " +TABLE_CHECKS + "");
            delete = true;
        } catch (Exception ex){
            ex.toString();
        } finally {
            dbHelper.close();
        }

        return delete;
    }
}
