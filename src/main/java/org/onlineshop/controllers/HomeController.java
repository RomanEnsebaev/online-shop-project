package org.onlineshop.controllers;

import org.onlineshop.dto.ProductPageDto;
import org.onlineshop.services.ProductService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.SQLException;

@Controller
public class HomeController {

    private static final Logger log = LogManager.getLogger(HomeController.class);
    private final ProductService products;

    @Value("${productPage.size}")
    private int pageSize;

    public HomeController(ProductService products) {
        this.products = products;
    }

    @GetMapping("/")
    public String home(Model model, @RequestParam(name="page", defaultValue="1") int page) throws SQLException, InterruptedException {
        ProductPageDto pageDto = products.getProductsPage(page, pageSize);
        log.debug("Loaded {} products, currentPage={}, totalPages={}",
                pageDto.getProducts().size(),
                pageDto.getCurrentPage(),
                pageDto.getTotalPages());
        model.addAttribute("pageDto", pageDto);
        return "home";
    }
}
