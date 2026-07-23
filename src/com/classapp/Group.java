package com.classapp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Group {

    private final int idGroup;
    private final String groupName;
    private final ClassRoom classRoom; // needed internally so addMember() knows which class to record

    private final List<Membership> memberships = new ArrayList<>();
    private final List<GroupTask> tasks = new ArrayList<>();

    Group(int idGroup, String groupName, ClassRoom classRoom) {
        this.idGroup = idGroup;
        this.groupName = groupName;
        this.classRoom = classRoom;
    }

    /**
     * Adds a user to this group. A user can only be in one group per
     * class at a time: if they already have a membership for this
     * class (bare, or pointing at a different group), it gets
     * reassigned to this group rather than adding a second row.
     */
    public void addMember(User user) {
        for (Membership m : memberships) {
            if (m.getUser().getIdUser() == user.getIdUser()) {
                return; // already in this specific group
            }
        }

        Membership existing = classRoom.findMembershipFor(user);
        if (existing != null) {
            existing.assignGroup(this); // moves them here from whatever group (or none) they were in
        } else {
            Membership.saveMembership(user, classRoom, this);
        }
    }

    /** Removes a user from this group - they stay in the class, just with no group assigned. */
    public void removeMember(User user) {
        for (Membership m : memberships) {
            if (m.getUser().getIdUser() == user.getIdUser()) {
                m.assignGroup(null);
                return;
            }
        }
    }

    /** Persists a not-yet-saved GroupTask under this group and adds it to this group's own task list. */
    public void createTask(GroupTask task) {
        task.saveTask(this);
        tasks.add(task);
    }

    public List<Membership> getMemberships() { return memberships; }
    public List<GroupTask> getTasks() { return tasks; }
    public ClassRoom getClassRoom() { return classRoom; }

    public int getIdGroup() { return idGroup; }
    public String getGroupName() { return groupName; }

    @Override
    public String toString() { return groupName; }

    // ---------- package-private sync hooks, called by Membership ----------

    void addMembershipInternal(Membership m) { memberships.add(m); }
    void removeMembershipInternal(Membership m) { memberships.remove(m); }
    void removeTaskInternal(GroupTask t) { tasks.remove(t); }

    // ---------- persistence helpers ----------

    static Group saveGroup(String name, ClassRoom classRoom) {
        String sql = "INSERT INTO groups_table (name, class_id) VALUES (?, ?)";
        try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setInt(2, classRoom.getIdClass());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Group(keys.getInt(1), name, classRoom); // brand new - lists start empty
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Loads every group in a class, each with its own tasks already populated. Called once by ClassRoom.load(). */
    static List<Group> findByClassId(int classId, ClassRoom classRoom) {
        String sql = "SELECT id, name FROM groups_table WHERE class_id = ?";
        List<Group> result = new ArrayList<>();
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Group g = new Group(rs.getInt("id"), rs.getString("name"), classRoom);
                    g.tasks.addAll(GroupTask.findByGroupId(g.idGroup, g));
                    result.add(g);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
