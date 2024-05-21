package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class FilmService {
    private FilmStorage filmStorage;
    private UserStorage userStorage;

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        validateId(newFilm.getId());
        return filmStorage.update(newFilm);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public void addLike(Long id, Long userId) {
        validateId(id);
        validateUserId(userId);
        filmStorage.getFilms().get(id).getLikes().add(userId);
        log.info("Фильму с id = {} поставил лайк пользователь с id = {}", id, userId);
    }

    public void deleteLike(Long id, Long userId) {
        validateId(id);
        validateUserId(userId);
        filmStorage.getFilms().get(id).getLikes().removeIf(x -> x.equals(userId));
        log.info("У фильма с id = {} пользователь с id = {} удалил лайк", id, userId);
    }

    public List<Film> getPopularFilms(Long count) {
        List<Film> sortedFilms = filmStorage.getFilms().values().stream()
                .filter(o -> o.getLikes() != null)
                .sorted(Comparator.comparingInt(o -> o.getLikes().size())).toList().reversed();

        return sortedFilms.stream().limit(count).toList();
    }

    public Film findFilmById(long id) {
        validateId(id);
        return filmStorage.findFilmById(id);
    }

    private void validateId(Long id) {
        if (!filmStorage.getFilms().containsKey(id)) {
            log.error("Указан несуществующий фильм с id: {}", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
    }

    private void validateUserId(Long userId) {
        if (!userStorage.getUsers().containsKey(userId)) {
            log.error("Указан несуществующий пользователь с id: {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }
}
