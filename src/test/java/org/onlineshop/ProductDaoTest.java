// src/test/java/org/onlineshop/dao/ProductDaoTest.java
package org.onlineshop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onlineshop.dao.ProductDao;
import org.onlineshop.db.ConnectionPool;
import org.onlineshop.model.Product;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductDaoTest {

    @Mock ConnectionPool pool;
    @Mock Connection connection;
    @Mock PreparedStatement ps;
    @Mock ResultSet rs;
    @InjectMocks ProductDao dao;

    @BeforeEach
    void init() throws InterruptedException {
        when(pool.borrow()).thenReturn(connection);
    }

    /**
     * Должен вернуть список активных продуктов, когда в БД есть хотя бы один.
     */
    @Test
    void shouldReturnListOfActiveProductsWhenTheyExist() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt("product_id")).thenReturn(1, 2);
        when(rs.getString("name")).thenReturn("A", "B");
        when(rs.getString("description")).thenReturn("dA", "dB");
        when(rs.getBigDecimal("price")).thenReturn(new BigDecimal("1.00"), new BigDecimal("2.00"));
        when(rs.getInt("stock_qty")).thenReturn(10, 20);
        when(rs.getBoolean("active")).thenReturn(true, true);
        Timestamp t1 = Timestamp.valueOf("2025-01-01 00:00:00");
        Timestamp t2 = Timestamp.valueOf("2025-02-01 00:00:00");
        when(rs.getTimestamp("created_at")).thenReturn(t1, t2);

        List<Product> list = dao.findAllActive();

        assertEquals(2, list.size());
        assertEquals(1, list.get(0).getProductId());
        assertEquals("B", list.get(1).getName());
        verify(pool).release(connection);
    }

    /**
     * Должен вернуть пустой список, когда активных продуктов нет.
     */
    @Test
    void shouldReturnEmptyListWhenNoActiveProducts() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        List<Product> list = dao.findAllActive();

        assertTrue(list.isEmpty());
        verify(pool).release(connection);
    }

    /**
     * Должен выбросить RuntimeException, если чтение из БД упало.
     */
    @Test
    void shouldThrowRuntimeWhenFindAllActiveFails() throws InterruptedException, SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> dao.findAllActive());
        assertTrue(ex.getCause() instanceof SQLException);
        verify(pool).release(connection);
    }

    /**
     * Должен успешно сохранить продукт с правильными параметрами.
     */
    @Test
    void shouldSaveProductWithCorrectParameters() throws InterruptedException, SQLException {
        Product p = new Product();
        p.setName("X"); p.setDescription("dX"); p.setPrice(new BigDecimal("3.14"));
        p.setStockQty(5); p.setActive(true);

        when(connection.prepareStatement(anyString())).thenReturn(ps);

        dao.save(p);

        verify(ps).setString(1, "X");
        verify(ps).setString(2, "dX");
        verify(ps).setBigDecimal(3, new BigDecimal("3.14"));
        verify(ps).setInt(4, 5);
        verify(ps).setBoolean(5, true);
        verify(ps).executeUpdate();
        verify(pool).release(connection);
    }

    /**
     * Должен выбросить RuntimeException, если при сохранении упала БД.
     */
    @Test
    void shouldThrowRuntimeWhenSaveFails() throws InterruptedException, SQLException {
        Product p = new Product();
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));
        assertThrows(RuntimeException.class, () -> dao.save(p));
    }

    /**
     * Должен обновить продукт с правильными параметрами.
     */
    @Test
    void shouldUpdateProductWithCorrectParameters() throws InterruptedException, SQLException {
        Product p = new Product();
        p.setProductId(7);
        p.setName("U"); p.setDescription("dU");
        p.setPrice(new BigDecimal("7.77")); p.setStockQty(2); p.setActive(false);

        when(connection.prepareStatement(anyString())).thenReturn(ps);

        dao.update(p);

        verify(ps).setString(1, "U");
        verify(ps).setString(2, "dU");
        verify(ps).setBigDecimal(3, new BigDecimal("7.77"));
        verify(ps).setInt(4, 2);
        verify(ps).setBoolean(5, false);
        verify(ps).setInt(6, 7);
        verify(ps).executeUpdate();
        verify(pool).release(connection);
    }

    /**
     * Должен выбросить RuntimeException, если при обновлении упала БД.
     */
    @Test
    void shouldThrowRuntimeWhenUpdateFails() throws InterruptedException, SQLException {
        Product p = new Product();
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));
        assertThrows(RuntimeException.class, () -> dao.update(p));
    }

    /**
     * Должен пометить продукт неактивным (active=false) при вызове delete.
     */
    @Test
    void shouldDeactivateProductWhenDeleteCalled() throws InterruptedException, SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(ps);

        dao.delete(13);

        verify(ps).setInt(1, 13);
        verify(ps).executeUpdate();
        verify(pool).release(connection);
    }

    /**
     * Должен выбросить RuntimeException, если при delete упала БД.
     */
    @Test
    void shouldThrowRuntimeWhenDeleteFails() throws InterruptedException, SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));
        assertThrows(RuntimeException.class, () -> dao.delete(1));
    }

    /**
     * Должен вернуть сущность при существующем ID.
     */
    @Test
    void shouldReturnProductWhenFoundById() throws InterruptedException, SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("product_id")).thenReturn(42);
        when(rs.getString("name")).thenReturn("T");
        when(rs.getString("description")).thenReturn("D");
        when(rs.getBigDecimal("price")).thenReturn(new BigDecimal("4.44"));
        when(rs.getInt("stock_qty")).thenReturn(8);
        when(rs.getBoolean("active")).thenReturn(true);
        Timestamp ts = Timestamp.valueOf("2025-06-01 12:00:00");
        when(rs.getTimestamp("created_at")).thenReturn(ts);

        Product p = dao.find(42);

        assertNotNull(p);
        assertEquals(42, p.getProductId());
        verify(pool).release(connection);
    }

    /**
     * Должен вернуть null при отсутствии продукта с данным ID.
     */
    @Test
    void shouldReturnNullWhenProductNotFoundById() throws InterruptedException, SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertNull(dao.find(99));
        verify(pool).release(connection);
    }

    /**
     * Должен выбросить RuntimeException, если при find(id) упала БД.
     */
    @Test
    void shouldThrowRuntimeWhenFindByIdFails() throws InterruptedException, SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));
        assertThrows(RuntimeException.class, () -> dao.find(5));
    }
}
