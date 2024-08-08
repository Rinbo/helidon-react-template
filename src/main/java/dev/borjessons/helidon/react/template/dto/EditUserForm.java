package dev.borjessons.helidon.react.template.dto;

import java.util.List;

import dev.borjessons.helidon.react.template.data.model.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EditUserForm(@Size(min = 2, max = 64) String name, @NotNull List<Role> roles) {
}
