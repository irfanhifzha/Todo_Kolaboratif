package com.classapp.app;

import com.classapp.*;
import com.classapp.controller.ValidationException;
import com.classapp.dao.*;
import com.classapp.dao.Rows.ClassTaskRow;
import com.classapp.dao.Rows.GroupRow;
import com.classapp.dao.Rows.GroupTaskRow;

import java.sql.SQLException;
import java.util.List;

/**
 * The Application layer holds every business process ("use case") the app supports:
 * register/login, creating and joining classes, creating and joining groups, and
 * creating/editing/deleting/tracking tasks. Controllers call these methods; these
 * methods talk to the DAOs. Neither the GUI nor the POJOs talk to a DAO directly.
 *
 * This is where the rules of the app live, e.g. "a user can belong to at most one
 * group per class" - that rule is enforced here (in joinGroup), not scattered across
 * the views. POJOs stay plain data holders; DAOs stay pure persistence.
 */
public class Application {

    private final UserDAO userDAO = new UserDAO();
    private final ClassRoomDAO classRoomDAO = new ClassRoomDAO();
    private final GroupDAO groupDAO = new GroupDAO();
    private final MembershipDAO membershipDAO = new MembershipDAO();
    private final ClassTaskDAO classTaskDAO = new ClassTaskDAO();
    private final GroupTaskDAO groupTaskDAO = new GroupTaskDAO();
    private final UserTaskStatusDAO userTaskStatusDAO = new UserTaskStatusDAO();
    private final GroupTaskStatusDAO groupTaskStatusDAO = new GroupTaskStatusDAO();

    // ---------------------------------------------------------------- Auth

    /** Use case: Register. Returns the new, persisted user. */
    public User register(String name, String username, String password) throws SQLException, ValidationException {
        if (name == null || name.trim().isEmpty()) throw new ValidationException("Name cannot be empty.");
        if (username == null || username.trim().isEmpty()) throw new ValidationException("Username cannot be empty.");
        if (password == null || password.isEmpty()) throw new ValidationException("Password cannot be empty.");
        if (userDAO.findByUsername(username.trim()) != null) {
            throw new ValidationException("That username is already taken.");
        }
        User user = new User(0, name.trim(), username.trim(), password);
        return userDAO.insert(user);
    }

    /** Use case: Log In. Returns the matching user, or null if the credentials are wrong. */
    public User login(String username, String password) throws SQLException {
        User user = userDAO.findByUsername(username);
        if (user == null) return null;
        return user.getPassword().equals(password) ? user : null;
    }

    // ---------------------------------------------------------------- Classes

    /** Use case: View My Classes. */
    public List<ClassRoom> listMyClasses(User user) throws SQLException {
        return classRoomDAO.findByUser(user.getIdUser());
    }

    /** Use case: Create Class. The creator is automatically enrolled as a member. */
    public ClassRoom createClass(User user, String className) throws SQLException, ValidationException {
        if (className == null || className.trim().isEmpty()) throw new ValidationException("Class name cannot be empty.");
        ClassRoom classRoom = classRoomDAO.insert(new ClassRoom(className.trim()));
        membershipDAO.insert(user.getIdUser(), classRoom.getIdClass(), null);
        return classRoom;
    }

    /** Use case: Join Class (by ID). Returns null if no such class exists. Does nothing if already a member. */
    public ClassRoom joinClassById(User user, int classId) throws SQLException, ValidationException {
        ClassRoom classRoom = classRoomDAO.findById(classId);
        if (classRoom == null) return null;
        if (membershipDAO.findMembership(user.getIdUser(), classId) != null) {
            throw new ValidationException("You're already a member of that class.");
        }
        membershipDAO.insert(user.getIdUser(), classId, null);
        return classRoom;
    }

    /** Use case: View Class Members. */
    public List<User> listClassMembers(int classId) throws SQLException {
        return membershipDAO.listClassMembers(classId);
    }

    // ---------------------------------------------------------------- Groups

    /** Use case: View Class Groups. */
    public List<GroupRow> listClassGroups(int classId) throws SQLException {
        return groupDAO.findByClass(classId);
    }

    /** Use case: Create Group. */
    public Group createGroup(int classId, String groupName) throws SQLException, ValidationException {
        if (groupName == null || groupName.trim().isEmpty()) throw new ValidationException("Group name cannot be empty.");
        return groupDAO.insert(classId, groupName.trim());
    }

