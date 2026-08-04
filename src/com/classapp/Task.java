package com.classapp;

public abstract class Task {
    private int idTask;
    private String title;
    private String description;

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }
    public Task(String title) {
        this.title = title;
        this.description = "";
    }

    public void setIdTask(int idTask) {this.idTask = idTask;}
    public void setTitle(String title) {this.title = title;}
    public void setDesc(String description) {this.description = description;}
    public void editTask(String title, String description) {
        this.title = title; this.description = description;
    } 

    public int getIdTask() {return idTask;}
    public String getTitle() {return title;}
    public String getDescription() {return description;}
}
