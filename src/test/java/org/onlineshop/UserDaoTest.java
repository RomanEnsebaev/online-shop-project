
package org.onlineshop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onlineshop.dao.UserDao;
import org.onlineshop.db.ConnectionPool;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserDaoTest {

    @Mock ConnectionPool pool;
    @Mock PasswordEncoder encoder;
    @Mock Connection connection;
    @Mock PreparedStatement ps;
    @Mock ResultSet rs;
    @InjectMocks
    UserDao dao;

    private final String sql = "SELECT 1 FROM users WHERE username = ?";

    @BeforeEach
    void setUp() throws SQLException, InterruptedException {
        when(pool.borrow()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(ps);
    }

    /**
     * Должен вернуть true, когда в БД есть запись с переданным именем пользователя.
     */
    @Test
    void shouldReturnTrueWhenUserExists() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        boolean exists = dao.exists("alice");

        assertTrue(exists);
        verify(ps).setString(1, "alice");
        verify(rs).close();
        verify(ps).close();
        verify(pool).release(connection);
    }

    /**
     * Должен вернуть false, когда в БД нет записи с переданным именем пользователя.
     */
    @Test
    void shouldReturnFalseWhenUserDoesNotExist() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        boolean exists = dao.exists("bob");

        assertFalse(exists);
        verify(ps).setString(1, "bob");
        verify(rs).close();
        verify(ps).close();
        verify(pool).release(connection);
    }

    /**
     * Должен обернуть SQLException в RuntimeException и освободить соединение.
     */
    @Test
    void shouldWrapSQLExceptionInRuntimeException() throws SQLException {
        when(connection.prepareStatement(sql)).thenThrow(new SQLException("db error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> dao.exists("charlie"));
        assertTrue(ex.getCause() instanceof SQLException);
        verify(pool).release(connection);
    }

    /**
     * Должен обернуть InterruptedException в RuntimeException.
     */
    @Test
    void shouldWrapInterruptedExceptionInRuntimeException() throws InterruptedException {
        when(pool.borrow()).thenThrow(new InterruptedException("interrupted"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> dao.exists("dave"));
        assertTrue(ex.getCause() instanceof InterruptedException);
        verify(pool, never()).release(any());
    }
}

