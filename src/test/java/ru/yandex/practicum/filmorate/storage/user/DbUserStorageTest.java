package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ContextConfiguration(classes = {DbUserStorage.class})
class DbUserStorageTest {
    private final DbUserStorage userStorage;


    @Test
    void create() {
        userStorage.create(User.builder()
                .email("email@email.ru")
                .login("test")
                .name("Nik")
                .birthday(LocalDate.now())
                .build());
        List<User> users = userStorage.findAll();
        assertEquals(1, users.size());
        assertThat(users.getFirst()).hasFieldOrPropertyWithValue("id", 1L);
    }

    @Test
    @Sql(scripts = {"/test-user.sql"})
    void get() {
        User user = userStorage.findUserById(1L);
        assertThat(user).hasFieldOrPropertyWithValue("id", 1L);
    }

    @Test
    @Sql(scripts = {"/test-user.sql"})
    void getAll() {
        List<User> users = userStorage.findAll();
        assertEquals(2, users.size());
        assertThat(users.get(0)).hasFieldOrPropertyWithValue("id", 1L);
        assertThat(users.get(1)).hasFieldOrPropertyWithValue("id", 2L);
    }

    @Test
    @Sql(scripts = {"/test-user.sql"})
    void update() {
        userStorage.update(User.builder()
                .id(1L)
                .email("email@email.ru")
                .login("test")
                .name("Nik")
                .birthday(LocalDate.now())
                .build());
        User user = userStorage.findUserById(1L);

        assertThat(user).hasFieldOrPropertyWithValue("id", 1L);
        assertThat(user).hasFieldOrPropertyWithValue("name", "Nik");
    }
}