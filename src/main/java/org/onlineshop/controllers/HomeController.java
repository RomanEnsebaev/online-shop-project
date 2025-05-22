package org.onlineshop.controllers;

import org.onlineshop.dao.ProductDao;
import org.onlineshop.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProductService products;

    public HomeController(ProductService products) {
        this.products = products;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", products.catalog());
        return "home";
    }
}
