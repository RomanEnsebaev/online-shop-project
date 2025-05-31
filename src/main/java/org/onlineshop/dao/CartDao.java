package org.onlineshop.dao;

import org.onlineshop.db.ConnectionPool;
import org.onlineshop.dto.CartItemDto;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CartDao {
    private final ConnectionPool pool;

    public CartDao(ConnectionPool pool) { this.pool = pool; }

    public int resolveCart(Integer userId, String sessionId)
            throws SQLException, InterruptedException {

        String findSql = "SELECT cart_id FROM carts WHERE "
                + (userId != null ? "user_id = ?" : "session_id = ?");
        String insertSql = "INSERT INTO carts(user_id, session_id) VALUES (?,?) RETURNING cart_id";

        Connection c = pool.borrow();
        try (PreparedStatement ps = c.prepareStatement(findSql)) {
            ps.setObject(1, userId != null ? userId : sessionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

            try (PreparedStatement ins = c.prepareStatement(insertSql)) {
                ins.setObject(1, userId);
                ins.setObject(2, sessionId);
                rs = ins.executeQuery();
                rs.next();
                return rs.getInt(1);
            }
        } finally { pool.release(c); }
    }

    public void mergeCarts(String sessionId, int userId)
            throws SQLException, InterruptedException {

        String sql = """
            WITH guest AS (
              SELECT cart_id FROM carts WHERE session_id = ?
            ), userc AS (
              SELECT cart_id FROM carts WHERE user_id = ?
            )
            INSERT INTO cart_items(cart_id, product_id, qty)
            SELECT u.cart_id, ci.product_id, ci.qty
            FROM guest g
            JOIN cart_items ci ON ci.cart_id = g.cart_id
            CROSS JOIN userc u
            ON CONFLICT (cart_id, product_id)
              DO UPDATE SET qty = cart_items.qty + EXCLUDED.qty;
            DELETE FROM carts WHERE session_id = ?;
            """;

        Connection c = pool.borrow();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            ps.setInt   (2, userId);
            ps.setString(3, sessionId);
            ps.executeUpdate();
        } finally { pool.release(c); }
    }

    public void addItem(int cartId, int productId, int qty)
            throws SQLException, InterruptedException {

        String sql = """
            INSERT INTO cart_items(cart_id, product_id, qty)
            VALUES (?,?,?)
            ON CONFLICT (cart_id, product_id)
              DO UPDATE SET qty = cart_items.qty + EXCLUDED.qty;
            """;
        Connection c = pool.borrow();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            ps.setInt(3, qty);
            ps.executeUpdate();
        } finally { pool.release(c); }
    }

    public List<CartItemDto> items(int cartId)
            throws SQLException, InterruptedException {

        String sql = """
            SELECT p.product_id, p.name, p.price, ci.qty
            FROM cart_items ci
            JOIN products p ON p.product_id = ci.product_id
            WHERE ci.cart_id = ?
            """;
        List<CartItemDto> list = new ArrayList<>();
        Connection c = pool.borrow();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                CartItemDto d = new CartItemDto();
                d.setProductId(rs.getInt(1));
                d.setName(rs.getString(2));
                d.setPrice(rs.getBigDecimal(3));
                d.setQty(rs.getInt(4));
                d.setLineTotal(d.getPrice().multiply(
                        new java.math.BigDecimal(d.getQty())));
                list.add(d);
            }
        } finally { pool.release(c); }
        return list;
    }

    public void deleteItem(int cartId, int productId)
            throws SQLException, InterruptedException {
        Connection c = pool.borrow();
        try (PreparedStatement ps = c.prepareStatement(
                "DELETE FROM cart_items WHERE cart_id=? AND product_id=?")) {
            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } finally { pool.release(c); }
    }
}
