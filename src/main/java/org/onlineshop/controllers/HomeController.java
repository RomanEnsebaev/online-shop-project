package org.onlineshop.controllers;

import org.onlineshop.dao.ProductDao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProductDao dao;

    public HomeController(ProductDao dao) {
        this.dao = dao;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", dao.findAll());
        return "home";
    }
}
