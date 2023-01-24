package com.dan.timewebclone.models;

public class Geocerca {

    private String idGeocerca, idCompany, clave, geoNombre, descripcion, direccion;
    private float geoLat;
    private float geoLong;
    private float radio;
    private long alta;
    private boolean status = false;

    public Geocerca(){}

    public Geocerca(String idGeocerca, String idCompany, String clave, String geoNombre, String descripcion, String direccion, float geoLat, float geoLong, float radio, long alta, boolean status) {
        this.idGeocerca = idGeocerca;
        this.idCompany = idCompany;
        this.clave = clave;
        this.geoNombre = geoNombre;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.geoLat = geoLat;
        this.geoLong = geoLong;
        this.radio = radio;
        this.alta = alta;
        this.status = status;
    }

    public String getIdGeocerca() {
        return idGeocerca;
    }

    public void setIdGeocerca(String idGeocerca) {
        this.idGeocerca = idGeocerca;
    }

    public String getIdCompany() {
        return idCompany;
    }

    public void setIdCompany(String idCompany) {
        this.idCompany = idCompany;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getGeoNombre() {
        return geoNombre;
    }

    public void setGeoNombre(String geoNombre) {
        this.geoNombre = geoNombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public float getGeoLat() {
        return geoLat;
    }

    public void setGeoLat(float geoLat) {
        this.geoLat = geoLat;
    }

    public float getGeoLong() {
        return geoLong;
    }

    public void setGeoLong(float geoLong) {
        this.geoLong = geoLong;
    }

    public float getRadio() {
        return radio;
    }

    public void setRadio(float radio) {
        this.radio = radio;
    }

    public long getAlta() {
        return alta;
    }

    public void setAlta(long alta) {
        this.alta = alta;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
