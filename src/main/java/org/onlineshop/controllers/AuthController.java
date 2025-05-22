package org.onlineshop.controllers;

import org.onlineshop.dto.RegistrationDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class AuthController {
    private final PasswordEncoder encoder;
    private final JdbcTemplate jdbc;
    private final UserDetailsManager  uds;

    public AuthController(PasswordEncoder encoder,
                          JdbcTemplate jdbc,
                          UserDetailsManager uds) {
        this.encoder = encoder;
        this.jdbc = jdbc;
        this.uds = uds;
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String form(Model m) {
        m.addAttribute("dto", new RegistrationDto("", "", ""));
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("dto") RegistrationDto dto,
                           RedirectAttributes ra) {

        if (!dto.password().equals(dto.confirmPassword())) {
            ra.addFlashAttribute("error", "Password mismatch");
            return "redirect:/register";
        }
        if (uds.userExists(dto.username())) {
            ra.addFlashAttribute("error", "User already exists");
            return "redirect:/register";
        }

        jdbc.update("""
                        INSERT INTO users(username, password, role)
                        VALUES (?,?, 'USER')
                        """,
                dto.username(),
                encoder.encode(dto.password())
        );

        ra.addFlashAttribute("msg",
                "Registration successful. Please log in.");
        return "redirect:/login";
    }
}
