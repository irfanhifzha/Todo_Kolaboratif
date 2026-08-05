package com.classapp;
import java.util.*;

public class Group {
    private int idGroup;
    private String groupName;
    private ClassRoom classRoom;
    private List<GroupTask> tasks;

    public Group(ClassRoom classRoom, String groupName) {
        this.classRoom = classRoom;
        this.groupName = groupName;
        this.tasks = new ArrayList<>();
    }

    public void setIdGroup(int idGroup) {
        this.idGroup = idGroup;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public int getIdGroup() {return idGroup;}
    public String getGroupName() {return groupName;}

    public void createClassTask(Group group, String title, String description) {
        GroupTask taskBaru = new GroupTask(group, title, description);
        tasks.add(taskBaru);
    }
    public void createGroupTask(Group group, String title) {
        GroupTask taskBaru = new GroupTask(group, title);
        tasks.add(taskBaru);
    }
}