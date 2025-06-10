
package org.onlineshop;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onlineshop.dao.CartDao;
import org.onlineshop.config.security.CustomUserDetails;
import org.onlineshop.dto.CartItemDto;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onlineshop.dto.mappers.CartItemMapper;
import org.onlineshop.model.CartItem;
import org.onlineshop.services.CartService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartDao dao;
    @Mock HttpSession session;
    @InjectMocks
    CartService service;

    /**
     * Должен добавить товар и увеличить счётчик для авторизованного пользователя
     */
    @Test
    void shouldAddToCartWhenUserAuthenticated() throws Exception {
        try (MockedStatic<SecurityContextHolder> sc = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            CustomUserDetails cud = mock(CustomUserDetails.class);
            when(cud.id()).thenReturn(7);
            when(auth.getPrincipal()).thenReturn(cud);
            when(auth.isAuthenticated()).thenReturn(true);
            SecurityContext ctx = mock(SecurityContext.class);
            when(ctx.getAuthentication()).thenReturn(auth);
            sc.when(SecurityContextHolder::getContext).thenReturn(ctx);

            when(session.getAttribute("GUEST_TOKEN")).thenReturn("tok");
            when(dao.resolveCart(7, "tok")).thenReturn(50);
            when(session.getAttribute("cartCount")).thenReturn(null);

            service.addToCart(5);

            verify(dao).resolveCart(7, "tok");
            verify(dao).addItem(50, 5, 1);
            verify(session).setAttribute("cartCount", 1);
        }
    }

    /**
     * Должен сгенерировать guest-token и добавить товар для неавторизованного пользователя
     */
    @Test
    void shouldAddToCartWhenGuestUser() throws Exception {
        try (MockedStatic<SecurityContextHolder> sc = mockStatic(SecurityContextHolder.class)) {
            SecurityContext ctx = mock(SecurityContext.class);
            when(ctx.getAuthentication()).thenReturn(null);
            sc.when(SecurityContextHolder::getContext).thenReturn(ctx);

            when(session.getAttribute("GUEST_TOKEN")).thenReturn(null);
            when(dao.resolveCart(isNull(), anyString())).thenReturn(200);

            service.addToCart(8);

            verify(session).setAttribute(eq("GUEST_TOKEN"), anyString());
            verify(dao).resolveCart(isNull(), anyString());
            verify(dao).addItem(200, 8, 1);
            verify(session).setAttribute("cartCount", 1);
        }
    }

    /**
     * Должен вернуть список позиций корзины для авторизованного пользователя
     */
    @Test
    void shouldReturnCartItemsWhenUserAuthenticated() throws Exception {
        try (MockedStatic<SecurityContextHolder> sc = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            CustomUserDetails cud = mock(CustomUserDetails.class);
            when(cud.id()).thenReturn(9);
            when(auth.getPrincipal()).thenReturn(cud);
            when(auth.isAuthenticated()).thenReturn(true);
            SecurityContext ctx = mock(SecurityContext.class);
            when(ctx.getAuthentication()).thenReturn(auth);
            sc.when(SecurityContextHolder::getContext).thenReturn(ctx);

            when(session.getAttribute("GUEST_TOKEN")).thenReturn("tk");
            when(dao.resolveCart(9, "tk")).thenReturn(30);

            CartItem entity = new CartItem();
            entity.setCartId(30);
            entity.setProductId(100);
            entity.setName("Продукт");
            entity.setPrice(new BigDecimal("42.50"));
            entity.setQty(3);
            List<CartItem> entities = List.of(entity);
            when(dao.items(30)).thenReturn(entities);

            CartItemDto expectedDto = CartItemMapper.toDto(entity);
            List<CartItemDto> expected = List.of(expectedDto);

            List<CartItemDto> result = service.viewCart();
            assertEquals(expected, result);
        }
    }

    /**
     * Должен объединить корзину после входа и обновить счётчик
     */
    @Test
    void shouldMergeAfterLoginWhenUserAuthenticated() throws Exception {
        try (MockedStatic<SecurityContextHolder> sc = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            CustomUserDetails cud = mock(CustomUserDetails.class);
            when(cud.id()).thenReturn(4);
            when(auth.getPrincipal()).thenReturn(cud);
            when(auth.isAuthenticated()).thenReturn(true);
            SecurityContext ctx = mock(SecurityContext.class);
            when(ctx.getAuthentication()).thenReturn(auth);
            sc.when(SecurityContextHolder::getContext).thenReturn(ctx);

            when(session.getAttribute("GUEST_TOKEN")).thenReturn("gt");
            doNothing().when(dao).mergeCarts("gt", 4);
            when(dao.resolveCart(4, null)).thenReturn(40);

            CartItem e1 = new CartItem();
            e1.setCartId(40);
            e1.setProductId(101);
            e1.setName("Product A");
            e1.setPrice(new BigDecimal("10.00"));
            e1.setQty(1);

            CartItem e2 = new CartItem();
            e2.setCartId(40);
            e2.setProductId(102);
            e2.setName("Product B");
            e2.setPrice(new BigDecimal("20.00"));
            e2.setQty(2);

            List<CartItem> entities = List.of(e1, e2);
            when(dao.items(40)).thenReturn(entities);

            service.mergeAfterLogin();

            verify(dao).mergeCarts("gt", 4);
            verify(session).removeAttribute("GUEST_TOKEN");
            verify(session).setAttribute("cartCount", 2);
        }
    }

    /**
     * Должен очистить корзину и сбросить счётчик
     */
    @Test
    void shouldClearCartAndResetCount() throws Exception {
        service.clearCart(6);
        verify(dao).clearCartByUserId(6);
        verify(session).setAttribute("cartCount", 0);
    }

    /**
     * Должен выбросить SQLException и не менять счётчик при ошибке clearCart
     */
    @Test
    void shouldThrowSQLExceptionWhenClearCartFails() throws SQLException, InterruptedException {
        doThrow(new SQLException("fail")).when(dao).clearCartByUserId(3);
        assertThrows(SQLException.class, () -> service.clearCart(3));
    }
}
