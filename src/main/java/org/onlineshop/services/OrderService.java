package org.onlineshop.services;

import org.onlineshop.dao.CartDao;
import org.onlineshop.dao.OrderDao;
import org.onlineshop.config.CustomUserDetails;
import org.onlineshop.dto.CartItemDto;
import org.onlineshop.dto.OrderDto;
import org.onlineshop.dto.OrderItemDto;
import org.onlineshop.dto.OrderPageDto;
import org.onlineshop.dto.mappers.OrderMapper;
import org.onlineshop.model.CartItem;
import org.onlineshop.model.Order;
import org.onlineshop.model.OrderItem;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        List<CartItem> cartItems = cartDao.getCartItemsByUserId(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            return;
        }

        BigDecimal total = cartItems.stream()
                .map(ci -> ci.getPrice().multiply(
                        BigDecimal.valueOf(ci.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int orderId = orderDao.saveOrder(userId, total);

        List<OrderItem> orderItems = cartItems.stream()
                .map(ci -> {
                    OrderItem oi = new OrderItem();
                    oi.setOrderId(orderId);
                    oi.setProductId(ci.getProductId());
                    oi.setProductName(ci.getName());
                    oi.setQuantity(ci.getQty());
                    oi.setPrice(ci.getPrice());
                    return oi;
                })
                .collect(Collectors.toList());

        orderDao.saveOrderItems(orderId, orderItems);

        cartService.clearCart(userId);
    }

    public OrderPageDto getOrdersForCurrentUser(int pageNum, int pageSize) throws SQLException, InterruptedException {
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

        List<Order> entities = orderDao.findOrderHeadersByUserId(userId, pageSize, offset);
        for (Order o : entities) {
            o.setItems(orderDao.findOrderItemsByOrderId(o.getId()));
        }

        List<OrderDto> headers = entities.stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());

        OrderPageDto pageDto = new OrderPageDto();
        pageDto.setOrders(headers);
        pageDto.setCurrentPage(pageNum);
        pageDto.setTotalPages(totalPages);

        return pageDto;
    }
}
