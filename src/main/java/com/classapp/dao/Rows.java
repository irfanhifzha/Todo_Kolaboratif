package com.classapp.dao;

/**
 * Small read-only "row" records used only for displaying joined data in the GUI
 * (e.g. a JTable).
 *
 * They exist because several of the given POJOs (Group, ClassTask, GroupTask,
 * Membership, UserTaskStatus, GroupTaskStatus) deliberately don't expose getters
 * for their reference fields (classRoom, group, user, task, etc.), so the DAO/
 * view layer cannot pull that information back out of those objects. Rather than
 * touch the POJOs, the DAOs return these small immutable records instead, built
 * straight from SQL JOIN results. The POJOs themselves are still what's used for
 * insert/update calls.
 */
public final class Rows {
    private Rows() {}

    public record GroupRow(int idGroup, String groupName, int idClass, String className) {}

    public record ClassTaskRow(int idTask, String title, String description, int idClass, String className) {}

    public record GroupTaskRow(int idTask, String title, String description, int idGroup, String groupName) {}

    public record MembershipRow(int idMembership, int idUser, String userName,
                                 int idClass, String className,
                                 Integer idGroup, String groupName) {}

    public record UserTaskStatusRow(int idStatus, int idUser, String userName,
                                     int idTask, String taskTitle, String status) {}

    public record GroupTaskStatusRow(int idStatus, int idGroupTask, String taskTitle, String status) {}
}
