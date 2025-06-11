package org.onlineshop.dto;

import jakarta.validation.constraints.*;

public record RegistrationDto(
        @NotBlank
        @Size(min = 3, max = 50)
        String username,

        @NotBlank
        @Size(min = 3, max = 100)
        String password,

        @NotBlank
        @Size(min = 3, max = 100)
        String confirmPassword
) {}
