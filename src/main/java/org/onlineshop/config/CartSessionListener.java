package org.onlineshop.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.onlineshop.dao.CartDao;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@WebListener
public class CartSessionListener implements HttpSessionListener {
    @Override
    public void sessionCreated(HttpSessionEvent se) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        ServletContext servletCtx = se.getSession().getServletContext();
        WebApplicationContext springCtx =
                WebApplicationContextUtils.getWebApplicationContext(servletCtx);

        if (springCtx != null) {
            CartDao cartDao = springCtx.getBean(CartDao.class);

            Object attr = se.getSession().getAttribute("cartId");
            if (attr instanceof Integer cartId) {
                try {
                    cartDao.deleteEmptyCartById(cartId);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
