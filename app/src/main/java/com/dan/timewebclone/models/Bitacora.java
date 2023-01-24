package com.dan.timewebclone.models;

public class Bitacora {

    private String idBitacora, idGeocerca, idUser;
    private boolean stateActivated = false;

    public Bitacora(){}

    public Bitacora(String idBitacora, String idGeocerca, String idUser, boolean stateActivated) {
        this.idBitacora = idBitacora;
        this.idGeocerca = idGeocerca;
        this.idUser = idUser;
        this.stateActivated = stateActivated;
    }

    public String getIdBitacora() {
        return idBitacora;
    }

    public void setIdBitacora(String idBitacora) {
        this.idBitacora = idBitacora;
    }

    public String getIdGeocerca() {
        return idGeocerca;
    }

    public void setIdGeocerca(String idGeocerca) {
        this.idGeocerca = idGeocerca;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public boolean isStateActivated() {
        return stateActivated;
    }

    public void setStateActivated(boolean stateActivated) {
        this.stateActivated = stateActivated;
    }
}
