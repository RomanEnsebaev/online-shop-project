package org.onlineshop.controllers;

import org.onlineshop.dto.CartItemDto;
import org.onlineshop.services.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cart;

    public CartController(CartService cart) { this.cart = cart; }

    @GetMapping("/cart")
    public String view(Model m) throws SQLException, InterruptedException {

        List<CartItemDto> items = cart.viewCart();
        BigDecimal total = items.stream()
                .map(CartItemDto::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        m.addAttribute("items", items);
        m.addAttribute("total", total);
        m.addAttribute("empty", items.isEmpty());
        return "cart";
    }

    @PostMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> add(@PathVariable int id) throws SQLException, InterruptedException {
        cart.addToCart(id);
        return ResponseEntity.ok().build();
    }
}
