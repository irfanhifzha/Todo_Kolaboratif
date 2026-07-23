package com.classapp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserTaskStatus {

    private int idStatus;
    private TaskStatus status;
    private final User user;
    private final ClassTask task;

    private UserTaskStatus(int idStatus, TaskStatus status, User user, ClassTask task) {
        this.idStatus = idStatus;
        this.status = status;
        this.user = user;
        this.task = task;
    }

    public void changeStatus(TaskStatus status) {
        String sql = "UPDATE user_task_status SET status = ? WHERE id = ?";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, idStatus);
            ps.executeUpdate();
            this.status = status;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public TaskStatus getStatus() { return status; }
    public User getUser() { return user; }
    public ClassTask getTask() { return task; }

    // ---------- persistence helper ----------

    /** Finds the existing status row for this (user, task) pair, or creates one defaulting to TODO. */
    static UserTaskStatus findOrCreate(User user, ClassTask task) {
        String selectSql = "SELECT id, status FROM user_task_status WHERE user_id = ? AND task_id = ?";
        try (PreparedStatement ps = Database.get().prepareStatement(selectSql)) {
            ps.setInt(1, user.getIdUser());
            ps.setInt(2, task.getIdTask());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserTaskStatus(rs.getInt("id"), TaskStatus.valueOf(rs.getString("status")), user, task);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String insertSql = "INSERT INTO user_task_status (status, user_id, task_id) VALUES ('TODO', ?, ?)";
        try (PreparedStatement ps = Database.get().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, user.getIdUser());
            ps.setInt(2, task.getIdTask());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new UserTaskStatus(keys.getInt(1), TaskStatus.TODO, user, task);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
