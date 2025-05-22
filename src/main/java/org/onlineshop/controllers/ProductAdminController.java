package org.onlineshop.controllers;

import org.onlineshop.dto.ProductDto;
import org.onlineshop.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;


@Controller
@RequestMapping("/admin/products")
public class ProductAdminController {

    private final ProductService products;

    public ProductAdminController(ProductService products) { this.products = products; }

    @GetMapping("/new")
    public String form(Model m) {
        m.addAttribute("productDto", new ProductDto());
        return "product_form";
    }

    @PostMapping("/save")
    public RedirectView save(@ModelAttribute("productDto") ProductDto dto,
                       RedirectAttributes ra) {
        products.add(dto);
        ra.addFlashAttribute("msg","Товар сохранён");
        RedirectView rv = new RedirectView("/", true);
        rv.setExposeModelAttributes(false);
        return rv;
    }
}
