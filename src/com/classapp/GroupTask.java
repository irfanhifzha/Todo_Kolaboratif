package com.classapp;

public class GroupTask extends Task {
    private Group group;

    public GroupTask(Group group, String title, String description) {
        this.group = group;
        super(title, description);
    }

    public GroupTask(Group group, String title) {
        this.group = group;
        super(title);
    }
}