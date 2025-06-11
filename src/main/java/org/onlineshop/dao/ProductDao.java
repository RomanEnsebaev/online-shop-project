package org.onlineshop.dao;

import org.onlineshop.config.database.ConnectionPool;
import org.onlineshop.model.Product;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@Repository
public class ProductDao {
    private final ConnectionPool pool;
    private static final Logger log = LoggerFactory.getLogger(ProductDao.class);

    public ProductDao(ConnectionPool pool) {
        this.pool = pool;
    }

    public List<Product> findAllActive() {
        final String sql = "SELECT * FROM products WHERE active = TRUE ORDER BY product_id";
        List<Product> list = new ArrayList<>();

        try {
            Connection c = pool.borrow();
            try (PreparedStatement ps = c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) list.add(map(rs));
            } finally { pool.release(c); }
        } catch (SQLException | InterruptedException ex) {
            log.error("ProductDao.findAllActive failed", ex);
            throw new RuntimeException("Ошибка при получении списка продуктов", ex);
        }
        return list;
    }

    public void save(Product p) {
        final String sql = """
            INSERT INTO products(name, description, price, stock_qty, active)
            VALUES (?,?,?,?,?)
            """;
        try {
            Connection c = pool.borrow();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString   (1, p.getName());
                ps.setString   (2, p.getDescription());
                ps.setBigDecimal(3, p.getPrice());
                ps.setInt      (4, p.getStockQty());
                ps.setBoolean  (5, p.isActive());
                ps.executeUpdate();
            } finally {
                pool.release(c);
            }
        } catch (SQLException | InterruptedException ex) {
            log.error("ProductDao.save failed for product={}", p, ex);
            throw new RuntimeException("Ошибка при сохранении продукта", ex);
        }
    }

    public void update(Product p) {
        final String sql = """
            UPDATE products
               SET name        = ? ,
                   description = ? ,
                   price       = ? ,
                   stock_qty   = ? ,
                   active      = ?
             WHERE product_id  = ?
            """;
        try {
            Connection c = pool.borrow();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString   (1, p.getName());
                ps.setString   (2, p.getDescription());
                ps.setBigDecimal(3, p.getPrice());
                ps.setInt      (4, p.getStockQty());
                ps.setBoolean  (5, p.isActive());
                ps.setInt      (6, p.getProductId());
                ps.executeUpdate();
            } finally { pool.release(c); }
        } catch (SQLException | InterruptedException ex) {
            log.error("ProductDao.update failed for productId={}", p.getProductId(), ex);
            throw new RuntimeException("Ошибка при обновлении продукта", ex);
        }
    }

    public void delete(int id) {
        try {
            Connection c = pool.borrow();
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE products SET active = FALSE WHERE product_id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            } finally { pool.release(c); }
        } catch (SQLException | InterruptedException ex) {
            log.error("ProductDao.delete failed for productId={}", id, ex);
            throw new RuntimeException("Ошибка при удалении продукта", ex);
        }
    }

    public Product find(int id) {
        final String sql = "SELECT * FROM products WHERE product_id = ?";
        try {
            Connection c = pool.borrow();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? map(rs) : null;
                }
            } finally { pool.release(c); }
        } catch (SQLException | InterruptedException ex) {
            log.error("ProductDao.find failed for productId={}", id, ex);
            throw new RuntimeException("Ошибка при поиске продукта", ex);
        }
    }

    private static Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId (rs.getInt("product_id"));
        p.setName      (rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice     (rs.getBigDecimal("price"));
        p.setStockQty  (rs.getInt("stock_qty"));
        p.setActive    (rs.getBoolean("active"));
        p.setCreatedAt (rs.getTimestamp("created_at").toLocalDateTime());
        return p;
    }
}
