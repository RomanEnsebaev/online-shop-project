package org.onlineshop.config.security;

import org.onlineshop.config.database.ConnectionPool;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.sql.*;
import java.util.List;

public class CustomUserDetailsService implements UserDetailsService {

    private final ConnectionPool pool;

    public CustomUserDetailsService(ConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final String sql = """
                SELECT user_id,
                         username,
                         password,
                         role,
                         (deleted_at IS NULL) AS enabled
                   FROM users
                   WHERE username = ?
                """;

        System.out.println(">>> loadUserByUsername CALLED with: " + username);

        try {
            Connection c = pool.borrow();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()){
                    throw new UsernameNotFoundException("User not found");
                }

                Integer id = rs.getInt("user_id");
                String login = rs.getString("username");
                String dbHash = rs.getString("password");
                String role = rs.getString("role");
                boolean enabled = rs.getBoolean("enabled");
                List<GrantedAuthority> auth =
                        List.of(new SimpleGrantedAuthority(role));

                return new CustomUserDetails(id, login, dbHash, enabled, auth);

            } catch (SQLException ex){
                ex.printStackTrace();
                throw new UsernameNotFoundException("SQL error", ex);
            }
            finally {
                pool.release(c);
            }

        } catch (InterruptedException e) {
            throw new UsernameNotFoundException("DB error", e);
        }
    }
}
