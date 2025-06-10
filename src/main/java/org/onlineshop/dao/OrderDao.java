package org.onlineshop.dao;

import org.onlineshop.config.database.ConnectionPool;
import org.onlineshop.model.Order;
import org.onlineshop.model.OrderItem;
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

    public void saveOrderItems(int orderId, List<OrderItem> items) throws SQLException, InterruptedException {
        String sql = "INSERT INTO order_items (order_id, product_id, qty, price_at_purchase) VALUES (?, ?, ?, ?)";
        Connection c = pool.borrow();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (OrderItem  item : items) {
                ps.setInt(1, orderId);
                ps.setInt(2, item.getProductId());
                ps.setInt(3, item.getQuantity());
                ps.setBigDecimal(4, item.getPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        } finally { pool.release(c); }
    }

    public List<Order> findOrderHeadersByUserId(int userId, int limit, int offset) throws SQLException, InterruptedException {
        String sql = "SELECT order_id, created_at, total FROM orders WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Order> headers = new ArrayList<>();

        Connection c = pool.borrow();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order header = new Order();
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

    public List<OrderItem> findOrderItemsByOrderId(int orderId) throws SQLException, InterruptedException{
        String sql = "SELECT oi.product_id, p.name as product_name, oi.qty, oi.price_at_purchase " +
                "FROM order_items as oi " +
                "JOIN products as p ON oi.product_id = p.product_id " +
                "WHERE oi.order_id = ?";
        List<OrderItem> items = new ArrayList<>();

        Connection c = pool.borrow();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem it = new OrderItem();
                    it.setOrderId(orderId);
                    it.setProductId(rs.getInt("product_id"));
                    it.setProductName(rs.getString("product_name"));
                    it.setQuantity(rs.getInt("qty"));
                    it.setPrice(rs.getBigDecimal("price_at_purchase"));
                    items.add(it);
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
