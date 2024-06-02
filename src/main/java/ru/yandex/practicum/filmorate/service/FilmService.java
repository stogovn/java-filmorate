package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class FilmService {
    private FilmStorage dbFilmStorage;

    public Film create(Film film) {
        return dbFilmStorage.create(film);
    }

    public Film update(Film newFilm) {
        return dbFilmStorage.update(newFilm);
    }

    public Collection<Film> findAll() {
        return dbFilmStorage.findAll();
    }

    public void addLike(Long id, Long userId) {
       dbFilmStorage.addLike(id,userId);
    }

    public void deleteLike(Long id, Long userId) {
        dbFilmStorage.deleteLike(id,userId);
    }

    public List<Film> getPopularFilms(Long count) {
        return dbFilmStorage.getPopularFilms(count);
    }

    public Film findFilmById(long id) {
        return dbFilmStorage.findFilmById(id);
    }
}
