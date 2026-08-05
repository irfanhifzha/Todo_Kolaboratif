package com.classapp;

public class GroupTask extends Task {
    private Group group;

    public GroupTask(Group group, String title, String description) {
        super(title, description);
        this.group = group;
    }

    public GroupTask(Group group, String title) {
        super(title);
        this.group = group;
    }
}