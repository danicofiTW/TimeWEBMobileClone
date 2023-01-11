package com.dan.timewebclone.models;

public class Employee {

    private String name;
    private String claveUser;
    private String company;
    private String rfcCompany;
    private String email;
    private String phone;
    private String password;
    private String token;
    private String idUser;
    private String idCompany;
    private String departamento;
    private String image;
    private String url;
    private String state;



    public Employee() {

    }



    public Employee(String name, String claveUser, String rfcCompany, String email, String phone, String password) {
        this.name = name;
        this.claveUser = claveUser;
        this.rfcCompany = rfcCompany;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public Employee(String name, String claveUser, String rfcCompany, String email, String phone, String password, String idUser) {
        this.name = name;
        this.claveUser = claveUser;
        this.rfcCompany = rfcCompany;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.idUser = idUser;
    }

    public Employee(String name, String claveUser,String company, String rfcCompany, String email, String phone, String password, String idUser, String idEmpresa, String departamento, String image, String url, String token,  String state) {
        this.name = name;
        this.claveUser = claveUser;
        this.rfcCompany = rfcCompany;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.token = token;
        this.idUser = idUser;
        this.idCompany = idEmpresa;
        this.departamento = departamento;
        this.image = image;
        this.url = url;
        this.state = state;
        this.company = company;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClaveUser() {
        return claveUser;
    }

    public void setClaveUser(String claveUser) {
        this.claveUser = claveUser;
    }

    public String getRfcCompany() {
        return rfcCompany;
    }

    public void setRfcCompany(String rfcCompany) {
        this.rfcCompany = rfcCompany;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getIdEmpresa() {
        return idCompany;
    }

    public void setIdEmpresa(String idEmpresa) {
        this.idCompany = idEmpresa;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
