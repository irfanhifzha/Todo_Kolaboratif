package com.classapp.dao;

import com.classapp.User;
import com.classapp.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data-access object that maps rows of the {@code user} table to/from {@link User}. */
public class UserDAO {

    public User insert(User user) throws SQLException {
        String sql = "INSERT INTO user (name, username, password) VALUES (?, ?, ?)";
        Connection conn = Database.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setIdUser(keys.getInt(1));
                }
            }
        }
        return user;
    }

    public void update(User user) throws SQLException {
        String sql = "UPDATE user SET name = ?, username = ?, password = ? WHERE id_user = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setInt(4, user.getIdUser());
            ps.executeUpdate();
        }
    }

    public void delete(int idUser) throws SQLException {
        String sql = "DELETE FROM user WHERE id_user = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUser);
            ps.executeUpdate();
        }
    }

    public User findById(int idUser) throws SQLException {
        String sql = "SELECT * FROM user WHERE id_user = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM user WHERE username = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM user ORDER BY id_user";
        List<User> result = new ArrayList<>();
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(map(rs));
            }
        }
        return result;
    }

    private User map(ResultSet rs) throws SQLException {
        User user = new User(rs.getInt("id_user"), rs.getString("name"),
                rs.getString("username"), rs.getString("password"));
        user.setIdUser(rs.getInt("id_user"));
        return user;
    }
}
