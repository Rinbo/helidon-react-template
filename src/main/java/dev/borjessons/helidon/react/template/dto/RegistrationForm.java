package dev.borjessons.helidon.react.template.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record RegistrationForm(@Email String email, @Size(min = 2, max = 64) String name) {
}
