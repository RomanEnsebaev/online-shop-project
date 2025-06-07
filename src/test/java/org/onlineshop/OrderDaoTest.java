
package org.onlineshop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onlineshop.dao.OrderDao;
import org.onlineshop.db.ConnectionPool;
import org.onlineshop.dto.order.OrderDto;
import org.onlineshop.dto.order.OrderItemDto;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderDaoTest {

    @Mock ConnectionPool pool;
    @Mock Connection connection;
    @Mock PreparedStatement ps;
    @Mock ResultSet rs;
    @InjectMocks
    OrderDao dao;

    @BeforeEach
    void init() throws InterruptedException {
        when(pool.borrow()).thenReturn(connection);
    }

    /**
     * Должен сохранить заказ и вернуть сгенерированный ID.
     */
    @Test
    void shouldReturnGeneratedOrderIdWhenSaveOrderSucceeds() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("order_id")).thenReturn(123);

        int id = dao.saveOrder(10, new BigDecimal("99.99"));

        assertEquals(123, id);
        verify(ps).setInt(1, 10);
        verify(ps).setBigDecimal(2, new BigDecimal("99.99"));
        verify(pool).release(connection);
    }

    /**
     * Должен бросить SQLException, если в saveOrder нет возвращаемого ID.
     */
    @Test
    void shouldThrowSQLExceptionWhenSaveOrderFailsToGenerateId() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertThrows(SQLException.class, () -> dao.saveOrder(1, BigDecimal.ONE));
        verify(pool).release(connection);
    }

    /**
     * Должен сохранить список позиций заказа батчем.
     */
    @Test
    void shouldBatchInsertOrderItemsWhenSaveOrderItemsCalled() throws Exception {
        OrderItemDto item = new OrderItemDto(5, "n", 2, new BigDecimal("3.00"));
        when(connection.prepareStatement(anyString())).thenReturn(ps);

        dao.saveOrderItems(7, List.of(item, item));

        verify(ps, times(2)).addBatch();
        verify(ps).executeBatch();
        verify(pool).release(connection);
    }

    /**
     * Должен выбросить SQLException, если insert order_items упал.
     */
    @Test
    void shouldThrowSQLExceptionWhenSaveOrderItemsFails() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));
        assertThrows(SQLException.class, () -> dao.saveOrderItems(1, List.of()));
        verify(pool).release(connection);
    }

    /**
     * Должен вернуть заголовки заказов пользователя.
     */
    @Test
    void shouldReturnOrderHeadersWhenTheyExist() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("order_id")).thenReturn(50);
        Timestamp ts = Timestamp.valueOf("2025-06-05 10:00:00");
        when(rs.getTimestamp("created_at")).thenReturn(ts);
        when(rs.getBigDecimal("total")).thenReturn(new BigDecimal("20.00"));

        List<OrderDto> list = dao.findOrderHeadersByUserId(3, 5, 0);

        assertEquals(1, list.size());
        assertEquals(50, list.get(0).getId());
        verify(pool).release(connection);
    }

    /**
     * Должен вернуть пустой список, если заголовков нет.
     */
    @Test
    void shouldReturnEmptyListWhenNoOrderHeaders() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertTrue(dao.findOrderHeadersByUserId(3, 5, 0).isEmpty());
        verify(pool).release(connection);
    }

    /**
     * Должен вернуть позиции заказа по ID.
     */
    @Test
    void shouldReturnOrderItemsWhenTheyExist() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("product_id")).thenReturn(8);
        when(rs.getString("product_name")).thenReturn("P");
        when(rs.getInt("qty")).thenReturn(3);
        when(rs.getBigDecimal("price_at_purchase")).thenReturn(new BigDecimal("9.99"));

        List<OrderItemDto> items = dao.findOrderItemsByOrderId(4);

        assertEquals(1, items.size());
        assertEquals(8, items.get(0).getProductId());
        verify(pool).release(connection);
    }

    /**
     * Должен вернуть пустой список, если позиций нет.
     */
    @Test
    void shouldReturnEmptyListWhenNoOrderItems() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertTrue(dao.findOrderItemsByOrderId(4).isEmpty());
        verify(pool).release(connection);
    }

    /**
     * Должен вернуть корректное число заказов пользователя.
     */
    @Test
    void shouldReturnCorrectCountWhenOrdersExist() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("total_count")).thenReturn(7);

        int count = dao.countOrdersByUserId(2);
        assertEquals(7, count);
        verify(pool).release(connection);
    }

    /**
     * Должен вернуть 0, если запрос count не вернул строк.
     */
    @Test
    void shouldReturnZeroWhenCountReturnsNoRows() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertEquals(0, dao.countOrdersByUserId(2));
        verify(pool).release(connection);
    }
}
