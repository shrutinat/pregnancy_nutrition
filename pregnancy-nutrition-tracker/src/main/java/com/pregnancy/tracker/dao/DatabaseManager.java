package com.pregnancy.tracker.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseManager handles SQLite database connection and schema initialization.
 * Uses singleton pattern for connection management.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:pregnancy_tracker.db";
    private static DatabaseManager instance;
    private Connection connection;

    /** Private constructor - singleton */
    private DatabaseManager() {}

    /**
     * Get singleton instance of DatabaseManager.
     * @return DatabaseManager instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Get database connection, creating one if it doesn't exist.
     * @return active database connection
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            // Enable foreign keys
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    /**
     * Initialize database schema. Creates all tables if they don't exist.
     * Should be called at application startup.
     */
    public void initializeDatabase() {
        try (Statement stmt = getConnection().createStatement()) {

            // Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    age INTEGER,
                    height REAL,
                    weight REAL,
                    pregnancy_start_date TEXT,
                    bmi REAL,
                    created_at TEXT DEFAULT (datetime('now','localtime'))
                )
            """);

            // Nutrition targets table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS nutrition_targets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    trimester INTEGER NOT NULL,
                    calories REAL,
                    protein REAL,
                    iron REAL,
                    calcium REAL,
                    vitamin_a REAL,
                    vitamin_c REAL,
                    vitamin_d REAL,
                    folic_acid REAL,
                    water_intake REAL,
                    is_doctor_modified INTEGER DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // Food items table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS food_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    category TEXT,
                    calories REAL,
                    protein REAL,
                    iron REAL,
                    calcium REAL,
                    serving_size REAL,
                    serving_unit TEXT
                )
            """);

            // Daily logs table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS daily_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    log_date TEXT NOT NULL,
                    food_item_id INTEGER NOT NULL,
                    quantity REAL,
                    consumed INTEGER DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (food_item_id) REFERENCES food_items(id)
                )
            """);

            // Reminders table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reminders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    scheduled_time TEXT,
                    is_active INTEGER DEFAULT 1,
                    is_recurring INTEGER DEFAULT 0,
                    recurrence_pattern TEXT,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // Doctor updates table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS doctor_updates (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    update_date TEXT NOT NULL,
                    doctor_name TEXT,
                    notes TEXT,
                    risk_conditions TEXT,
                    updated_targets TEXT,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // Weight tracking table (for progress charts)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS weight_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    log_date TEXT NOT NULL,
                    weight REAL NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            System.out.println("[DB] Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("[DB] Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Close the database connection.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }
}
