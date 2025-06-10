
package org.onlineshop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onlineshop.dao.CartDao;
import org.onlineshop.config.ConnectionPool;
import org.onlineshop.dto.CartItemDto;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onlineshop.model.CartItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartDaoTest {

    @Mock
    ConnectionPool pool;

    @Mock
    Connection connection;

    @Mock
    PreparedStatement ps;

    @Mock
    ResultSet rs;

    @InjectMocks
    @Spy
    CartDao dao;

    @BeforeEach
    void init() throws InterruptedException {
        lenient().when(pool.borrow()).thenReturn(connection);
    }


    /**
     * Должен вернуть существующий cart_id для авторизованного пользователя
     */
    @Test
    void shouldReturnExistingCartIdWhenUserExists() throws Exception {
        when(connection.prepareStatement(startsWith("SELECT cart_id"))).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(123);

        int id = dao.resolveCart(7, "ignored");

        assertEquals(123, id);
        verify(ps).setObject(1, 7);
        verify(pool).release(connection);
    }

    /**
     * Должен создать новую запись и вернуть сгенерированный cart_id при отсутствии записи
     */
    @Test
    void shouldCreateNewCartWhenNoneExists() throws Exception {
        when(connection.prepareStatement(startsWith("SELECT cart_id"))).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        PreparedStatement psIns = mock(PreparedStatement.class);
        ResultSet rsGen = mock(ResultSet.class);
        when(connection.prepareStatement(startsWith("INSERT INTO carts"))).thenReturn(psIns);
        when(psIns.executeQuery()).thenReturn(rsGen);
        when(rsGen.next()).thenReturn(true);
        when(rsGen.getInt(1)).thenReturn(77);

        int newId = dao.resolveCart(null, "sess-1");

        assertEquals(77, newId);
        verify(psIns).setObject(1, null);
        verify(psIns).setObject(2, "sess-1");
        verify(pool).release(connection);
    }

    /**
     * Должен выбросить SQLException и освободить соединение при ошибке resolveCart
     */
    @Test
    void shouldThrowSQLExceptionWhenResolveCartFails() throws InterruptedException, SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));
        assertThrows(SQLException.class, () -> dao.resolveCart(null, "s"));
        verify(pool).release(connection);
    }

    /**
     * Должен объединить корзины с правильными параметрами
     */
    @Test
    void shouldMergeCartsWithCorrectParameters() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);

        dao.mergeCarts("token-1", 5);

        verify(ps).setString(1, "token-1");
        verify(ps).setInt(2, 5);
        verify(ps).setString(3, "token-1");
        verify(ps).executeUpdate();
        verify(pool).release(connection);
    }

    /**
     * Должен выбросить SQLException и освободить соединение при ошибке mergeCarts
     */
    @Test
    void shouldThrowSQLExceptionWhenMergeCartsFails() throws InterruptedException, SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));
        assertThrows(SQLException.class, () -> dao.mergeCarts("tok", 1));
        verify(pool).release(connection);
    }


    /**
     * Должен добавить элемент в корзину и обновить количество
     */
    @Test
    void shouldAddItemAndUpdateWhenCalled() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);

        dao.addItem(3, 4, 2);

        verify(ps).setInt(1, 3);
        verify(ps).setInt(2, 4);
        verify(ps).setInt(3, 2);
        verify(ps).executeUpdate();
        verify(pool).release(connection);
    }

    /**
     * Должен выбросить SQLException и освободить соединение при ошибке addItem
     */
    @Test
    void shouldThrowSQLExceptionWhenAddItemFails() throws InterruptedException, SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));
        assertThrows(SQLException.class, () -> dao.addItem(1, 2, 3));
        verify(pool).release(connection);
    }


    /**
     * Должен вернуть список позиций корзины с расчётом итоговой стоимости
     */
    @Test
    void shouldReturnItemsListWhenExists() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt(1)).thenReturn(10, 20);
        when(rs.getString(2)).thenReturn("X", "Y");
        when(rs.getBigDecimal(3)).thenReturn(new BigDecimal("2.50"), new BigDecimal("3.00"));
        when(rs.getInt(4)).thenReturn(2, 1);

        List<CartItem> list = dao.items(5);

        assertEquals(2, list.size());
        assertEquals(10, list.get(0).getProductId());
        assertEquals(new BigDecimal("5.00"), list.get(0).getLineTotal());
        verify(pool).release(connection);
    }

    /**
     * Должен вернуть пустой список, если в корзине нет позиций
     */
    @Test
    void shouldReturnEmptyListWhenNoItems() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        List<CartItem> list = dao.items(7);

        assertTrue(list.isEmpty());
        verify(pool).release(connection);
    }

    /**
     * Должен выбросить SQLException и освободить соединение при ошибке items
     */
    @Test
    void shouldThrowSQLExceptionWhenItemsFails() throws InterruptedException, SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));
        assertThrows(SQLException.class, () -> dao.items(1));
        verify(pool).release(connection);
    }

    /**
     * Должен удалить элемент из корзины по cartId и productId
     */
    @Test
    void shouldDeleteItemWhenCalled() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);

        dao.deleteItem(4, 5);

        verify(ps).setInt(1, 4);
        verify(ps).setInt(2, 5);
        verify(ps).executeUpdate();
        verify(pool).release(connection);
    }

    /**
     * Должен выбросить SQLException и освободить соединение при ошибке deleteItem
     */
    @Test
    void shouldThrowSQLExceptionWhenDeleteItemFails() throws InterruptedException, SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));
        assertThrows(SQLException.class, () -> dao.deleteItem(1, 1));
        verify(pool).release(connection);
    }

    /**
     * Должен очистить корзину пользователя и сбросить количество
     */
    @Test
    void shouldClearCartByUserIdWhenCalled() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);

        dao.clearCartByUserId(9);

        verify(ps).setInt(1, 9);
        verify(ps).executeUpdate();
        verify(pool).release(connection);
    }

    /**
     * Должен выбросить SQLException и освободить соединение при ошибке clearCartByUserId
     */
    @Test
    void shouldThrowSQLExceptionWhenClearCartFails() throws InterruptedException, SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));
        assertThrows(SQLException.class, () -> dao.clearCartByUserId(2));
        verify(pool).release(connection);
    }

    /**
     * Должен вернуть список позиций через getCartItemsByUserId
     */
    @Test
    void shouldReturnItemsWhenGetCartItemsByUserIdCalled() throws Exception {
        CartDao spyDao = spy(new CartDao(pool));
        doReturn(20).when(spyDao).resolveCart(2, null);
        List<CartItemDto> expected = List.of(new CartItemDto());
        doReturn(expected).when(spyDao).items(20);

        List<CartItem> result = spyDao.getCartItemsByUserId(2);

        assertSame(expected, result);
        verify(connection, never()).prepareStatement(anyString());
    }
}