    /** The group this user currently belongs to within this class, or null if none. */
    public GroupRow findMyGroupInClass(User user, int classId) throws SQLException {
        MembershipDAO.MembershipInfo info = membershipDAO.findMembership(user.getIdUser(), classId);
        if (info == null || info.idGroup() == null) return null;
        return groupDAO.findById(info.idGroup());
    }

    /**
     * Use case: Join Group. A user may belong to at most one group per class, so
     * joining a new group in a class they're already grouped in simply moves them.
     */
    public void joinGroup(User user, int classId, int groupId) throws SQLException, ValidationException {
        MembershipDAO.MembershipInfo info = membershipDAO.findMembership(user.getIdUser(), classId);
        if (info == null) {
            throw new ValidationException("You must join the class before joining one of its groups.");
        }
        membershipDAO.updateGroupOnly(info.idMembership(), groupId);
    }

    /** Use case: Leave Group (stays a member of the class itself). */
    public void leaveGroup(User user, int classId) throws SQLException {
        MembershipDAO.MembershipInfo info = membershipDAO.findMembership(user.getIdUser(), classId);
        if (info != null) {
            membershipDAO.updateGroupOnly(info.idMembership(), null);
        }
    }

    /** Use case: View Group Members. */
    public List<User> listGroupMembers(int groupId) throws SQLException {
        return membershipDAO.listGroupMembers(groupId);
    }

    // ---------------------------------------------------------------- Class Tasks

    /** Use case: View Class Tasks. */
    public List<ClassTaskRow> listClassTasks(int classId) throws SQLException {
        return classTaskDAO.findByClass(classId);
    }

    /** Use case: Create Class Task. */
    public ClassTask createClassTask(int classId, String title, String description) throws SQLException, ValidationException {
        if (title == null || title.trim().isEmpty()) throw new ValidationException("Title cannot be empty.");
        return classTaskDAO.insert(classId, title.trim(), description == null ? "" : description.trim());
    }

    /** Use case: Edit Class Task. */
    public void editClassTask(int taskId, String title, String description, int classId) throws SQLException, ValidationException {
        if (title == null || title.trim().isEmpty()) throw new ValidationException("Title cannot be empty.");
        classTaskDAO.update(taskId, title.trim(), description == null ? "" : description.trim(), classId);
    }

    /** Use case: Delete Class Task. */
    public void deleteClassTask(int taskId) throws SQLException {
        classTaskDAO.delete(taskId);
    }

    /** Use case: Update My Task Status (for a class task). Defaults to TODO if never set. */
    public TaskStatus getMyClassTaskStatus(int userId, int taskId) throws SQLException {
        UserTaskStatusDAO.StatusInfo info = userTaskStatusDAO.findByUserAndTask(userId, taskId);
        return info == null ? TaskStatus.TODO : info.status();
    }

    public void setMyClassTaskStatus(int userId, int taskId, TaskStatus status) throws SQLException {
        userTaskStatusDAO.upsert(userId, taskId, status);
    }

    // ---------------------------------------------------------------- Group Tasks

    /** Use case: View Group Tasks. */
    public List<GroupTaskRow> listGroupTasks(int groupId) throws SQLException {
        return groupTaskDAO.findByGroup(groupId);
    }

    /** Use case: Create Group Task. */
    public GroupTask createGroupTask(int groupId, String title, String description) throws SQLException, ValidationException {
        if (title == null || title.trim().isEmpty()) throw new ValidationException("Title cannot be empty.");
        return groupTaskDAO.insert(groupId, title.trim(), description == null ? "" : description.trim());
    }

    /** Use case: Edit Group Task. */
    public void editGroupTask(int taskId, String title, String description, int groupId) throws SQLException, ValidationException {
        if (title == null || title.trim().isEmpty()) throw new ValidationException("Title cannot be empty.");
        groupTaskDAO.update(taskId, title.trim(), description == null ? "" : description.trim(), groupId);
    }

    /** Use case: Delete Group Task. */
    public void deleteGroupTask(int taskId) throws SQLException {
        groupTaskDAO.delete(taskId);
    }

    /** Use case: Update Group Task Status (one shared status per group task). Defaults to TODO if never set. */
    public TaskStatus getGroupTaskStatus(int groupTaskId) throws SQLException {
        GroupTaskStatusDAO.StatusInfo info = groupTaskStatusDAO.findByGroupTask(groupTaskId);
        return info == null ? TaskStatus.TODO : info.status();
    }

    public void setGroupTaskStatus(int groupTaskId, TaskStatus status) throws SQLException {
        groupTaskStatusDAO.upsert(groupTaskId, status);
    }
}
