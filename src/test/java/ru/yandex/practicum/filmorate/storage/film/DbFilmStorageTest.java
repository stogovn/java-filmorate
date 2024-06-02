package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.user.DbUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ContextConfiguration(classes = {DbFilmStorage.class, DbUserStorage.class})
class DbFilmStorageTest {
    private final DbFilmStorage dbFilmStorage;

    @Test
    void shouldBeCorrectDbFilmStorage() {
        dbFilmStorage.create(Film.builder()
                .name("test")
                .description("testDescription")
                .duration(120L)
                .releaseDate(LocalDate.now())
                .mpa(new Mpa(1, "G"))
                .build()
        );
        List<Film> films = dbFilmStorage.findAll();
        assertEquals(1, films.size());
        assertThat(films.getFirst()).hasFieldOrPropertyWithValue("id", 1L);

        Film film = dbFilmStorage.findFilmById(1L);
        assertThat(film).hasFieldOrPropertyWithValue("id", 1L);

        dbFilmStorage.create(Film.builder()
                .name("test2")
                .description("testDescription2")
                .duration(121L)
                .releaseDate(LocalDate.now())
                .mpa(new Mpa(1, "G"))
                .build()
        );
        List<Film> films1 = dbFilmStorage.findAll();
        assertEquals(2, films1.size());
        assertThat(films1.get(0)).hasFieldOrPropertyWithValue("id", 1L);
        assertThat(films1.get(1)).hasFieldOrPropertyWithValue("id", 2L);

        dbFilmStorage.update(Film.builder()
                .id(1L)
                .name("testUpdate")
                .description("testUpdate")
                .duration(80L)
                .releaseDate(LocalDate.now())
                .mpa(new Mpa(1, "G"))
                .build()
        );
        Film film1 = dbFilmStorage.findFilmById(1L);
        assertThat(film1).hasFieldOrPropertyWithValue("id", 1L);
        assertThat(film1).hasFieldOrPropertyWithValue("name", "testUpdate");
    }
}