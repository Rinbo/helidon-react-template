package io.helidon.examples.quickstart.se.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserForm(@Email String email, @Size(min = 2, max = 64) String name) {
}
