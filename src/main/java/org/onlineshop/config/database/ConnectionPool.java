package org.onlineshop.config.database;

import jakarta.annotation.PreDestroy;

import java.sql.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionPool {

    private final BlockingQueue<Connection> queue;

    public ConnectionPool(String url, String user, String pass, int size)
            throws SQLException {

        try { Class.forName("org.postgresql.Driver"); }
        catch (ClassNotFoundException e) { throw new SQLException(e); }

        queue = new ArrayBlockingQueue<>(size);
        for (int i = 0; i < size; i++) {
            Connection c = DriverManager.getConnection(url, user, pass);
            c.setAutoCommit(true);
            queue.add(c);
        }
    }

    private static class Holder {
        private static ConnectionPool INSTANCE;
    }

    public static synchronized void init(String url,
                                         String user,
                                         String pass,
                                         int size) throws SQLException {

        if (Holder.INSTANCE != null)
            return;
        Holder.INSTANCE = new ConnectionPool(url, user, pass, size);
    }

    public static ConnectionPool get() {
        if (Holder.INSTANCE == null)
            throw new IllegalStateException("Pool not initialised");
        return Holder.INSTANCE;
    }

    public Connection borrow() throws InterruptedException { return queue.take(); }

    public void release(Connection c) { queue.offer(c); }

    @PreDestroy
    public void closeAll() throws SQLException {
        for (Connection c : queue) c.close();
    }
}
