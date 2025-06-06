package org.onlineshop.controllers;

import org.onlineshop.db.CustomUserDetails;
import org.onlineshop.dto.OrderDto;
import org.onlineshop.services.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.sql.SQLException;
import java.util.List;

@Controller
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public String listOrders(Model model, Authentication authentication) throws SQLException, InterruptedException {
        List<OrderDto> orders = orderService.getOrdersForCurrentUser();
        model.addAttribute("orders", orders);
        return "orders";
    }

    @PostMapping("/orders/create")
    public String createOrder(Authentication authentication) throws SQLException, InterruptedException {
        orderService.placeOrder();
        return "redirect:/orders";
    }

}
