package org.onlineshop.services;

import org.onlineshop.dao.CartDao;
import org.onlineshop.dao.OrderDao;
import org.onlineshop.db.CustomUserDetails;
import org.onlineshop.dto.CartItemDto;
import org.onlineshop.dto.OrderDto;
import org.onlineshop.dto.OrderItemDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    private final OrderDao orderDao;
    private final CartDao cartDao;   // чтобы взять содержимое корзины
    private final CartService cartService;

    public OrderService(OrderDao orderDao, CartDao cartDao, CartService cartService) {
        this.orderDao = orderDao;
        this.cartDao = cartDao;
        this.cartService = cartService;
    }

    private int getCurrentUserId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || "anonymousUser".equals(a.getName())) {
            throw new IllegalStateException("Пользователь не аутентифицирован");
        }
        return ((CustomUserDetails) a.getPrincipal()).id();
    }

    public void placeOrder() throws SQLException, InterruptedException {
        int userId = getCurrentUserId();

        List<CartItemDto> cartItems = cartDao.getCartItemsByUserId(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CartItemDto ci : cartItems) {
            BigDecimal line = ci.getPrice().multiply(BigDecimal.valueOf(ci.getQty()));
            total = total.add(line);
        }

        int orderId = orderDao.saveOrder(userId, total);

        List<OrderItemDto> itemsToSave = new ArrayList<>();
        for (CartItemDto ci : cartItems) {
            OrderItemDto oItem = new OrderItemDto(
                    ci.getProductId(),
                    ci.getName(),
                    ci.getQty(),
                    ci.getPrice()
            );
            itemsToSave.add(oItem);
        }
        orderDao.saveOrderItems(orderId, itemsToSave);

        cartService.clearCart(userId);
    }

    public List<OrderDto> getOrdersForCurrentUser() throws SQLException, InterruptedException {
        int userId = getCurrentUserId();

        List<OrderDto> headers = orderDao.findOrderHeadersByUserId(userId);

        for (OrderDto header : headers) {
            List<OrderItemDto> items = orderDao.findOrderItemsByOrderId(header.getId());
            header.setItems(items);
        }
        return headers;
    }
}
