package dev.borjessons.helidon.react.template.data.model;

import java.time.LocalDateTime;
import java.util.List;

public record User(int id, String email, String name, LocalDateTime createdAt, LocalDateTime updatedAt, List<Role> roles) {
}
