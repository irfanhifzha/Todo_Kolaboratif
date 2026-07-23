package com.classapp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClassRoom {

    private final int idClass;
    private final String className;

    private final List<Membership> memberships = new ArrayList<>();
    private final List<Group> groups = new ArrayList<>();
    private final List<ClassTask> tasks = new ArrayList<>();

    ClassRoom(int idClass, String className) {
        this.idClass = idClass;
        this.className = className;
    }

    /** Adds a user to this class (a "bare" membership, no group yet) - unless they're already a member. */
    public Membership addMember(User user) {
        Membership existing = findMembershipFor(user);
        if (existing != null) {
            return existing;
        }
        return Membership.saveMembership(user, this, null);
    }

    /** Finds this user's one-and-only Membership row for this class, if any (whether bare or in a group). */
    Membership findMembershipFor(User user) {
        for (Membership m : memberships) {
            if (m.getUser().getIdUser() == user.getIdUser()) {
                return m;
            }
        }
        return null;
    }

    /** Creates a group that lives directly in this class's own `groups` list. */
    public Group createGroup(String name) {
        Group group = Group.saveGroup(name, this);
        groups.add(group);
        return group;
    }

    /** Persists a not-yet-saved ClassTask under this class and adds it to this class's own task list. */
    public void addTask(ClassTask task) {
        task.saveTask(this);
        tasks.add(task);
    }

    public List<Membership> getMemberships() { return memberships; }
    public List<Group> getGroups() { return groups; }
    public List<ClassTask> getTasks() { return tasks; }

    public int getIdClass() { return idClass; }
    public String getClassName() { return className; }

    @Override
    public String toString() { return className; }

    // ---------- package-private sync hooks, called by Membership ----------

    void addMembershipInternal(Membership m) { memberships.add(m); }
    void removeMembershipInternal(Membership m) { memberships.remove(m); }
    void removeTaskInternal(ClassTask t) { tasks.remove(t); }

    // ---------- persistence helpers ----------

    static ClassRoom insert(String name) {
        String sql = "INSERT INTO classes (name) VALUES (?)";
        try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new ClassRoom(keys.getInt(1), name); // brand new - lists start empty
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads a class and its ENTIRE object graph in one pass: its groups
     * (each with its own tasks), its class tasks, and every membership
     * (linked to the already-loaded Group instances above, so nothing
     * gets duplicated).
     */
    public static ClassRoom load(int idClass) {
        String sql = "SELECT name FROM classes WHERE id = ?";
        String name;
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setInt(1, idClass);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                name = rs.getString("name");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ClassRoom classRoom = new ClassRoom(idClass, name);

        List<Group> loadedGroups = Group.findByClassId(idClass, classRoom);
        classRoom.groups.addAll(loadedGroups);

        classRoom.tasks.addAll(ClassTask.findByClassId(idClass, classRoom));

        classRoom.memberships.addAll(Membership.loadForClassRoom(idClass, classRoom, loadedGroups));

        return classRoom;
    }
}
