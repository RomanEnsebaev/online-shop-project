
package org.onlineshop;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onlineshop.dao.CartDao;
import org.onlineshop.dao.OrderDao;
import org.onlineshop.config.security.CustomUserDetails;
import org.onlineshop.dto.OrderPageDto;
import org.onlineshop.model.CartItem;
import org.onlineshop.model.Order;
import org.onlineshop.model.OrderItem;
import org.onlineshop.services.CartService;
import org.onlineshop.services.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderDao orderDao;
    @Mock CartDao cartDao;
    @Mock CartService cartService;
    @Mock HttpSession session;
    @InjectMocks OrderService service;

    private MockedStatic<SecurityContextHolder> mockSecurityContext(int userId) {
        MockedStatic<SecurityContextHolder> sc = mockStatic(SecurityContextHolder.class);
        Authentication auth = mock(Authentication.class);
        CustomUserDetails cud = mock(CustomUserDetails.class);
        when(cud.id()).thenReturn(userId);
        when(auth.getPrincipal()).thenReturn(cud);
        when(auth.isAuthenticated()).thenReturn(true);

        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        sc.when(SecurityContextHolder::getContext).thenReturn(ctx);
        return sc;
    }

    /**
     * Должен бросить IllegalStateException, если пользователь не аутентифицирован.
     */
    @Test
    void shouldThrowWhenNotAuthenticatedOnPlaceOrder() {
        assertThrows(IllegalStateException.class, () -> service.placeOrder());
    }

    /**
     * Не должен вызывать DAO, если корзина пуста или null.
     */
    @Test
    void shouldDoNothingWhenCartIsEmpty() throws Exception {
        var sc = mockSecurityContext(5);
        when(cartDao.getCartItemsByUserId(5)).thenReturn(List.of());

        service.placeOrder();

        verify(orderDao, never()).saveOrder(anyInt(), any());
        verify(cartService, never()).clearCart(anyInt());
        sc.close();
    }

    /**
     * Должен сохранить заказ и очистить корзину, когда есть элементы.
     */
    @Test
    void shouldSaveOrderAndClearCartWhenCartHasItems() throws Exception {
        var sc = mockSecurityContext(5);

        CartItem ci = new CartItem();
        ci.setProductId(2);
        ci.setName("n");
        ci.setQty(3);
        ci.setPrice(new BigDecimal("4.00"));

        when(cartDao.getCartItemsByUserId(5)).thenReturn(List.of(ci));
        when(orderDao.saveOrder(5, new BigDecimal("12.00"))).thenReturn(77);

        service.placeOrder();

        verify(orderDao).saveOrder(5, new BigDecimal("12.00"));
        verify(orderDao).saveOrderItems(eq(77), anyList());
        verify(cartService).clearCart(5);
        sc.close();
    }

    /**
     * Должен вернуть пустую страницу, когда у пользователя нет заказов.
     */
    @Test
    void shouldReturnEmptyPageWhenNoOrders() throws Exception {
        var sc = mockSecurityContext(3);
        when(orderDao.countOrdersByUserId(3)).thenReturn(0);

        OrderPageDto page = service.getOrdersForCurrentUser(1, 5);

        assertEquals(1, page.getCurrentPage());
        assertEquals(1, page.getTotalPages());
        assertTrue(page.getOrders().isEmpty());
        sc.close();
    }

    /**
     * Должен скорректировать pageNum за пределами и вернуть заполненную страницу.
     */
    @Test
    void shouldReturnCorrectPageWhenOrdersExist() throws Exception {
        var sc = mockSecurityContext(3);
        when(orderDao.countOrdersByUserId(3)).thenReturn(5);
        Order h1 = new Order();
        h1.setId(10);
        Order h2 = new Order();
        h2.setId(11);
        when(orderDao.findOrderHeadersByUserId(3, 2, 4)).thenReturn(List.of(h1, h2));

        OrderItem item1 = new OrderItem();
        item1.setProductId(1);
        item1.setProductName("n");
        item1.setQuantity(1);
        item1.setPrice(new BigDecimal("1.00"));
        when(orderDao.findOrderItemsByOrderId(10)).thenReturn(List.of(item1));
        when(orderDao.findOrderItemsByOrderId(11)).thenReturn(List.of());

        OrderPageDto page = service.getOrdersForCurrentUser(5, 2);

        assertEquals(3, page.getCurrentPage());
        assertEquals(3, page.getTotalPages());
        assertEquals(2, page.getOrders().size());
        assertEquals(1, page.getOrders().get(0).getItems().size());
        sc.close();
    }

    /**
     * Должен бросить IllegalStateException при попытке getOrders без аутентификации.
     */
    @Test
    void shouldThrowWhenNotAuthenticatedOnGetOrders() {
        assertThrows(IllegalStateException.class, () -> service.getOrdersForCurrentUser(1,1));
    }
}
