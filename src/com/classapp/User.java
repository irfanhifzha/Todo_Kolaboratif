package com.classapp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A user account. Following the diagram, User owns the behavior for
 * logging in/out and for creating/joining classes and groups, and it
 * keeps its own `memberships` list as a real field - loaded once (at
 * login) and mutated in place afterwards, not re-queried from the
 * database on every getMemberships() call.
 */
public class User {

    private int idUser;
    private String name;
    private String username;
    private String password;

    private final List<Membership> memberships = new ArrayList<>();

    /** Used when attempting to log in: you don't have an idUser yet. */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /** Used internally once a user is loaded/created from the database. */
    User(int idUser, String name, String username) {
        this.idUser = idUser;
        this.name = name;
        this.username = username;
    }

    // The diagram doesn't show account creation explicitly, but some
    // method has to create the first row.
    public static User register(String name, String username, String password) {
        String sql = "INSERT INTO users (name, username, password) VALUES (?, ?, ?)";
        try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new User(keys.getInt(1), name, username);
                }
            }
        } catch (SQLException e) {
            return null; // most likely username already taken
        }
        return null;
    }

    /** Loads a User by id (used internally when rebuilding Membership objects from rows). */
    static User loadStub(int idUser) {
        String sql = "SELECT name, username FROM users WHERE id = ?";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(idUser, rs.getString("name"), rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /** Checks username/password against the database and, on success, fills in idUser/name/memberships. */
    public boolean login() {
        String sql = "SELECT id, name, password FROM users WHERE username = ?";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString("password").equals(password)) {
                    this.idUser = rs.getInt("id");
                    this.name = rs.getString("name");
                    loadMemberships();
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * Populates this.memberships once, at login. For every class this
     * user belongs to, the whole class is loaded (ClassRoom.load), which
     * builds that class's own complete, cross-linked object graph
     * (groups, tasks, memberships). This user's own membership rows are
     * then picked out of that graph, so everyone ends up sharing the
     * same Group/ClassRoom instances rather than duplicates.
     */
    private void loadMemberships() {
        memberships.clear();
        for (int classId : Membership.findDistinctClassIdsForUser(idUser)) {
            ClassRoom classRoom = ClassRoom.load(classId);
            for (Membership m : classRoom.getMemberships()) {
                if (m.getUser().getIdUser() == idUser) {
                    memberships.add(m);
                }
            }
        }
    }

    public void logout() {
        this.idUser = 0;
        this.name = null;
        this.memberships.clear();
    }

    /** Creates a new class and immediately enrolls this user as its first member. */
    public ClassRoom createClass(String name) {
        ClassRoom classRoom = ClassRoom.insert(name);
        classRoom.addMember(this);
        return classRoom;
    }

    /** Joins an existing class (class-level membership only, no group yet). */
    public void joinClass(ClassRoom classRoom) {
        classRoom.addMember(this);
    }

    /** Creates a new group inside a class and enrolls this user into it right away. */
    public Group createGroup(ClassRoom classRoom, String name) {
        Group group = classRoom.createGroup(name);
        group.addMember(this);
        return group;
    }

    public void joinGroup(Group group) {
        group.addMember(this);
    }

    /** Leaves a group (stays in the class). Not in the original diagram, added for symmetry with joinGroup. */
    public void leaveGroup(Group group) {
        group.removeMember(this);
    }

    /** All class tasks and group tasks visible to this user, across every class/group they belong to. */
    public List<Task> viewTasks() {
        List<Task> tasks = new ArrayList<>();
        Set<Integer> seenClassIds = new HashSet<>();
        for (Membership m : memberships) {
            if (seenClassIds.add(m.getClassRoom().getIdClass())) {
                tasks.addAll(m.getClassRoom().getTasks());
            }
            if (m.getGroup() != null) {
                tasks.addAll(m.getGroup().getTasks());
            }
        }
        return tasks;
    }

    public List<Membership> getMemberships() {
        return memberships;
    }

    // ---------- package-private sync hooks, called by Membership ----------

    void addMembershipInternal(Membership m) { memberships.add(m); }
    void removeMembershipInternal(Membership m) { memberships.remove(m); }

    public int getIdUser() { return idUser; }
    public String getName() { return name; }
    public String getUsername() { return username; }

    @Override
    public String toString() {
        return name + " (@" + username + ")";
    }
}
