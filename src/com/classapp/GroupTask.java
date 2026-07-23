package com.classapp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GroupTask extends Task {

    private Group group;

    /** Not-yet-saved task; call group.createTask(this) to persist it. */
    public GroupTask(String title, String description) {
        super(title, description);
    }

    private GroupTask(int idTask, Group group, String title, String description) {
        super(title, description);
        this.idTask = idTask;
        this.group = group;
    }

    @Override
    protected String tableName() { return "group_tasks"; }

    @Override
    public void deleteTask() {
        super.deleteTask();
        if (group != null) {
            group.removeTaskInternal(this);
        }
    }

    /** Sets the single, shared status for this task. */
    public void updateGroupStatus(TaskStatus status) {
        GroupTaskStatus.findOrCreate(this).changeStatus(status);
    }

    /** Convenience for the GUI: this task's current (shared) status. */
    public TaskStatus getStatus() {
        return GroupTaskStatus.findOrCreate(this).getStatus();
    }

    public Group getGroup() { return group; }

    // ---------- persistence helpers ----------

    /** Called by Group.createTask(task) to actually save a new task, plus its initial status row. */
    void saveTask(Group group) {
        String sql = "INSERT INTO group_tasks (group_id, title, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, group.getIdGroup());
            ps.setString(2, title);
            ps.setString(3, description);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                this.idTask = keys.getInt(1);
                this.group = group;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        GroupTaskStatus.findOrCreate(this); // seeds the 1-to-1 status row with TODO
    }

    static List<GroupTask> findByGroupId(int groupId, Group group) {
        String sql = "SELECT id, title, description FROM group_tasks WHERE group_id = ?";
        List<GroupTask> result = new ArrayList<>();
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new GroupTask(rs.getInt("id"), group,
                            rs.getString("title"), rs.getString("description")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
