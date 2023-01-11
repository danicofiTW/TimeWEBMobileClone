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
    String str;
    String imagenString;
    Bitmap  bitmap;
    HomeTW homeTW;

    public DbChecks(@Nullable Context context) {
        super(context);
        myContext = context;
    }

    public long insertCheck(Check check){
        long id = 0;
        try {
            DbHelper dbHelper = new DbHelper(myContext);
            SQLiteDatabase db = getWritableDatabase();
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

            values.put("isDelete", String.valueOf(check.isDelete()));

            if(check.getImage()!=null){
                //File file = new File(check.getImage());
                values.put("image", check.getImage());
            }
            //values.put("idCompany", employee.getCompany());
            id = db.insert(TABLE_CHECKS, null, values);
        } catch (Exception ex){
            Toast.makeText(myContext, ex.toString(), Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    public ArrayList<Check> getChecksSendSucces(){
        DbHelper dbChecks = new DbHelper(myContext);
        SQLiteDatabase db = dbChecks.getWritableDatabase();

        ArrayList<Check> listChecks = new ArrayList<>();
        Check check = null;
        Cursor cursorChecks = null;


        cursorChecks = db.rawQuery("SELECT * FROM " +TABLE_CHECKS+ " WHERE statusSend='1' ", null);
        if(cursorChecks.moveToFirst()){
            do{
                Long dateL = Long.valueOf(cursorChecks.getString(7));
                double latD = Double.parseDouble(cursorChecks.getString(8));
                double longD = Double.parseDouble(cursorChecks.getString(9));
                boolean bool = Boolean.parseBoolean(cursorChecks.getString(10));
                //byte[] image = cursorChecks.getBlob(6);
               /* if(image != null){
                    imagenString = new String(image);
                } else imagenString = null;*/

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
                listChecks.add(check);
            } while (cursorChecks.moveToNext());
        }
        imagenString = null;
        cursorChecks.close();
        Collections.reverse(listChecks);
        return listChecks;
    }

    public ArrayList<Check> getChecksNotSendSucces(List<Integer> statusSend){
        DbHelper dbChecks = new DbHelper(myContext);
        SQLiteDatabase db = dbChecks.getWritableDatabase();

        ArrayList<Check> listChecks = new ArrayList<>();
        Check check = null;
        Cursor cursorChecks = null;

        if(statusSend.size()==1) {
            cursorChecks = db.rawQuery("SELECT * FROM " + TABLE_CHECKS + " WHERE statusSend='2' ", null);
         } else {
            cursorChecks = db.rawQuery("SELECT * FROM " + TABLE_CHECKS + " WHERE statusSend='0' OR statusSend='2' ", null);
        }
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
                listChecks.add(check);
            } while (cursorChecks.moveToNext());
        }
        imagenString = null;
        cursorChecks.close();
        Collections.reverse(listChecks);
        return listChecks;
    }

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
        }
        cursorChecks.close();
        //Collections.reverse(listChecks);
        return check;
    }

    public boolean updateCheck(String idCheck, int tipeSendCheck, long date){
        DbHelper dbChecks = new DbHelper(myContext);
        SQLiteDatabase db = dbChecks.getWritableDatabase();
        ArrayList<Check> listChecks = new ArrayList<>();
        Check check = null;
        Cursor cursorChecks = null;
        boolean update = false;
        String dateS = String.valueOf(date);

        try{
            db.execSQL("UPDATE " + TABLE_CHECKS + " SET statusSend = '"+tipeSendCheck+"'WHERE statusSend = '0'");
            db.execSQL("UPDATE " + TABLE_CHECKS + " SET idCheck = '"+idCheck+"'WHERE date = '"+dateS+"' and WHERE idcheck = NULL");
            update = true;
        } catch (Exception ex){
            ex.toString();
            update = false;
        } finally {
            dbChecks.close();
        }

        return update;
    }

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


    public boolean updateChecksDelete(boolean isDelete, String idUser){
        DbHelper dbChecks = new DbHelper(myContext);
        SQLiteDatabase db = dbChecks.getWritableDatabase();
        ArrayList<Check> listChecks = new ArrayList<>();
        Check check = null;
        Cursor cursorChecks = null;
        boolean update = false;
        String dateS = String.valueOf(isDelete);

        try{
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

    public boolean deleteAllChecks() {
        boolean delete = false;
        try {
            DbHelper dbHelper = new DbHelper(myContext);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("DELETE FROM " +TABLE_CHECKS + "");
            delete = true;
        } catch (Exception ex){
            ex.toString();
        }

        return delete;
    }
}
