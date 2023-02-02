package com.dan.timewebclone.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.models.Employee;
import com.dan.timewebclone.models.Geocerca;

import java.util.ArrayList;

public class DbGeocercas extends DbHelper{
    Context myContext;
    //Employee employee;
    public DbGeocercas(@Nullable Context context) {
        super(context);
        myContext = context;
    }

    //Insertar empleado en SQLite
    public long insertGeocerca(Geocerca geocerca){
        long id = 0;

        DbHelper dbHelper = new DbHelper(myContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("idGeocerca", geocerca.getIdGeocerca());
            values.put("clave", geocerca.getClave());
            values.put("idCompany", geocerca.getIdCompany());
            values.put("geoNombre", geocerca.getGeoNombre());
            values.put("descripcion", geocerca.getDescripcion());
            values.put("geoLatitud", geocerca.getGeoLat());
            values.put("geoLongitud", geocerca.getGeoLong());
            values.put("radio", geocerca.getRadio());
            values.put("direccion", geocerca.getDireccion());
            values.put("alta", geocerca.getAlta());
            if(geocerca.isStatus()){
                values.put("status", 1);
            } else{
                values.put("status", 0);
            }
            //values.put("idCompany", employee.getCompany());
            id = db.insert(TABLE_GEOCERCA, null, values);
        } catch (Exception ex){
            ex.toString();
        } finally {
            dbHelper.close();
        }

        return id;
    }

    //Eliminar empleado por id
    public boolean deleteGeocerca(String idGeocerca){
        boolean delete = false;
        DbHelper dbHelper = new DbHelper(myContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " +TABLE_GEOCERCA + " WHERE idGeocerca= '"+idGeocerca+"'");
            delete=true;
        } catch (Exception ex){
            ex.toString();
        } finally {
            dbHelper.close();
        }

        return delete;
    }

    //Obtener el empleado por id
    public Geocerca getGeocerca(String idGeocerca){
        DbHelper dbHelper = new DbHelper(myContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Geocerca geocerca = null;
        Cursor cursorChecks = null;

        cursorChecks = db.rawQuery("SELECT * FROM " +TABLE_GEOCERCA+ " WHERE idGeocerca='"+idGeocerca+"'", null);
        if(cursorChecks.moveToFirst()){
            float latD = Float.parseFloat(cursorChecks.getString(6));
            float lngD = Float.parseFloat(cursorChecks.getString(7));
            float radioD = Float.parseFloat(cursorChecks.getString(8));
            long altaD = Long.parseLong(cursorChecks.getString(10));
            geocerca = new Geocerca();
            geocerca.setIdGeocerca(cursorChecks.getString(1));
            geocerca.setIdCompany(cursorChecks.getString(2));
            geocerca.setClave(cursorChecks.getString(3));
            geocerca.setGeoNombre(cursorChecks.getString(4));
            geocerca.setDescripcion(cursorChecks.getString(5));
            geocerca.setGeoLat(latD);
            geocerca.setGeoLong(lngD);
            geocerca.setRadio(radioD);
            geocerca.setDireccion(cursorChecks.getString(9));
            geocerca.setAlta(altaD);

                if(cursorChecks.getInt(11) == 1){
                    geocerca.setStatus(true);
                } else {
                    geocerca.setStatus(false);
                }
        }
        cursorChecks.close();
        dbHelper.close();
        //Collections.reverse(listChecks);
        return geocerca;
    }

    public ArrayList<Geocerca> getAllGeocercas(){
        DbHelper dbHelper = new DbHelper(myContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Geocerca> geocercas = new ArrayList<>();
        Geocerca geocerca = null;
        Cursor cursorGeocerca = null;

        cursorGeocerca = db.rawQuery("SELECT * FROM " + TABLE_GEOCERCA, null);

        if(cursorGeocerca.getCount() != 0){
            if(cursorGeocerca.moveToFirst()){
                do{
                    float latD = Float.parseFloat(cursorGeocerca.getString(6));
                    float lngD = Float.parseFloat(cursorGeocerca.getString(7));
                    float radioD = Float.parseFloat(cursorGeocerca.getString(8));
                    long altaD = Long.parseLong(cursorGeocerca.getString(10));
                    geocerca = new Geocerca();
                    geocerca.setIdGeocerca(cursorGeocerca.getString(1));
                    geocerca.setIdCompany(cursorGeocerca.getString(2));
                    geocerca.setClave(cursorGeocerca.getString(3));
                    geocerca.setGeoNombre(cursorGeocerca.getString(4));
                    geocerca.setDescripcion(cursorGeocerca.getString(5));
                    geocerca.setGeoLat(latD);
                    geocerca.setGeoLong(lngD);
                    geocerca.setRadio(radioD);
                    geocerca.setDireccion(cursorGeocerca.getString(9));
                    geocerca.setAlta(altaD);

                    if(cursorGeocerca.getInt(11) == 1){
                        geocerca.setStatus(true);
                    } else {
                        geocerca.setStatus(false);
                    }
                    geocercas.add(geocerca);
                } while (cursorGeocerca.moveToNext());
            }
        }
        cursorGeocerca.close();
        dbHelper.close();
        //Collections.reverse(listChecks);
        return geocercas;
    }

    //Eliminar todas las geocercas
    public boolean deleteAllGeocercas() {
        DbHelper dbHelper = new DbHelper(myContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean delete = false;
        try{
            db.execSQL("DELETE FROM " +TABLE_GEOCERCA+ "");
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
