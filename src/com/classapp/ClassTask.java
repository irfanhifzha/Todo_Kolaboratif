package com.classapp;

public class ClassTask extends Task {
    private ClassRoom classRoom;

    public ClassTask(ClassRoom classRoom, String title, String description) {
        this.classRoom = classRoom;
        super(title, description);
    }

    public ClassTask(ClassRoom classRoom, String title) {
        this.classRoom = classRoom;
        super(title);
    }
}