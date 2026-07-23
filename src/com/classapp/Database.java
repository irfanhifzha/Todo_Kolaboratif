package com.classapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Holds the single shared SQLite connection and creates the schema.
 * This is plumbing, not a "DAO" - the domain classes (User, ClassRoom,
 * Group, Task, ...) each write their own SQL directly, and each keeps
 * its own in-memory List fields (memberships/groups/tasks) exactly as
 * the class diagram shows, instead of re-querying the database on
 * every getter call.
 */
public class Database {

    private static final String URL = "jdbc:sqlite:classapp.db";
    private static Connection connection;

    public static Connection get() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(URL);
                createSchema();
            } catch (SQLException e) {
                throw new RuntimeException("Could not connect to database", e);
            }
        }
        return connection;
    }

    private static void createSchema() throws SQLException {
        String[] statements = {
            "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL" +
            ")",

            "CREATE TABLE IF NOT EXISTS classes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL" +
            ")",

            // class_id makes ClassRoom.groups a real, directly-owned
            // collection - a group's parent class no longer depends on
            // some membership row still pointing at it.
            "CREATE TABLE IF NOT EXISTS groups_table (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "class_id INTEGER NOT NULL," +
                "FOREIGN KEY(class_id) REFERENCES classes(id)" +
            ")",

            // Membership ties a user to a class, and optionally further to
            // a group within that class. A user may have more than one row
            // for the same class: at most one "bare" row (group_id NULL)
            // plus one row per group they've joined.
            "CREATE TABLE IF NOT EXISTS memberships (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "class_id INTEGER NOT NULL," +
                "group_id INTEGER," +
                "FOREIGN KEY(user_id) REFERENCES users(id)," +
                "FOREIGN KEY(class_id) REFERENCES classes(id)," +
                "FOREIGN KEY(group_id) REFERENCES groups_table(id)" +
            ")",

            "CREATE TABLE IF NOT EXISTS class_tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "class_id INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "FOREIGN KEY(class_id) REFERENCES classes(id)" +
            ")",

            "CREATE TABLE IF NOT EXISTS group_tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "group_id INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "FOREIGN KEY(group_id) REFERENCES groups_table(id)" +
            ")",

            "CREATE TABLE IF NOT EXISTS user_task_status (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "status TEXT NOT NULL DEFAULT 'TODO'," +
                "user_id INTEGER NOT NULL," +
                "task_id INTEGER NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES users(id)," +
                "FOREIGN KEY(task_id) REFERENCES class_tasks(id)" +
            ")",

            "CREATE TABLE IF NOT EXISTS group_task_status (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "status TEXT NOT NULL DEFAULT 'TODO'," +
                "task_id INTEGER NOT NULL UNIQUE," +
                "FOREIGN KEY(task_id) REFERENCES group_tasks(id)" +
            ")"
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : statements) {
                stmt.execute(sql);
            }
        }
    }
}
