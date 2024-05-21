package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class FilmorateApplicationTests {
    static InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
    static InMemoryUserStorage userStorage = new InMemoryUserStorage();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @AllArgsConstructor
    @Builder
    static class ExpectedViolation {
        String propertyPath;
        String message;
    }

    @Test
    void validateFilmControllerFail() {
        LocalDate release = LocalDate.of(1900, 3, 25);
        Film filmFail = Film.builder()
                .name("Film name")
                .description("Пятеро друзей ( комик-группа «Шарло»), " +
                        "приезжают в город Бризуль. Здесь они хотят разыскать господина Огюста Куглова," +
                        " который задолжал им деньги, а именно 20 миллионов. о Куглов, " +
                        "который за время «своего отсутствия», стал кандидатом Коломбани.")
                .releaseDate(release)
                .duration(200L)
                .build();
        List<ConstraintViolation<Film>> violationsDescription = new ArrayList<>(validator.validate(filmFail));
        ExpectedViolation expectedDescription = ExpectedViolation.builder()
                .propertyPath("description")
                .message("Максимальная длина описания — 200 символов")
                .build();
        assertEquals(1, violationsDescription.size());
        assertEquals(expectedDescription.propertyPath,
                violationsDescription.getFirst().getPropertyPath().toString());
        assertEquals(expectedDescription.message,
                violationsDescription.getFirst().getMessage());
        LocalDate failRelease = LocalDate.of(1800, 3, 25);
        filmFail.setReleaseDate(failRelease);
        assertThrows(ValidationException.class, () -> filmStorage.create(filmFail));
        filmFail.setName("");
        filmFail.setDescription("description");
        List<ConstraintViolation<Film>> violationsName = new ArrayList<>(validator.validate(filmFail));
        ExpectedViolation expectedName = ExpectedViolation.builder()
                .propertyPath("name")
                .message("Название не должно быть пустым")
                .build();
        assertEquals(expectedName.propertyPath,
                violationsName.getFirst().getPropertyPath().toString());
        assertEquals(expectedName.message,
                violationsName.getFirst().getMessage());
        filmFail.setName("Film name");
        filmFail.setDuration(-100L);
        List<ConstraintViolation<Film>> violationsDuration = new ArrayList<>(validator.validate(filmFail));
        ExpectedViolation expectedDuration = ExpectedViolation.builder()
                .propertyPath("duration")
                .message("Продолжительность фильма не может быть отрицательным числом")
                .build();
        assertEquals(expectedDuration.propertyPath,
                violationsDuration.getFirst().getPropertyPath().toString());
        assertEquals(expectedDuration.message,
                violationsDuration.getFirst().getMessage());

    }

    @Test
    void validateUserControllerFail() {
        LocalDate birthday = LocalDate.of(1992, 4, 17);
        LocalDate birthdayFail = LocalDate.of(2222, 3, 25);
        String emailFail = "mail.mail.ru";
        String loginFail = "lo gin";
        String nameFail = "";
        User userFail = User.builder()
                .name(nameFail)
                .login("login")
                .email(emailFail)
                .birthday(birthday)
                .build();
        List<ConstraintViolation<User>> violationsEmail = new ArrayList<>(validator.validate(userFail));
        ExpectedViolation expectedEmail = ExpectedViolation.builder()
                .propertyPath("email")
                .message("Неправильно введён email")
                .build();
        assertEquals(expectedEmail.propertyPath,
                violationsEmail.getFirst().getPropertyPath().toString());
        assertEquals(expectedEmail.message,
                violationsEmail.getFirst().getMessage());
        userStorage.create(userFail);
        assertEquals("login", userFail.getName(), "Имя не стало логином");
        userFail.setName("name");
        userFail.setLogin(loginFail);
        assertThrows(ValidationException.class, () -> userStorage.create(userFail));
        userFail.setLogin("login");
        userFail.setEmail("mail@mail.ru");
        userFail.setBirthday(birthdayFail);
        List<ConstraintViolation<User>> violationsBirthday = new ArrayList<>(validator.validate(userFail));
        ExpectedViolation expectedBirthday = ExpectedViolation.builder()
                .propertyPath("birthday")
                .message("Дата рождения не может быть в будущем")
                .build();
        assertEquals(expectedBirthday.propertyPath,
                violationsBirthday.getFirst().getPropertyPath().toString());
        assertEquals(expectedBirthday.message,
                violationsBirthday.getFirst().getMessage());

    }
}
