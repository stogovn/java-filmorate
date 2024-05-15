package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film data);

    Film update(Film data);

    Collection<Film> findAll();

    Optional<Film> findFilmById(long id);
}
