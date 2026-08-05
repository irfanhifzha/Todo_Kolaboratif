package com.classapp.dao;

import com.classapp.ClassRoom;
import com.classapp.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data-access object that maps rows of the {@code classroom} table to/from {@link ClassRoom}. */
public class ClassRoomDAO {

    public ClassRoom insert(ClassRoom classRoom) throws SQLException {
        String sql = "INSERT INTO classroom (class_name) VALUES (?)";
        Connection conn = Database.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, classRoom.getClassName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    classRoom.setIdClass(keys.getInt(1));
                }
            }
        }
        return classRoom;
    }

    public void update(ClassRoom classRoom) throws SQLException {
        String sql = "UPDATE classroom SET class_name = ? WHERE id_class = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, classRoom.getClassName());
            ps.setInt(2, classRoom.getIdClass());
            ps.executeUpdate();
        }
    }

    public void delete(int idClass) throws SQLException {
        String sql = "DELETE FROM classroom WHERE id_class = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idClass);
            ps.executeUpdate();
        }
    }

    public ClassRoom findById(int idClass) throws SQLException {
        String sql = "SELECT * FROM classroom WHERE id_class = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idClass);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Classes the given user belongs to (via any membership row), most recently joined first. */
    public List<ClassRoom> findByUser(int idUser) throws SQLException {
        String sql = """
            SELECT DISTINCT c.id_class, c.class_name
            FROM classroom c JOIN membership m ON c.id_class = m.id_class
            WHERE m.id_user = ?
            ORDER BY c.id_class
        """;
        List<ClassRoom> result = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }
        return result;
    }

    public List<ClassRoom> findAll() throws SQLException {
        String sql = "SELECT * FROM classroom ORDER BY id_class";
        List<ClassRoom> result = new ArrayList<>();
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(map(rs));
            }
        }
        return result;
    }

    private ClassRoom map(ResultSet rs) throws SQLException {
        ClassRoom classRoom = new ClassRoom(rs.getString("class_name"));
        classRoom.setIdClass(rs.getInt("id_class"));
        return classRoom;
    }
}
