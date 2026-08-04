package com.classapp;
import java.util.*;

public class Group {
    private int idClass;
    private int idGroup;
    private String groupName;

    private List<GroupTask> tasks;

    public Group(int idClass, int idGroup, String groupName) {
        this.idClass = idClass;
        this.idGroup = idGroup;
        this.groupName = groupName;
        this.tasks = new ArrayList<>();
    }

    public void setIdClass(int idClass) {
        this.idClass = idClass;
    }
    public void setIdGroup(int idGroup) {
        this.idGroup = idGroup;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public int getIdClass() {return idClass;}
    public int getIdUser() {return idGroup;}
    public String getGroupName() {return groupName;}

    // public void addMember(Membership user){
    //     memberships.add(user);
    // }

    public void createGroupTask(int idGroup, String title) {
        ClassTask taskBaru = new GroupTask(idGroup, title);
        tasks.add(taskBaru);
    }
    public void createClassTask(String title, String description) {
        ClassTask taskBaru = new ClassTaks(title, description);
        tasks.add(taskBaru);
    }



}