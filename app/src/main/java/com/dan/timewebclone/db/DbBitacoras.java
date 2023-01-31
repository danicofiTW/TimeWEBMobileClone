package com.dan.timewebclone.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.dan.timewebclone.models.Bitacora;
import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.models.Geocerca;

import java.util.ArrayList;
import java.util.Collections;

public class DbBitacoras extends DbHelper{


    Context myContext;
    //Employee employee;
    public DbBitacoras(@Nullable Context context) {
        super(context);
        myContext = context;
    }

    //Insertar empleado en SQLite
    public long insertBitacora(Bitacora bitacora){
        long id = 0;

        DbHelper dbHelper = new DbHelper(myContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("idBitacora", bitacora.getIdBitacora());
            values.put("idGeocerca", bitacora.getIdGeocerca());
            values.put("idUser", bitacora.getIdUser());
            if(bitacora.isStateActivated()){
                values.put("stateActivated", 1);
            } else{
                values.put("stateActivated", 0);
            }
            //values.put("idCompany", employee.getCompany());
            id = db.insert(TABLE_BITACORA, null, values);
        } catch (Exception ex){
            ex.toString();
        } finally {
            dbHelper.close();
        }

        return id;
    }

    //Eliminar empleado por id
    public boolean deleteBitacora(String idBitacora){
        boolean delete = false;
        DbHelper dbHelper = new DbHelper(myContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " +TABLE_BITACORA + " WHERE idBitacora= '"+idBitacora+"'");
            delete=true;
        } catch (Exception ex){
            ex.toString();
        } finally {
            dbHelper.close();
        }

        return delete;
    }

    //Obtener el empleado por id
    public Bitacora getBitacora(String idBitacora){
        DbHelper dbHelper = new DbHelper(myContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Bitacora bitacora = null;
        Cursor cursorChecks = null;

        cursorChecks = db.rawQuery("SELECT * FROM " +TABLE_BITACORA+ " WHERE idBitacora='"+idBitacora+"'", null);
        if(cursorChecks.moveToFirst()){
            bitacora = new Bitacora();
            bitacora.setIdBitacora(cursorChecks.getString(1));
            bitacora.setIdGeocerca(cursorChecks.getString(2));
            bitacora.setIdUser(cursorChecks.getString(3));

            if(cursorChecks.getInt(4) == 1){
                bitacora.setStateActivated(true);
            } else {
                bitacora.setStateActivated(false);
            }
        }
        cursorChecks.close();
        dbHelper.close();
        //Collections.reverse(listChecks);
        return bitacora;
    }

    public ArrayList<Bitacora> getBitacorasByIdUser(String idUser){
        DbHelper dbHelper = new DbHelper(myContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Bitacora> listBitacoras = new ArrayList<>();
        Bitacora bitacora = null;
        Cursor cursorChecks = null;


        cursorChecks = db.rawQuery("SELECT * FROM " +TABLE_BITACORA+ " WHERE idUser='"+idUser+"' ", null);
        if(cursorChecks.moveToFirst()){
            do{
                bitacora = new Bitacora();
                bitacora.setIdBitacora(cursorChecks.getString(1));
                bitacora.setIdGeocerca(cursorChecks.getString(2));
                bitacora.setIdUser(cursorChecks.getString(3));

                if(cursorChecks.getInt(4) == 1){
                    bitacora.setStateActivated(true);
                } else {
                    bitacora.setStateActivated(false);
                }
                listBitacoras.add(bitacora);
            } while (cursorChecks.moveToNext());
        }
        dbHelper.close();
        cursorChecks.close();
        //Collections.reverse(listBitacoras);
        return listBitacoras;
    }

    //Eliminar todas las geocercas
    public boolean deleteAllBitacoras() {
        DbHelper dbHelper = new DbHelper(myContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean delete = false;
        try{
            db.execSQL("DELETE FROM " +TABLE_BITACORA+ "");
            delete = true;
        } catch (Exception ex){
            ex.toString();
            delete = false;
        } finally {
            dbHelper.close();
        }
        return delete;
    }

}
