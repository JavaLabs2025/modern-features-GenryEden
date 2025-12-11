package org.lab.user;

public record User(
        Long id,
        String name,
        String email
) {
    public User {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Некорректный email");
        }
    }
}