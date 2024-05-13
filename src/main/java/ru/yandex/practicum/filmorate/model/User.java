package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class User {
    private Long id;
    @Email(message = "Неправильно введён email")
    @NotBlank
    private String email;
    @NotBlank
    private String login;
    private String name;
    @Past(message = "Дата рождения не может быть в будущем")
    @NotNull
    private LocalDate birthday;
}
