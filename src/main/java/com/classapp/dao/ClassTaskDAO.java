package com.classapp.dao;

import com.classapp.ClassRoom;
import com.classapp.ClassTask;
import com.classapp.dao.Rows.ClassTaskRow;
import com.classapp.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data-access object for the {@code class_task} table. */
public class ClassTaskDAO {

    public ClassTask insert(int idClass, String title, String description) throws SQLException {
        String sql = "INSERT INTO class_task (title, description, id_class) VALUES (?, ?, ?)";
        Connection conn = Database.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setInt(3, idClass);
            ps.executeUpdate();

            ClassRoom classRoom = new ClassRoom("");
            classRoom.setIdClass(idClass);
            ClassTask task = new ClassTask(classRoom, title, description == null ? "" : description);

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    task.setIdTask(keys.getInt(1));
                }
            }
            return task;
        }
    }

    public void update(int idTask, String title, String description, int idClass) throws SQLException {
        String sql = "UPDATE class_task SET title = ?, description = ?, id_class = ? WHERE id_task = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setInt(3, idClass);
            ps.setInt(4, idTask);
            ps.executeUpdate();
        }
    }

    public void delete(int idTask) throws SQLException {
        String sql = "DELETE FROM class_task WHERE id_task = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idTask);
            ps.executeUpdate();
        }
    }

    public List<ClassTaskRow> findAll() throws SQLException {
        String sql = """
            SELECT t.id_task, t.title, t.description, t.id_class, c.class_name
            FROM class_task t JOIN classroom c ON t.id_class = c.id_class
            ORDER BY t.id_task
        """;
        List<ClassTaskRow> result = new ArrayList<>();
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new ClassTaskRow(
                        rs.getInt("id_task"), rs.getString("title"), rs.getString("description"),
                        rs.getInt("id_class"), rs.getString("class_name")));
            }
        }
        return result;
    }

    public List<ClassTaskRow> findByClass(int idClass) throws SQLException {
        String sql = """
            SELECT t.id_task, t.title, t.description, t.id_class, c.class_name
            FROM class_task t JOIN classroom c ON t.id_class = c.id_class
            WHERE t.id_class = ?
            ORDER BY t.id_task
        """;
        List<ClassTaskRow> result = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idClass);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new ClassTaskRow(
                            rs.getInt("id_task"), rs.getString("title"), rs.getString("description"),
                            rs.getInt("id_class"), rs.getString("class_name")));
                }
            }
        }
        return result;
    }
}
