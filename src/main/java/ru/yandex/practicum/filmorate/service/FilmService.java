package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class FilmService {
    private FilmStorage dbFilmStorage;
    private UserStorage dbUserStorage;

    public Film create(Film film) {
        validateReleaseDate(film);
        return dbFilmStorage.create(film);
    }

    public Film update(Film newFilm) {
        validateReleaseDate(newFilm);
        return dbFilmStorage.update(newFilm);
    }

    public Collection<Film> findAll() {
        return dbFilmStorage.findAll();
    }

    public void addLike(Long id, Long userId) {
        dbUserStorage.findUserById(userId);
        dbFilmStorage.addLike(id, userId);
    }

    public void deleteLike(Long id, Long userId) {
        dbUserStorage.findUserById(userId);
        dbFilmStorage.deleteLike(id, userId);
    }

    public List<Film> getPopularFilms(Long count) {
        return dbFilmStorage.getPopularFilms(count);
    }

    public Film findFilmById(long id) {
        return dbFilmStorage.findFilmById(id);
    }

    private void validateReleaseDate(Film film) {
        LocalDate minDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(minDate)) {
            log.error("При попытке создания фильма указана дата раньше {}", minDate);
            throw new ValidationException("Дата должна быть не раньше 28 декабря 1895 года");
        }
    }
}
