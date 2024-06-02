package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
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
    void shouldBeCorrectDbUserStorage() {
        userStorage.create(User.builder()
                .email("email@email.ru")
                .login("test")
                .name("Nik")
                .birthday(LocalDate.now())
                .build());
        List<User> users = userStorage.findAll();
        assertEquals(1, users.size());
        assertThat(users.getFirst()).hasFieldOrPropertyWithValue("id", 1L);

        User user = userStorage.findUserById(1L);
        assertThat(user).hasFieldOrPropertyWithValue("id", 1L);

        userStorage.create(User.builder()
                .email("emai2l@email.ru")
                .login("test2")
                .name("Nik2")
                .birthday(LocalDate.now())
                .build());
        List<User> users1 = userStorage.findAll();
        assertEquals(2, users1.size());
        assertThat(users1.get(0)).hasFieldOrPropertyWithValue("id", 1L);
        assertThat(users1.get(1)).hasFieldOrPropertyWithValue("id", 2L);

        userStorage.update(User.builder()
                .id(1L)
                .email("email@email.ru")
                .login("test")
                .name("Nikita")
                .birthday(LocalDate.now())
                .build());
        User user1 = userStorage.findUserById(1L);
        assertThat(user1).hasFieldOrPropertyWithValue("id", 1L);
        assertThat(user1).hasFieldOrPropertyWithValue("name", "Nikita");
    }
}