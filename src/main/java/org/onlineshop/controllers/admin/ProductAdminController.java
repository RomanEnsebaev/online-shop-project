package org.onlineshop.controllers.admin;

import jakarta.validation.Valid;
import org.onlineshop.dto.ProductDto;
import org.onlineshop.services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;


@Controller
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/admin/products")
public class ProductAdminController {

    private final ProductService products;
    private static final Logger log = LoggerFactory.getLogger(ProductAdminController.class);

    public ProductAdminController(ProductService products) {
        this.products = products;
    }

    @GetMapping("/new")
    public String form(Model m) {
        log.info("ProductAdminController.list");
        m.addAttribute("productDto", new ProductDto());
        m.addAttribute("formAction", "/admin/products/save");
        return "product_form";
    }

    @PostMapping("/save")
    public RedirectView save(@ModelAttribute("productDto") ProductDto dto,
                             BindingResult br,
                             RedirectAttributes ra) {
        if (br.hasErrors()) new RedirectView("/product_form");
        products.add(dto);
        ra.addFlashAttribute("msg", "Товар сохранён");
        RedirectView rv = new RedirectView("/", true);
        rv.setExposeModelAttributes(false);
        return rv;
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        ProductDto dto = products.find(id);
        model.addAttribute("productDto", dto);
        model.addAttribute("formAction", "/admin/products/" + id + "/edit");
        return "product_form";
    }

    @PostMapping("/{id}/edit")
    public RedirectView update(@PathVariable("id") int id,
                               @ModelAttribute("productDto") @Valid ProductDto dto,
                               BindingResult br,
                               RedirectAttributes ra) {
        if (br.hasErrors()) return new RedirectView("/admin/products/" + id + "/edit");
        products.update(dto);
        ra.addFlashAttribute("msg", "Product updated");
        RedirectView rv = new RedirectView("/", true);
        rv.setExposeModelAttributes(false);
        return rv;
    }

    @DeleteMapping("{id}")
    @ResponseBody
    public void delete(@PathVariable("id") int id) {
        products.delete(id);
    }
}
