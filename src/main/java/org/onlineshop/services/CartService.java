package org.onlineshop.services;

import jakarta.servlet.http.HttpSession;
import org.onlineshop.dao.CartDao;
import org.onlineshop.dto.CartItemDto;
import org.onlineshop.config.security.CustomUserDetails;
import org.onlineshop.dto.mappers.CartItemMapper;
import org.onlineshop.model.CartItem;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    private static final Logger log = LogManager.getLogger(CartService.class);
    private final CartDao dao;
    private final HttpSession session;

    public CartService(CartDao dao, HttpSession session) {
        this.dao = dao;
        this.session = session;
    }

    public void addToCart(int productId) throws SQLException, InterruptedException {
        try {
            int id = dao.resolveCart(currentUserId(), guestToken());
            dao.addItem(id, productId, 1);
            session.setAttribute("cartCount",
                    (Integer) Optional.ofNullable(session.getAttribute("cartCount")).orElse(0) + 1);
        } catch (InterruptedException ex) {
            log.error("CartService.addItemToCart failed (productId={})", productId, ex);
            throw new RuntimeException("Не удалось добавить товар в корзину. Попробуйте позже.", ex);
        }
    }

    public List<CartItemDto> viewCart() throws InterruptedException, SQLException {
        try {
            int id = dao.resolveCart(currentUserId(), guestToken());
            List<CartItem> items = dao.items(id);
            return items.stream()
                    .map(CartItemMapper::toDto)
                    .toList();
        } catch (RuntimeException ex) {
            log.error("CartService.viewCart failed for userId={}", currentUserId(), ex);
            throw new RuntimeException("Не удалось загрузить корзину. Попробуйте позже.", ex);
        }
    }

    public void mergeAfterLogin() throws SQLException, InterruptedException {
        dao.mergeCarts(guestToken(), currentUserId());
        session.removeAttribute("GUEST_TOKEN");

        int newCount = dao.items(
                dao.resolveCart(currentUserId(), null)).size();
        session.setAttribute("cartCount", newCount);
    }

    private Integer currentUserId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return (a != null && a.isAuthenticated() && !"anonymousUser".equals(a.getName()))
                ? ((CustomUserDetails) a.getPrincipal()).id()
                : null;
    }

    private String guestToken() {
        String token = (String) session.getAttribute("GUEST_TOKEN");
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute("GUEST_TOKEN", token);
        }
        return token;
    }

    public void clearCart(int userId) throws SQLException, InterruptedException {
        dao.clearCartByUserId(userId);
        session.setAttribute("cartCount", 0);
    }
}
