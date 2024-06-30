package io.helidon.examples.quickstart.se.dto;

import jakarta.validation.constraints.Email;

public record LoginForm(@Email String email) {
}
