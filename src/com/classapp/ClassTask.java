package com.classapp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClassTask extends Task {

    private ClassRoom classRoom;

    /** Not-yet-saved task; call classRoom.addTask(this) to persist it. */
    public ClassTask(String title, String description) {
        super(title, description);
    }

    private ClassTask(int idTask, ClassRoom classRoom, String title, String description) {
        super(title, description);
        this.idTask = idTask;
        this.classRoom = classRoom;
    }

    @Override
    protected String tableName() { return "class_tasks"; }

    @Override
    public void deleteTask() {
        super.deleteTask();
        if (classRoom != null) {
            classRoom.removeTaskInternal(this);
        }
    }

    /** Sets (or creates if needed) this user's status for this task. */
    public void updateUserStatus(User user, TaskStatus status) {
        UserTaskStatus.findOrCreate(user, this).changeStatus(status);
    }

    /** Convenience for the GUI: this user's current status for this task. */
    public TaskStatus getStatusFor(User user) {
        return UserTaskStatus.findOrCreate(user, this).getStatus();
    }

    public ClassRoom getClassRoom() { return classRoom; }

    // ---------- persistence helpers ----------

    /** Called by ClassRoom.addTask(task) to actually save a new task. */
    void saveTask(ClassRoom classRoom) {
        String sql = "INSERT INTO class_tasks (class_id, title, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, classRoom.getIdClass());
            ps.setString(2, title);
            ps.setString(3, description);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                this.idTask = keys.getInt(1);
                this.classRoom = classRoom;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static List<ClassTask> findByClassId(int classId, ClassRoom classRoom) {
        String sql = "SELECT id, title, description FROM class_tasks WHERE class_id = ?";
        List<ClassTask> result = new ArrayList<>();
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new ClassTask(rs.getInt("id"), classRoom,
                            rs.getString("title"), rs.getString("description")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
