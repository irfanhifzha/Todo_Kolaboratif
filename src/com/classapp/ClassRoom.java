package com.classapp;
import java.util.*;

public class ClassRoom {
    private int idClass;
    private String className;
    private List<Group> groups;
    private List<ClassTask> tasks;

    public ClassRoom(String className) {
        this.className = className;
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

    public void createGroup(ClassRoom classRoom, String groupName) {
        Group groupBaru = new Group(classRoom, groupName);
        groups.add(groupBaru);
    }
    public void createClassTask(ClassRoom classRoom, String title, String description) {
        ClassTask taskBaru = new ClassTask(classRoom, title, description);
        tasks.add(taskBaru);
    }
    public void createClassTask(ClassRoom classRoom, String title) {
        ClassTask taskBaru = new ClassTask(classRoom, title);
        tasks.add(taskBaru);
    }
}