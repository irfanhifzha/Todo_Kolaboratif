package com.classapp;

public class Membership {
    private int idMembership;
    private User user;
    private ClassRoom classRoom;
    private Group group;

    public Membership(User user, ClassRoom classRoom, Group group) {
        this.user = user;
        this.classRoom = classRoom;
        this.group = group;
    }
    public Membership(User user, ClassRoom classRoom) {
        this.user = user;
        this.classRoom = classRoom;
    }

    public void setIdMembership(int idMembership) {
        this.idMembership = idMembership;
    }
    public int getIdMembership() {return idMembership;}

    // kenapa tidak ada getter? dan terkadang tidak ada setter?
    // karna attr pada kelas ini sudah refence objek attr kelas lain

    public void setUser(User user) {
        this.user = user;
    }
    public void setClassRoom(ClassRoom classRoom) {
        this.classRoom = classRoom;
    }
    public void setGroup(Group group) {
        this.group = group;
    }   
}