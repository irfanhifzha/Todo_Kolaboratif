package com.classapp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GroupTaskStatus {

    private int idStatus;
    private TaskStatus status;
    private final GroupTask task;

    private GroupTaskStatus(int idStatus, TaskStatus status, GroupTask task) {
        this.idStatus = idStatus;
        this.status = status;
        this.task = task;
    }

    public void changeStatus(TaskStatus status) {
        String sql = "UPDATE group_task_status SET status = ? WHERE id = ?";
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
    public GroupTask getTask() { return task; }

    // ---------- persistence helper ----------

    /** Finds the (always exactly one) status row for this group task, or creates one defaulting to TODO. */
    static GroupTaskStatus findOrCreate(GroupTask task) {
        String selectSql = "SELECT id, status FROM group_task_status WHERE task_id = ?";
        try (PreparedStatement ps = Database.get().prepareStatement(selectSql)) {
            ps.setInt(1, task.getIdTask());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new GroupTaskStatus(rs.getInt("id"), TaskStatus.valueOf(rs.getString("status")), task);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String insertSql = "INSERT INTO group_task_status (status, task_id) VALUES ('TODO', ?)";
        try (PreparedStatement ps = Database.get().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, task.getIdTask());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new GroupTaskStatus(keys.getInt(1), TaskStatus.TODO, task);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
