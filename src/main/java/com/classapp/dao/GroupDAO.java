package com.classapp.dao;

import com.classapp.ClassRoom;
import com.classapp.Group;
import com.classapp.dao.Rows.GroupRow;
import com.classapp.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access object for the {@code group_table} table.
 *
 * Group's own createGroup(...) convenience method on ClassRoom is not used here
 * (it NPEs, since ClassRoom never initializes its "groups" list) - instead a
 * Group is built directly with {@code new Group(classRoom, groupName)} and
 * persisted explicitly.
 */
public class GroupDAO {

    public Group insert(int idClass, String groupName) throws SQLException {
        String sql = "INSERT INTO group_table (group_name, id_class) VALUES (?, ?)";
        Connection conn = Database.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, groupName);
            ps.setInt(2, idClass);
            ps.executeUpdate();

            ClassRoom classRoom = new ClassRoom("");
            classRoom.setIdClass(idClass);
            Group group = new Group(classRoom, groupName);

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    group.setIdGroup(keys.getInt(1));
                }
            }
            return group;
        }
    }

    public void update(int idGroup, String groupName, int idClass) throws SQLException {
        String sql = "UPDATE group_table SET group_name = ?, id_class = ? WHERE id_group = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, groupName);
            ps.setInt(2, idClass);
            ps.setInt(3, idGroup);
            ps.executeUpdate();
        }
    }

    public void delete(int idGroup) throws SQLException {
        String sql = "DELETE FROM group_table WHERE id_group = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idGroup);
            ps.executeUpdate();
        }
    }

    public GroupRow findById(int idGroup) throws SQLException {
        String sql = """
            SELECT g.id_group, g.group_name, g.id_class, c.class_name
            FROM group_table g JOIN classroom c ON g.id_class = c.id_class
            WHERE g.id_group = ?
        """;
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idGroup);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new GroupRow(rs.getInt("id_group"), rs.getString("group_name"),
                        rs.getInt("id_class"), rs.getString("class_name"));
            }
        }
    }

    public List<GroupRow> findAll() throws SQLException {
        String sql = """
            SELECT g.id_group, g.group_name, g.id_class, c.class_name
            FROM group_table g JOIN classroom c ON g.id_class = c.id_class
            ORDER BY g.id_group
        """;
        List<GroupRow> result = new ArrayList<>();
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new GroupRow(
                        rs.getInt("id_group"), rs.getString("group_name"),
                        rs.getInt("id_class"), rs.getString("class_name")));
            }
        }
        return result;
    }

    public List<GroupRow> findByClass(int idClass) throws SQLException {
        String sql = """
            SELECT g.id_group, g.group_name, g.id_class, c.class_name
            FROM group_table g JOIN classroom c ON g.id_class = c.id_class
            WHERE g.id_class = ?
            ORDER BY g.id_group
        """;
        List<GroupRow> result = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idClass);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new GroupRow(
                            rs.getInt("id_group"), rs.getString("group_name"),
                            rs.getInt("id_class"), rs.getString("class_name")));
                }
            }
        }
        return result;
    }
}
