package com.pregnancy.tracker.dao;

import com.pregnancy.tracker.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User entity.
 * Handles all CRUD operations for the users table.
 */
public class UserDao {

    private final DatabaseManager dbManager;

    public UserDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Insert a new user into the database.
     * @param user User object to insert
     * @return generated user ID, or -1 on failure
     */
    public int insert(User user) {
        String sql = """
            INSERT INTO users (name, email, password, age, height, weight, pregnancy_start_date, bmi)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.setInt(4, user.getAge());
            pstmt.setDouble(5, user.getHeight());
            pstmt.setDouble(6, user.getWeight());
            pstmt.setString(7, user.getPregnancyStartDate() != null ?
                    user.getPregnancyStartDate().toString() : null);
            pstmt.setDouble(8, user.getBmi());

            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                user.setId(id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("[UserDao] Insert error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Find a user by email and password (login).
     * @param email user email
     * @param password user password (hashed)
     * @return User object if found, null otherwise
     */
    public User findByEmailAndPassword(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserDao] Login error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Find a user by email.
     * @param email user email
     * @return User object if found, null otherwise
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserDao] FindByEmail error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Find a user by ID.
     * @param id user ID
     * @return User object if found, null otherwise
     */
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserDao] FindById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Update an existing user's profile.
     * @param user User object with updated values
     * @return true if update was successful
     */
    public boolean update(User user) {
        String sql = """
            UPDATE users SET name=?, age=?, height=?, weight=?,
            pregnancy_start_date=?, bmi=? WHERE id=?
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setInt(2, user.getAge());
            pstmt.setDouble(3, user.getHeight());
            pstmt.setDouble(4, user.getWeight());
            pstmt.setString(5, user.getPregnancyStartDate() != null ?
                    user.getPregnancyStartDate().toString() : null);
            pstmt.setDouble(6, user.calculateBMI());
            pstmt.setInt(7, user.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDao] Update error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get all users from the database.
     * @return list of all users
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDao] FindAll error: " + e.getMessage());
        }
        return users;
    }

    /**
     * Map a ResultSet row to a User object.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setAge(rs.getInt("age"));
        user.setHeight(rs.getDouble("height"));
        user.setWeight(rs.getDouble("weight"));

        String dateStr = rs.getString("pregnancy_start_date");
        if (dateStr != null && !dateStr.isEmpty()) {
            user.setPregnancyStartDate(LocalDate.parse(dateStr));
        }

        user.setBmi(rs.getDouble("bmi"));
        user.setCreatedAt(rs.getString("created_at"));
        return user;
    }
}
