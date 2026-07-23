package com.classapp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Ties a User to a ClassRoom, and optionally further to a Group within
 * that class. A user can have more than one Membership for the same
 * class: at most one "bare" one (group == null) plus one per group
 * they've joined - so joining a second group doesn't disturb the first.
 *
 * Whenever a Membership is created, reassigned, or deleted, it keeps
 * the User's, ClassRoom's, and Group's own in-memory `memberships`
 * lists in sync via package-private hooks on those classes.
 */
public class Membership {

    private int idMembership;
    private final User user;
    private final ClassRoom classRoom;
    private Group group; // may be null - the user hasn't joined a group yet

    Membership(int idMembership, User user, ClassRoom classRoom, Group group) {
        this.idMembership = idMembership;
        this.user = user;
        this.classRoom = classRoom;
        this.group = group;
    }

    /** Moves this membership to a different group (keeps everyone's lists in sync). */
    public void assignGroup(Group newGroup) {
        String sql = "UPDATE memberships SET group_id = ? WHERE id = ?";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            if (newGroup == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, newGroup.getIdGroup());
            }
            ps.setInt(2, idMembership);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (this.group != null) {
            this.group.removeMembershipInternal(this);
        }
        this.group = newGroup;
        if (newGroup != null) {
            newGroup.addMembershipInternal(this);
        }
    }

    /** Deletes this membership entirely, removing it from every in-memory list that held it. */
    public void delete() {
        String sql = "DELETE FROM memberships WHERE id = ?";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setInt(1, idMembership);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        user.removeMembershipInternal(this);
        classRoom.removeMembershipInternal(this);
        if (group != null) {
            group.removeMembershipInternal(this);
        }
    }

    public ClassRoom getClassRoom() { return classRoom; }
    public Group getGroup() { return group; }
    public User getUser() { return user; }
    public int getIdMembership() { return idMembership; }

    // ---------- persistence helpers (no separate DAO - lives right on Membership) ----------

    /**
     * Creates a brand new membership row and links it into user, classRoom,
     * and (if not null) group's own in-memory lists. Callers are expected
     * to have already checked for an existing equivalent row if they want
     * to avoid duplicates (see ClassRoom.addMember / Group.addMember).
     */
    static Membership saveMembership(User user, ClassRoom classRoom, Group group) {
        String sql = "INSERT INTO memberships (user_id, class_id, group_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = Database.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, user.getIdUser());
            ps.setInt(2, classRoom.getIdClass());
            if (group == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, group.getIdGroup());
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                Membership m = new Membership(keys.getInt(1), user, classRoom, group);
                user.addMembershipInternal(m);
                classRoom.addMembershipInternal(m);
                if (group != null) {
                    group.addMembershipInternal(m);
                }
                return m;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Distinct class IDs a user belongs to - used once by User.login() to know which classes to load. */
    static List<Integer> findDistinctClassIdsForUser(int userId) {
        String sql = "SELECT DISTINCT class_id FROM memberships WHERE user_id = ?";
        List<Integer> result = new ArrayList<>();
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt("class_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Loads every membership row for a class, in one pass, resolving user_id
     * via User.loadStub and group_id by looking the group up in the
     * ClassRoom's already-loaded `groups` list (so the same Group instance
     * is reused rather than creating a duplicate). Used once by ClassRoom.load().
     */
    static List<Membership> loadForClassRoom(int classId, ClassRoom classRoom, List<Group> loadedGroups) {
        String sql = "SELECT id, user_id, group_id FROM memberships WHERE class_id = ?";
        List<Membership> result = new ArrayList<>();
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = User.loadStub(rs.getInt("user_id"));
                    int groupId = rs.getInt("group_id");
                    Group group = null;
                    if (!rs.wasNull()) {
                        for (Group g : loadedGroups) {
                            if (g.getIdGroup() == groupId) { group = g; break; }
                        }
                    }
                    Membership m = new Membership(rs.getInt("id"), user, classRoom, group);
                    if (group != null) {
                        group.addMembershipInternal(m);
                    }
                    result.add(m);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
