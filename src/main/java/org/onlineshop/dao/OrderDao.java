package org.onlineshop.dao;

import org.onlineshop.db.ConnectionPool;
import org.onlineshop.dto.order.OrderDto;
import org.onlineshop.dto.order.OrderItemDto;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderDao {
    private final ConnectionPool pool;

    public OrderDao(ConnectionPool pool) {
        this.pool = pool;
    }

    public int saveOrder(int userId, BigDecimal total) throws SQLException, InterruptedException {
        String sql = "INSERT INTO orders (user_id, total) VALUES (?, ?) RETURNING order_id";

        Connection c = pool.borrow();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setBigDecimal(2, total);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("order_id");
                } else {
                    throw new SQLException("Не удалось получить сгенерированный ID заказа");
                }
            }
        } finally { pool.release(c); }
    }

    public void saveOrderItems(int orderId, List<OrderItemDto> items) throws SQLException, InterruptedException {
        String sql = "INSERT INTO order_items (order_id, product_id, qty, price_at_purchase) VALUES (?, ?, ?, ?)";
        Connection c = pool.borrow();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (OrderItemDto item : items) {
                ps.setInt(1, orderId);
                ps.setInt(2, item.getProductId());
                ps.setInt(3, item.getQuantity());
                ps.setBigDecimal(4, item.getPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        } finally { pool.release(c); }
    }

    public List<OrderDto> findOrderHeadersByUserId(int userId, int limit, int offset) throws SQLException, InterruptedException {
        String sql = "SELECT order_id, created_at, total FROM orders WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<OrderDto> headers = new ArrayList<>();

        Connection c = pool.borrow();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDto header = new OrderDto();
                    header.setId(rs.getInt("order_id"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    header.setOrderDate(ts.toLocalDateTime());
                    header.setTotal(rs.getBigDecimal("total"));
                    headers.add(header);
                }
            }
        } finally { pool.release(c); }

        return headers;
    }

    public List<OrderItemDto> findOrderItemsByOrderId(int orderId) throws SQLException, InterruptedException{
        String sql = "SELECT oi.product_id, p.name as product_name, oi.qty, oi.price_at_purchase " +
                "FROM order_items as oi " +
                "JOIN products as p ON oi.product_id = p.product_id " +
                "WHERE oi.order_id = ?";
        List<OrderItemDto> items = new ArrayList<>();

        Connection c = pool.borrow();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int pid = rs.getInt("product_id");
                    String productName = rs.getString("product_name");
                    int quantity = rs.getInt("qty");
                    BigDecimal price = rs.getBigDecimal("price_at_purchase");
                    items.add(new OrderItemDto(pid, productName, quantity, price));
                }
            }
        } finally { pool.release(c); }
        return items;
    }

    public int countOrdersByUserId(int userId) throws InterruptedException, SQLException {
        String sql = "SELECT COUNT(*) AS total_count FROM orders WHERE user_id = ?";
        Connection c = pool.borrow();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_count");
                } else {
                    return 0;
                }
            }
        } finally {
            pool.release(c);
        }
    }

}
