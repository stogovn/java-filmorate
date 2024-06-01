package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;


public interface FilmStorage {
    Film create(Film data);

    Film update(Film data);

    Collection<Film> findAll();

    Film findFilmById(long id);

    void validate(Film film);

    JdbcTemplate getJdbcTemplate();
}
