package com.classapp;

public class GroupTaskStatus {
    private int idStatus;
    private TaskStatus status;
    private GroupTask task;

    public GroupTaskStatus(GroupTask task) {
        this.task = task;
    }

    public void changeStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskStatus getStatus() {return status;}
}