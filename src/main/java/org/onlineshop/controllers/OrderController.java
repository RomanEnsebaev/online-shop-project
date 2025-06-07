package org.onlineshop.controllers;

import org.onlineshop.dto.order.OrderPageDto;
import org.onlineshop.services.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.SQLException;

@Controller
public class OrderController {

    @Value("${orderPage.size}")
    private int pageSize;

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public String listOrders(Model model, Authentication authentication, @RequestParam(name = "page", defaultValue = "1") int page) throws SQLException, InterruptedException {
        OrderPageDto pageDto = orderService.getOrdersForCurrentUser(page, pageSize);
        model.addAttribute("pageDto", pageDto);
        return "orders";
    }

    @PostMapping("/orders/create")
    public String createOrder(Authentication authentication) throws SQLException, InterruptedException {
        orderService.placeOrder();
        return "redirect:/orders";
    }

}
