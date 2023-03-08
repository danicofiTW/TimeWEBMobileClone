package com.dan.timewebclone.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.dan.timewebclone.models.Check;
import com.dan.timewebclone.models.Employee;

import java.util.ArrayList;
import java.util.Collections;

public class DbEmployees extends DbHelper{
    Context myContext;
    //Employee employee;
    public DbEmployees(@Nullable Context context) {
        super(context);
        myContext = context;
    }

    //Insertar empleado en SQLite
    public long insertEmployye(Employee employee){
        long id = 0;
        DbHelper dbHelper = new DbHelper(myContext);
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("name", employee.getName());
            values.put("claveUser", employee.getClaveUser());
            values.put("rfcCompany", employee.getRfcCompany());
            values.put("email", employee.getEmail());
            values.put("phone", employee.getPhone());
            values.put("password", employee.getPassword());
            values.put("idUser", employee.getIdUser());
            values.put("company", employee.getCompany());
            values.put("company", employee.getCompany());
            if(employee.isStateCamera()){
                values.put("stateCamera", 1);
            } else{
                values.put("stateCamera", 0);
            }
            if(employee.isStateBiometrics()){
                values.put("stateBiometrics", 1);
            } else {
                values.put("stateBiometrics", 0);
            }
            if(employee.getImage()!=null){
                values.put("image", employee.getImage());
            }
            if(employee.getToken()!=null){
                values.put("token", employee.getToken());
            }
            //values.put("idCompany", employee.getCompany());
            id = db.insert(TABLE_EMPLOYEES, null, values);
        } catch (Exception ex){
            ex.toString();
        } finally {
            dbHelper.close();
        }

