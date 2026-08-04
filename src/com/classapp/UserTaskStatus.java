package com.classapp;

public class UserTaskStatus {
    private int idStatus;
    private TaskStatus status;
    private User user;
    private ClassTask task;

    public UserTaskStatus(User user, ClassTask task) {
        this.user = user;
        this.task = task;
    }

    public void changeStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskStatus getStatus() {return status;}
}