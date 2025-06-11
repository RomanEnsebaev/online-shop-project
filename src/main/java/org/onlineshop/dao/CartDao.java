package org.onlineshop.dao;

import org.onlineshop.config.database.ConnectionPool;
import org.onlineshop.model.CartItem;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CartDao {
    private final ConnectionPool pool;
    private static final Logger log = LoggerFactory.getLogger(CartDao.class);

    public CartDao(ConnectionPool pool) { this.pool = pool; }

    public int resolveCart(Integer userId, String sessionId) throws InterruptedException, SQLException {

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
        } catch (SQLException ex) {
            log.error("CartDao.resolveCart failed (sessionId={}, userId={})", sessionId, userId, ex);
            throw new SQLException("Ошибка доступа к данным корзины", ex);
        } finally { pool.release(c); }
    }

    public void mergeCarts(String sessionId, int userId)
            throws InterruptedException, SQLException {

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
        } catch (SQLException ex) {
            log.error("CartDao.mergeCarts failed (sessionId={}, userId={})", sessionId, userId, ex);
            throw new SQLException("Ошибка при объединении корзин", ex);
        } finally { pool.release(c); }
    }

    public void addItem(int cartId, int productId, int qty)
            throws InterruptedException, SQLException {

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
        } catch (SQLException  ex) {
            log.error("CartDao.addItem failed (cartId={}, productId={}, qty={})", cartId, productId, qty, ex);
            throw new SQLException("Ошибка при добавлении товара в корзину", ex);
        }finally { pool.release(c); }
    }

    public List<CartItem> items(int cartId)
            throws InterruptedException, SQLException {

        String sql = """
            SELECT p.product_id, p.name, p.price, ci.qty
            FROM cart_items ci
            JOIN products p ON p.product_id = ci.product_id
            WHERE ci.cart_id = ?
            """;
        List<CartItem> list = new ArrayList<>();
        Connection c = pool.borrow();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                CartItem item  = new CartItem();
                item .setProductId(rs.getInt(1));
                item .setName(rs.getString(2));
                item .setPrice(rs.getBigDecimal(3));
                item .setQty(rs.getInt(4));
                item .setLineTotal(item .getPrice().multiply(
                        new java.math.BigDecimal(item .getQty())));
                list.add(item );
            }
        } catch (SQLException ex) {
            log.error("CartDao.items failed for cartId={}", cartId, ex);
            throw new SQLException("Ошибка при получении товаров корзины", ex);
        }finally { pool.release(c); }
        return list;
    }

    public void deleteItem(int cartId, int productId)
            throws InterruptedException, SQLException {
        Connection c = pool.borrow();
        try (PreparedStatement ps = c.prepareStatement(
                "DELETE FROM cart_items WHERE cart_id=? AND product_id=?")) {
            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            log.error("CartDao.deleteItem failed (cartId={}, productId={})", cartId, productId, ex);
            throw new SQLException("Ошибка при удалении товара из корзины", ex);
        } finally { pool.release(c); }
    }

    public void clearCartByUserId(int userId) throws InterruptedException, SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_id = (SELECT cart_id FROM carts WHERE user_id = ?)";
        Connection c = pool.borrow();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            log.error("CartDao.clearCartByUserId failed for userId={}", userId, ex);
            throw new SQLException("Ошибка при очистке корзины пользователя", ex);
        } finally {
            pool.release(c);
        }
    }

    public List<CartItem> getCartItemsByUserId(int userId)
            throws InterruptedException, SQLException {
        try {
            int cartId = resolveCart(userId, null);
            return items(cartId);
        } catch (RuntimeException ex) {
            log.error("CartDao.getCartItemsByUserId failed for userId={}", userId, ex);
            throw new RuntimeException("Ошибка при загрузке корзины пользователя", ex);
        }
    }

    public void deleteEmptyCartById(int cartId) throws InterruptedException {
        String sql = """
        DELETE FROM cart
        WHERE cart_id = ?
          AND NOT EXISTS (
            SELECT 1 FROM cart_item ci
             WHERE ci.cart_id = cart.cart_id
          )
        """;
        Connection c = pool.borrow();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            log.error("CartDao.deleteEmptyCartById failed for cartId={}", cartId, ex);
            throw new RuntimeException("Ошибка при удалении пустой корзины " + cartId, ex);
        } finally {
            pool.release(c);
        }
    }
}
