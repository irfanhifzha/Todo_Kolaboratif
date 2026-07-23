package com.classapp;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Task {

    protected int idTask;
    protected String title;
    protected String description;

    protected Task(String title, String description) {
        this.title = title;
        this.description = description;
    }

    /** The table this concrete task type is stored in - used by editTask/deleteTask below. */
    protected abstract String tableName();

    public void editTask(String title, String description) {
        String sql = "UPDATE " + tableName() + " SET title = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setInt(3, idTask);
            ps.executeUpdate();
            this.title = title;
            this.description = description;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteTask() {
        String sql = "DELETE FROM " + tableName() + " WHERE id = ?";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setInt(1, idTask);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getIdTask() { return idTask; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
