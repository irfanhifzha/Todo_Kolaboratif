package com.classapp;

public class ClassTask extends Task {
    private ClassRoom classRoom;

    public ClassTask(ClassRoom classRoom, String title, String description) {
        super(title, description);
        this.classRoom = classRoom;
    }

    public ClassTask(ClassRoom classRoom, String title) {
        super(title);
        this.classRoom = classRoom;
    }
}