        return id;
    }

    /*public boolean updateEmployee(boolean isDelete, String idCheck){
        DbHelper dbHelper = new DbHelper(myContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ArrayList<Check> listChecks = new ArrayList<>();
        Employee employee = null;
        Cursor cursorChecks = null;
        boolean update = false;

        try{
            //db.execSQL("UPDATE " + TABLE_CHECKS + " SET statusSend = '"+tipeSendCheck+"'WHERE statusSend = '0'");
            db.execSQL("UPDATE " + TTABLE_EMPLOYEES + " SET isDelete = '"+dateS+"'WHERE idCheck = '"+idCheck+"'");
            update = true;
        } catch (Exception ex){
            ex.toString();
            update = false;
        } finally {
            dbChecks.close();
        }

        return update;
    }*/

    //Eliminar empleado por id
    public boolean deleteEmployee(String idUser){
        boolean delete = false;

        DbHelper dbHelper = new DbHelper(myContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " +TABLE_EMPLOYEES + " WHERE idUser= '"+idUser+"'");
            delete=true;
        } catch (Exception ex){
            ex.toString();
        } finally {
            dbHelper.close();
        }

        return delete;
    }

    //Obtener el empleado por id
    public Employee getEmployee(String idUser){
        DbHelper dbEmploye = new DbHelper(myContext);
        SQLiteDatabase db = dbEmploye.getWritableDatabase();

        Employee employee = null;
        Cursor cursorChecks = null;

        cursorChecks = db.rawQuery("SELECT * FROM " +TABLE_EMPLOYEES+ " WHERE idUser='"+idUser+"'", null);
        if(cursorChecks.moveToFirst()){
            employee = new Employee();
            employee.setName(cursorChecks.getString(2));
            employee.setIdUser(cursorChecks.getString(1));
            employee.setCompany(cursorChecks.getString(4));
            employee.setPhone(cursorChecks.getString(7));
            employee.setEmail(cursorChecks.getString(6));
            employee.setPassword(cursorChecks.getString(8));
            employee.setToken(cursorChecks.getString(9));
            employee.setImage(cursorChecks.getString(11));
            if(cursorChecks.getInt(13) == 1){
                employee.setStateCamera(true);
            } else {
                employee.setStateCamera(false);
            }
            if(cursorChecks.getInt(15) == 1){
                employee.setStateBiometrics(true);
            } else {
                employee.setStateBiometrics(false);
            }
        }
        cursorChecks.close();
        dbEmploye.close();
        //Collections.reverse(listChecks);
        return employee;
    }

    //Actualizar nombre de usuario
    public boolean updateName(String idUser, String name){
        boolean up = false;
        DbHelper dbEmployee = new DbHelper(myContext);
        SQLiteDatabase db = dbEmployee.getWritableDatabase();
        Employee employee = null;
        Cursor cursorChecks = null;
        //String dateS = String.valueOf(date);

        try{
            db.execSQL("UPDATE " + TABLE_EMPLOYEES + " SET name = '"+name+"'WHERE idUser = '"+idUser+"'");
            up = true;
        } catch (Exception ex){
            ex.toString();
            up = false;
        } finally {
            dbEmployee.close();
        }
        return up;
    }

    //Actualizar compania
    public boolean updateCompany(String idUser, String company){
        boolean save = false;
        DbHelper dbEmployee = new DbHelper(myContext);
        SQLiteDatabase db = dbEmployee.getWritableDatabase();
        Employee employee = null;
        Cursor cursorChecks = null;
        //String dateS = String.valueOf(date);

        try{
            db.execSQL("UPDATE " + TABLE_EMPLOYEES + " SET company = '"+company+"'WHERE idUser = '"+idUser+"'");
            save = true;
        } catch (Exception ex){
            ex.toString();
            save = false;
        } finally {
            dbEmployee.close();
        }
        return save;
    }

    //Actualizar password
    public boolean updatePassword(String idUser, String password){
        boolean save = false;
        DbHelper dbEmployee = new DbHelper(myContext);
        SQLiteDatabase db = dbEmployee.getWritableDatabase();
        //Employee employee = null;
        //Cursor cursorChecks = null;
        //String dateS = String.valueOf(date);

        try{
            db.execSQL("UPDATE " + TABLE_EMPLOYEES + " SET password = '"+password+"'WHERE idUser = '"+idUser+"'");
            save = true;
        } catch (Exception ex){
            ex.toString();
            save = false;
        } finally {
            dbEmployee.close();
        }
        return save;
    }

    public boolean updateTokenDB(String idUser, String token){
        boolean save = false;
        DbHelper dbEmployee = new DbHelper(myContext);
        SQLiteDatabase db = dbEmployee.getWritableDatabase();
        try{
            db.execSQL("UPDATE " + TABLE_EMPLOYEES + " SET token = '"+token+"'WHERE idUser = '"+idUser+"'");
            save = true;
        } catch (Exception ex){
            ex.toString();
            save = false;
        } finally {
            dbEmployee.close();
        }
        return save;
    }

    //Actualizar numero telefonico
    public boolean updatePhone(String idUser, String phone){
        boolean save = false;
        DbHelper dbEmployee = new DbHelper(myContext);
        SQLiteDatabase db = dbEmployee.getWritableDatabase();
        Employee employee = null;
        Cursor cursorChecks = null;
        //String dateS = String.valueOf(date);

        try{
            db.execSQL("UPDATE " + TABLE_EMPLOYEES + " SET phone = '"+phone+"'WHERE idUser = '"+idUser+"'");
            save = true;
        } catch (Exception ex){
            ex.toString();
            save = false;
        } finally {
            dbEmployee.close();
        }
        return save;
    }

    //Actualizar configuracion de la camara
    public boolean updateStateCamera(String idUser, boolean stateCamera){
        boolean save = false;
        DbHelper dbEmployee = new DbHelper(myContext);
        SQLiteDatabase db = dbEmployee.getWritableDatabase();

        try{
            if(stateCamera){
                db.execSQL("UPDATE " + TABLE_EMPLOYEES + " SET stateCamera = '"+1+"'WHERE idUser = '"+idUser+"'");
            } else{
                db.execSQL("UPDATE " + TABLE_EMPLOYEES + " SET stateCamera = '"+0+"'WHERE idUser = '"+idUser+"'");
            }
            save = true;
        } catch (Exception ex){
            ex.toString();
            save = false;
        } finally {
            dbEmployee.close();
        }
        return save;
    }

    public boolean updateStateBiometrics(String idUser, boolean stateBiometrics){
        boolean save = false;
        DbHelper dbEmployee = new DbHelper(myContext);
        SQLiteDatabase db = dbEmployee.getWritableDatabase();
        //String dateS = String.valueOf(date);

        try{
            if(stateBiometrics){
                db.execSQL("UPDATE " + TABLE_EMPLOYEES + " SET stateBiometrics = '"+1+"'WHERE idUser = '"+idUser+"'");
            } else{
                db.execSQL("UPDATE " + TABLE_EMPLOYEES + " SET stateBiometrics = '"+0+"'WHERE idUser = '"+idUser+"'");
            }
            save = true;
        } catch (Exception ex){
            ex.toString();
            save = false;
        } finally {
            dbEmployee.close();
        }
        return save;
    }

    //Eliminar todos los empleados
    public boolean deleteAllEmployees() {
        DbHelper dbEmployee = new DbHelper(myContext);
        SQLiteDatabase db = dbEmployee.getWritableDatabase();
        boolean delete = false;
        try{
            db.execSQL("DELETE FROM " +TABLE_EMPLOYEES+ "");
            delete = true;
        } catch (Exception ex){
            ex.toString();
            delete = false;
        } finally {
            dbEmployee.close();
        }
        return delete;
    }

    //Guardar Imagen
    public boolean saveImage(String idUser, String image){
        boolean save = false;
        DbHelper dbEmployee = new DbHelper(myContext);
        SQLiteDatabase db = dbEmployee.getWritableDatabase();
        Employee employee = null;
        Cursor cursorChecks = null;
        //String dateS = String.valueOf(date);

        try{
            db.execSQL("UPDATE " + TABLE_EMPLOYEES + " SET image = '"+image+"'WHERE idUser = '"+idUser+"'");
            save = true;
        } catch (Exception ex){
            ex.toString();
            save = false;
        } finally {
            dbEmployee.close();
        }
        return save;
    }

}
