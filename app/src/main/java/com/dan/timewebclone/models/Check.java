package com.dan.timewebclone.models;

import android.graphics.Bitmap;

public class Check {

    private String idCheck;
    private String idUser;
    private String idCompany;
    private String tipeCheck;
    private String urlImage;
    private String image;
    private Long time;
    private double checkLat;
    private double checkLong;
    private int statusSend;
    private boolean isDelete = false;

    public Check(){
    }

    public Check(String idCheck, String idUser, String idCompany, String tipeCheck, String urlImage, String image, Long time, double checkLat, double checkLong, int statusSend) {
        this.idCheck = idCheck;
        this.idUser = idUser;
        this.idCompany = idCompany;
        this.tipeCheck = tipeCheck;
        this.urlImage = urlImage;
        this.image = image;
        this.time = time;
        this.checkLat = checkLat;
        this.checkLong = checkLong;
        this.statusSend = statusSend;
    }

    public Check(String idCheck, String idUser, String idCompany, String tipeCheck, String urlImage, String image, Long time, double checkLat, double checkLong, int statusSend, boolean isDelete) {
        this.idCheck = idCheck;
        this.idUser = idUser;
        this.idCompany = idCompany;
        this.tipeCheck = tipeCheck;
        this.urlImage = urlImage;
        this.image = image;
        this.time = time;
        this.checkLat = checkLat;
        this.checkLong = checkLong;
        this.statusSend = statusSend;
        this.isDelete = isDelete;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getIdCheck() {
        return idCheck;
    }

    public void setIdCheck(String idCheck) {
        this.idCheck = idCheck;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getIdCompany() {
        return idCompany;
    }

    public void setIdCompany(String idCompany) {
        this.idCompany = idCompany;
    }

    public String getTipeCheck() {
        return tipeCheck;
    }

    public void setTipeCheck(String tipeCheck) {
        this.tipeCheck = tipeCheck;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public double getCheckLat() {
        return checkLat;
    }

    public void setCheckLat(double checkLat) {
        this.checkLat = checkLat;
    }

    public double getCheckLong() {
        return checkLong;
    }

    public void setCheckLong(double checkLong) {
        this.checkLong = checkLong;
    }

    public int getStatusSend() {
        return statusSend;
    }

    public void setStatusSend(int statusSend) {
        this.statusSend = statusSend;
    }
}
