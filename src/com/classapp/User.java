package com.classapp;

public class User {
    private int idUser;
    private String name;
    private String username;
    private String password;

    public User(int idUser, String name, String username, String password) {
        this.name = name; this.username = username; this.password = password;
    }
    
    public void setIdUser(int idUser) {this.idUser = idUser;}
    public void setName(String name) {this.name = name;}
    public void setUsername(String username) {this.username = username;}
    public void setPassword(String password) {this.password = password;}
    
    public int getIdUser() {return idUser;}
    public String getName() {return name;}
    public String getUsername() {return username;}
    public String getPassword() {return password;}
}