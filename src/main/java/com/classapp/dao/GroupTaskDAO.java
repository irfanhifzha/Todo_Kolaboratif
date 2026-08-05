package com.classapp.dao;

import com.classapp.ClassRoom;
import com.classapp.Group;
import com.classapp.GroupTask;
import com.classapp.dao.Rows.GroupTaskRow;
import com.classapp.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data-access object for the {@code group_task} table. */
public class GroupTaskDAO {

    public GroupTask insert(int idGroup, String title, String description) throws SQLException {
        String sql = "INSERT INTO group_task (title, description, id_group) VALUES (?, ?, ?)";
        Connection conn = Database.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setInt(3, idGroup);
            ps.executeUpdate();

            Group group = new Group(new ClassRoom(""), "");
            group.setIdGroup(idGroup);
            GroupTask task = new GroupTask(group, title, description == null ? "" : description);

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    task.setIdTask(keys.getInt(1));
                }
            }
            return task;
        }
    }

    public void update(int idTask, String title, String description, int idGroup) throws SQLException {
        String sql = "UPDATE group_task SET title = ?, description = ?, id_group = ? WHERE id_task = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setInt(3, idGroup);
            ps.setInt(4, idTask);
            ps.executeUpdate();
        }
    }

    public void delete(int idTask) throws SQLException {
        String sql = "DELETE FROM group_task WHERE id_task = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idTask);
            ps.executeUpdate();
        }
    }

    public List<GroupTaskRow> findByGroup(int idGroup) throws SQLException {
        String sql = """
            SELECT t.id_task, t.title, t.description, t.id_group, g.group_name
            FROM group_task t JOIN group_table g ON t.id_group = g.id_group
            WHERE t.id_group = ?
            ORDER BY t.id_task
        """;
        List<GroupTaskRow> result = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idGroup);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new GroupTaskRow(
                            rs.getInt("id_task"), rs.getString("title"), rs.getString("description"),
                            rs.getInt("id_group"), rs.getString("group_name")));
                }
            }
        }
        return result;
    }

    public List<GroupTaskRow> findAll() throws SQLException {
        String sql = """
            SELECT t.id_task, t.title, t.description, t.id_group, g.group_name
            FROM group_task t JOIN group_table g ON t.id_group = g.id_group
            ORDER BY t.id_task
        """;
        List<GroupTaskRow> result = new ArrayList<>();
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new GroupTaskRow(
                        rs.getInt("id_task"), rs.getString("title"), rs.getString("description"),
                        rs.getInt("id_group"), rs.getString("group_name")));
            }
        }
        return result;
    }
}
