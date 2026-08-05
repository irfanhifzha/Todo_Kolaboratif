package com.classapp.dao;

import com.classapp.User;
import com.classapp.dao.Rows.MembershipRow;
import com.classapp.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access object for the {@code membership} table.
 *
 * Membership's own getters for user/classRoom/group were intentionally left
 * out of the POJO, so this DAO works with plain ids and returns joined
 * {@link MembershipRow} records for display instead of Membership objects.
 */
public class MembershipDAO {

    public int insert(int idUser, int idClass, Integer idGroup) throws SQLException {
        String sql = "INSERT INTO membership (id_user, id_class, id_group) VALUES (?, ?, ?)";
        Connection conn = Database.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idUser);
            ps.setInt(2, idClass);
            if (idGroup == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, idGroup);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public void update(int idMembership, int idUser, int idClass, Integer idGroup) throws SQLException {
        String sql = "UPDATE membership SET id_user = ?, id_class = ?, id_group = ? WHERE id_membership = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUser);
            ps.setInt(2, idClass);
            if (idGroup == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, idGroup);
            }
            ps.setInt(4, idMembership);
            ps.executeUpdate();
        }
    }

    public void delete(int idMembership) throws SQLException {
        String sql = "DELETE FROM membership WHERE id_membership = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMembership);
            ps.executeUpdate();
        }
    }

    /** A user can only have one membership row per class, so this is enough to know if/how they belong. */
    public record MembershipInfo(int idMembership, Integer idGroup) {}

    public MembershipInfo findMembership(int idUser, int idClass) throws SQLException {
        String sql = "SELECT id_membership, id_group FROM membership WHERE id_user = ? AND id_class = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUser);
            ps.setInt(2, idClass);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                int idGroup = rs.getInt("id_group");
                Integer idGroupObj = rs.wasNull() ? null : idGroup;
                return new MembershipInfo(rs.getInt("id_membership"), idGroupObj);
            }
        }
    }

    public void updateGroupOnly(int idMembership, Integer idGroup) throws SQLException {
        String sql = "UPDATE membership SET id_group = ? WHERE id_membership = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            if (idGroup == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, idGroup);
            }
            ps.setInt(2, idMembership);
            ps.executeUpdate();
        }
    }

    public List<User> listClassMembers(int idClass) throws SQLException {
        String sql = """
            SELECT DISTINCT u.id_user, u.name, u.username, u.password
            FROM membership m JOIN user u ON m.id_user = u.id_user
            WHERE m.id_class = ?
            ORDER BY u.name
        """;
        List<User> result = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idClass);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = new User(rs.getInt("id_user"), rs.getString("name"),
                            rs.getString("username"), rs.getString("password"));
                    u.setIdUser(rs.getInt("id_user"));
                    result.add(u);
                }
            }
        }
        return result;
    }

    public List<User> listGroupMembers(int idGroup) throws SQLException {
        String sql = """
            SELECT u.id_user, u.name, u.username, u.password
            FROM membership m JOIN user u ON m.id_user = u.id_user
            WHERE m.id_group = ?
            ORDER BY u.name
        """;
        List<User> result = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idGroup);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = new User(rs.getInt("id_user"), rs.getString("name"),
                            rs.getString("username"), rs.getString("password"));
                    u.setIdUser(rs.getInt("id_user"));
                    result.add(u);
                }
            }
        }
        return result;
    }

    public List<MembershipRow> findAll() throws SQLException {
        String sql = """
            SELECT m.id_membership, m.id_user, u.name AS user_name,
                   m.id_class, c.class_name,
                   m.id_group, g.group_name
            FROM membership m
            JOIN user u ON m.id_user = u.id_user
            JOIN classroom c ON m.id_class = c.id_class
            LEFT JOIN group_table g ON m.id_group = g.id_group
            ORDER BY m.id_membership
        """;
        List<MembershipRow> result = new ArrayList<>();
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int idGroup = rs.getInt("id_group");
                Integer idGroupObj = rs.wasNull() ? null : idGroup;
                result.add(new MembershipRow(
                        rs.getInt("id_membership"),
                        rs.getInt("id_user"), rs.getString("user_name"),
                        rs.getInt("id_class"), rs.getString("class_name"),
                        idGroupObj, rs.getString("group_name")));
            }
        }
        return result;
    }
}
