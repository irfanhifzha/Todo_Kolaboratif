package com.classapp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Central place that owns the single JDBC connection to the SQLite database
 * file and knows how to create the schema the very first time the app runs.
 *
 * This class is intentionally simple (a lazily-created singleton connection)
 * because Swing apps in a school-project context are single-threaded on the
 * EDT and don't need a connection pool.
 */
public final class Database {

    private static final String DB_FILE = "classapp.db";
    private static final String URL = "jdbc:sqlite:" + DB_FILE;

    private static Connection connection;

    private Database() {
        // utility class, no instances
    }

    /** Returns the single shared connection, opening it (and the schema) on first use. */
    public static synchronized Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(URL);
                try (Statement st = connection.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON");
                }
                initSchema(connection);
            } catch (ClassNotFoundException | SQLException e) {
                throw new RuntimeException("Failed to open SQLite database: " + e.getMessage(), e);
            }
        }
        return connection;
    }

    /** Creates every table if it does not already exist. Safe to call every startup. */
    private static void initSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS user (
                    id_user   INTEGER PRIMARY KEY AUTOINCREMENT,
                    name      TEXT NOT NULL,
                    username  TEXT NOT NULL UNIQUE,
                    password  TEXT NOT NULL
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS classroom (
                    id_class   INTEGER PRIMARY KEY AUTOINCREMENT,
                    class_name TEXT NOT NULL
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS group_table (
                    id_group   INTEGER PRIMARY KEY AUTOINCREMENT,
                    group_name TEXT NOT NULL,
                    id_class   INTEGER NOT NULL,
                    FOREIGN KEY (id_class) REFERENCES classroom(id_class) ON DELETE CASCADE
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS membership (
                    id_membership INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_user       INTEGER NOT NULL,
                    id_class      INTEGER NOT NULL,
                    id_group      INTEGER,
                    FOREIGN KEY (id_user)  REFERENCES user(id_user)   ON DELETE CASCADE,
                    FOREIGN KEY (id_class) REFERENCES classroom(id_class) ON DELETE CASCADE,
                    FOREIGN KEY (id_group) REFERENCES group_table(id_group) ON DELETE SET NULL
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS class_task (
                    id_task     INTEGER PRIMARY KEY AUTOINCREMENT,
                    title       TEXT NOT NULL,
                    description TEXT,
                    id_class    INTEGER NOT NULL,
                    FOREIGN KEY (id_class) REFERENCES classroom(id_class) ON DELETE CASCADE
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS group_task (
                    id_task     INTEGER PRIMARY KEY AUTOINCREMENT,
                    title       TEXT NOT NULL,
                    description TEXT,
                    id_group    INTEGER NOT NULL,
                    FOREIGN KEY (id_group) REFERENCES group_table(id_group) ON DELETE CASCADE
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS user_task_status (
                    id_status INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_user   INTEGER NOT NULL,
                    id_task   INTEGER NOT NULL,
                    status    TEXT NOT NULL,
                    FOREIGN KEY (id_user) REFERENCES user(id_user) ON DELETE CASCADE,
                    FOREIGN KEY (id_task) REFERENCES class_task(id_task) ON DELETE CASCADE
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS group_task_status (
                    id_status     INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_group_task INTEGER NOT NULL,
                    status        TEXT NOT NULL,
                    FOREIGN KEY (id_group_task) REFERENCES group_task(id_task) ON DELETE CASCADE
                )
            """);
        }
    }

    /** Closes the shared connection. Call this once, when the application exits. */
    public static synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // nothing useful to do on shutdown
            } finally {
                connection = null;
            }
        }
    }
}
