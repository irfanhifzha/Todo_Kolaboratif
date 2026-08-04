package com.classapp;
// import java.util.*;

public class User {
    private int idUser;
    private String name;
    private String username;
    private String password;

    // private List<Membership> memberships; -> doesnt makes sense, karna user ga perlu tau membership. hanya membership tau user sehingga ada koneksi dot product thingy

    public User(int idUser, String name, String username, String password) {
        this.idUser = idUser; this.name = name; 
        this.username = username; this.password = password;
        // this.memberships = new ArrayList<>(); // init kosong dulu
    }
    
    public void setIdUser(int idUser) {this.idUser = idUser;}
    public void setName(String name) {this.name = name;}
    public void setUsername(String username) {this.username = username;}
    public void setPassword(String password) {this.password = password;}
    
    public int getIdUser() {return idUser;}
    public String getName() {return name;}
    public String getUsername() {return username;}
    public String getPassword() {return password;}

    // public void addMembership(Membership membership) {
    //     memberships.add(membership);
    // }

    // public void removeMembership(Membership membership) {
    //     memberships.remove(membership);
    // }

}