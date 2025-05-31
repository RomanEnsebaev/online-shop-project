package org.onlineshop.controllers;

import org.onlineshop.dao.UserDao;
import org.onlineshop.dto.RegistrationDto;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class AuthController {
    private final PasswordEncoder encoder;
    private final UserDao dao;
    private final UserDetailsService   uds;

    public AuthController(PasswordEncoder encoder,
                          UserDao dao,
                          UserDetailsService uds) {
        this.encoder = encoder;
        this.dao = dao;
        this.uds = uds;
    }

    @GetMapping("/login")
    public String login() {
        return "login"; }

    @GetMapping("/register")
    public String form(Model m) {
        m.addAttribute("dto", new RegistrationDto("", "", ""));
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("dto") RegistrationDto dto,
                           RedirectAttributes ra) {

        if (!dto.password().equals(dto.confirmPassword())) {
            ra.addFlashAttribute("error","Password mismatch");
            return "redirect:/register";
        }
        if (dao.exists(dto.username())) {
            ra.addFlashAttribute("error","User already exists");
            return "redirect:/register";
        }

        dao.saveUser(dto.username(), dto.password());
        ra.addFlashAttribute("msg","Registration successful. Please log in.");
        return "redirect:/login";
    }
}
