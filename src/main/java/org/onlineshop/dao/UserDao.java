package org.onlineshop.dao;

import org.onlineshop.config.ConnectionPool;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.sql.SQLException;

@Repository
public class UserDao {
    private final ConnectionPool pool;
    private final PasswordEncoder encoder;

    public UserDao(ConnectionPool pool, PasswordEncoder encoder) {
        this.pool = pool;
        this.encoder = encoder;
    }

    public void saveUser(String username, String rawPassword) {
        String sql = "INSERT INTO users(username, password, role) VALUES (?,?, 'USER')";
        try {
            Connection c = pool.borrow();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, encoder.encode(rawPassword));
                ps.executeUpdate();
            } finally { pool.release(c); }
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean exists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try {
            Connection c = pool.borrow();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
            } finally { pool.release(c); }
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
