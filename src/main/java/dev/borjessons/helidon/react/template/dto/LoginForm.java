package dev.borjessons.helidon.react.template.dto;

import jakarta.validation.constraints.Email;

public record LoginForm(@Email String email) {
}
