package io.helidon.examples.quickstart.se.dto;

import java.util.List;

import io.helidon.examples.quickstart.se.data.model.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EditUserForm(@Size(min = 2, max = 64) String name, @NotNull List<Role> roles) {
}
