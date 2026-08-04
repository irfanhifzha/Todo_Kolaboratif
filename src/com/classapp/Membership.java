package com.classapp;
import java.util.*;

public class Membership {
    private int idMembership;
    private int idUser;
    // private User user;
    private ClassRoom classRoom;
    private Group group;

    public Membership(int idMembership, int idUser, ClassRoom classRoom, Group group) {
        this.idMembership = idMembership;
        this.idUser = idUser;
        this.classRoom = classRoom;
        this.group = group;
    }
    public Membership(int idMembership, int idUser, ClassRoom classRoom) {
        this.idMembership = idMembership;
        this.idUser = idUser;
        this.classRoom = classRoom;
    }

    // public Membership(int idMembership, User user) {
    //     this.idMembership = idMembership;
    //     this.user = user;
    //     this.classRoom = new ClassRoom();
    //     this.group = new Group();
    // }

    public void setIdMembership(int idMembership) {
        this.idMembership = idMembership;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public void assignClassRoom(ClassRoom classRoom) {
        this.classRoom = ClassRoom;
    }
    public void assignGroup(Group group) {
        this.group = Group;
    }

    public int getIdMembership() {return idMembership;}
    // public User getUser() {return User;} 
    // public String getIdUser() {return user.getIdUser()};
    // -> ngapain ada getter idUser()?? ga bakal select membership satu ini, nanti juga bakal SELECT * WHERE user = ?
    // public ClassRoom getClassRoom() {return ClassRoom;}
    // public Group getGroup() {return Group;}
    
}