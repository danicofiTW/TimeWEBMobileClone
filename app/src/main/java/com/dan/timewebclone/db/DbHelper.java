package com.dan.timewebclone.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {

    Context myContext;

    private static final int DATABASE_VERSION=16;
    private static final String DATABASE_NAME="timeWEBMobile.db";
    public static final String TABLE_CHECKS="checks";
    public static final String TABLE_EMPLOYEES="employees";
    public static final String TABLE_GEOCERCA="geocercas";
    public static final String TABLE_BITACORA="bitacora";


    public DbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Crear tablas
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_EMPLOYEES + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "idUser TEXT NOT NULL," +
                "name TEXT NOT NULL," +
                "claveUser TEXT NOT NULL," +
                "company TEXT," +
                "rfcCompany TEXT NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "phone TEXT NOT NULL," +
                "password TEXT NOT NULL," +
                "token TEXT," +
                "idCompany TEXT," +
                "image TEXT," +
                "url TEXT," +
                "stateCamera INTEGER," +
                "department TEXT," +
                "stateBiometrics INTEGER)");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_CHECKS + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "idCheck TEXT," +
                "idUser TEXT NOT NULL," +
                "idCompany TEXT," +
                "tipeCheck TEXT NOT NULL," +
                "urlImage TEXT," +
                "image TEXT," +
                "date TEXT NOT NULL," +
                "checkLat TEXT NOT NULL," +
                "checkLong TEXT NOT NULL," +
                "isDelete TEXT NOT NULL," +
                "statusSend INTEGER NOT NULL," +
                "dateSend TEXT NOT NULL,"+
                "idGeocerca TEXT,"+
                "nameGeocerca TEXT)");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_GEOCERCA + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "idGeocerca TEXT NOT NULL," +
                "idCompany TEXT," +
                "clave TEXT NOT NULL," +
                "geoNombre TEXT," +
                "descripcion TEXT," +
                "geoLatitud TEXT NOT NULL," +
                "geoLongitud TEXT NOT NULL," +
                "radio TEXT NOT NULL," +
                "direccion TEXT NOT NULL," +
                "alta TEXT," +
                "status INTEGER NOT NULL)");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_BITACORA + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "idBitacora TEXT NOT NULL," +
                "idGeocerca TEXT NOT NULL," +
                "idUser TEXT NOT NULL," +
                "stateActivated INTEGER)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //eliminarTabla
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CHECKS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_BITACORA);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_GEOCERCA);
        onCreate(sqLiteDatabase);
    }
}
