package com.classapp.dao;

import com.classapp.TaskStatus;
import com.classapp.dao.Rows.GroupTaskStatusRow;
import com.classapp.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data-access object for the {@code group_task_status} table (tracks a group's progress on a GroupTask). */
public class GroupTaskStatusDAO {

    public int insert(int idGroupTask, TaskStatus status) throws SQLException {
        String sql = "INSERT INTO group_task_status (id_group_task, status) VALUES (?, ?)";
        Connection conn = Database.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idGroupTask);
            ps.setString(2, status.name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public void updateStatus(int idStatus, TaskStatus status) throws SQLException {
        String sql = "UPDATE group_task_status SET status = ? WHERE id_status = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, idStatus);
            ps.executeUpdate();
        }
    }

    public void delete(int idStatus) throws SQLException {
        String sql = "DELETE FROM group_task_status WHERE id_status = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idStatus);
            ps.executeUpdate();
        }
    }

    public record StatusInfo(int idStatus, TaskStatus status) {}

    public StatusInfo findByGroupTask(int idGroupTask) throws SQLException {
        String sql = "SELECT id_status, status FROM group_task_status WHERE id_group_task = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idGroupTask);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new StatusInfo(rs.getInt("id_status"), TaskStatus.valueOf(rs.getString("status")));
            }
        }
    }

    /** Inserts a new status row for this group task, or updates the existing one (one status per group task). */
    public void upsert(int idGroupTask, TaskStatus status) throws SQLException {
        StatusInfo existing = findByGroupTask(idGroupTask);
        if (existing == null) {
            insert(idGroupTask, status);
        } else {
            updateStatus(existing.idStatus(), status);
        }
    }

    public List<GroupTaskStatusRow> findAll() throws SQLException {
        String sql = """
            SELECT s.id_status, s.id_group_task, t.title AS task_title, s.status
            FROM group_task_status s
            JOIN group_task t ON s.id_group_task = t.id_task
            ORDER BY s.id_status
        """;
        List<GroupTaskStatusRow> result = new ArrayList<>();
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new GroupTaskStatusRow(
                        rs.getInt("id_status"), rs.getInt("id_group_task"),
                        rs.getString("task_title"), rs.getString("status")));
            }
        }
        return result;
    }
}
