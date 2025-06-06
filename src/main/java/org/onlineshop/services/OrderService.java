package org.onlineshop.services;

import org.onlineshop.dao.CartDao;
import org.onlineshop.dao.OrderDao;
import org.onlineshop.db.CustomUserDetails;
import org.onlineshop.dto.CartItemDto;
import org.onlineshop.dto.order.OrderDto;
import org.onlineshop.dto.order.OrderItemDto;
import org.onlineshop.dto.order.OrderPageDto;
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

    public OrderPageDto  getOrdersForCurrentUser(int pageNum, int pageSize) throws SQLException, InterruptedException {
        int userId = getCurrentUserId();

        int totalOrders = orderDao.countOrdersByUserId(userId);

        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);
        if (totalPages < 1) {
            totalPages = 1;
        }
        if (pageNum < 1) {
            pageNum = 1;
        } else if (pageNum > totalPages) {
            pageNum = totalPages;
        }

        int offset = (pageNum - 1) * pageSize;

        List<OrderDto> headers = orderDao.findOrderHeadersByUserId(userId, pageSize, offset);

        for (OrderDto header : headers) {
            List<OrderItemDto> items = orderDao.findOrderItemsByOrderId(header.getId());
            header.setItems(items);
        }

        OrderPageDto pageDto = new OrderPageDto();
        pageDto.setOrders(headers);
        pageDto.setCurrentPage(pageNum);
        pageDto.setTotalPages(totalPages);

        return pageDto;
    }
}
