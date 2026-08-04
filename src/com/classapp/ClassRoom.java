package com.classapp;
import java.util.*;

public class ClassRoom {
    private int idClass;
    private String className;
    // private List<Membership> memberships;  -> harusnya hanya ada di membership, cause that the point of that class.
    private List<Group> groups;
    private List<ClassTask> tasks;

    public ClassRoom(int idClass, String className) {
        this.idClass = idClass;
        this.className = className;
        // this.memberships = new ArrayList<>();
        this.groups = new ArrayList<>();
        this.tasks = new ArrayList<>();
    }

    public void setIdClass(int idClass) {
        this.idClass = idClass;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public int getIdClass() {return idClass;}
    public String getClassName() {return className;}

    // public void addMember(Membership user){
    //     memberships.add(user);
    // }

    public void createGroup(int idClass, String groupName) {
        Group groupBaru = new Group(idClass, groupName);
        groups.add(groupBaru);
    }
    public void createClassTask(int idClass, String title) {
        ClassTask taskBaru = new ClassTask(idClass, title);
        tasks.add(taskBaru);
    }
    public void createClassTask(String title, String description) {
        ClassTask taskBaru = new ClassTaks(title, description);
        tasks.add(taskBaru);
    }



}