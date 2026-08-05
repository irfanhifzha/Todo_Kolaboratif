package com.classapp.dao;

import com.classapp.TaskStatus;
import com.classapp.dao.Rows.UserTaskStatusRow;
import com.classapp.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data-access object for the {@code user_task_status} table (tracks a user's progress on a ClassTask). */
public class UserTaskStatusDAO {

    public int insert(int idUser, int idTask, TaskStatus status) throws SQLException {
        String sql = "INSERT INTO user_task_status (id_user, id_task, status) VALUES (?, ?, ?)";
        Connection conn = Database.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idUser);
            ps.setInt(2, idTask);
            ps.setString(3, status.name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public void updateStatus(int idStatus, TaskStatus status) throws SQLException {
        String sql = "UPDATE user_task_status SET status = ? WHERE id_status = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, idStatus);
            ps.executeUpdate();
        }
    }

    public void delete(int idStatus) throws SQLException {
        String sql = "DELETE FROM user_task_status WHERE id_status = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idStatus);
            ps.executeUpdate();
        }
    }

    public record StatusInfo(int idStatus, TaskStatus status) {}

    public StatusInfo findByUserAndTask(int idUser, int idTask) throws SQLException {
        String sql = "SELECT id_status, status FROM user_task_status WHERE id_user = ? AND id_task = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUser);
            ps.setInt(2, idTask);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new StatusInfo(rs.getInt("id_status"), TaskStatus.valueOf(rs.getString("status")));
            }
        }
    }

    /** Inserts a new status row, or updates the existing one for this user+task. */
    public void upsert(int idUser, int idTask, TaskStatus status) throws SQLException {
        StatusInfo existing = findByUserAndTask(idUser, idTask);
        if (existing == null) {
            insert(idUser, idTask, status);
        } else {
            updateStatus(existing.idStatus(), status);
        }
    }

    public List<UserTaskStatusRow> findAll() throws SQLException {
        String sql = """
            SELECT s.id_status, s.id_user, u.name AS user_name,
                   s.id_task, t.title AS task_title, s.status
            FROM user_task_status s
            JOIN user u ON s.id_user = u.id_user
            JOIN class_task t ON s.id_task = t.id_task
            ORDER BY s.id_status
        """;
        List<UserTaskStatusRow> result = new ArrayList<>();
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new UserTaskStatusRow(
                        rs.getInt("id_status"), rs.getInt("id_user"), rs.getString("user_name"),
                        rs.getInt("id_task"), rs.getString("task_title"), rs.getString("status")));
            }
        }
        return result;
    }
}
