package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Slf4j
@Getter
@Component
public class InMemoryFilmStorage extends Storage<Film> implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        // проверяем выполнение необходимых условий
        validate(film);
        // формируем дополнительные данные
        film.setId(getNextId(films));
        film.setLikes(new HashSet<>());
        // сохраняем новый фильм в памяти приложения
        films.put(film.getId(), film);
        log.info("Создался новый фильм с id = {}", film.getId());
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        // проверяем необходимые условия
        if (newFilm.getId() == null) {
            log.error("При попытке обновления фильма не был указан id");
            throw new ValidationException("Id должен быть указан");
        }
        validate(newFilm);
        Film oldFilm = films.get(newFilm.getId());
        // если фильм найден и все условия соблюдены, обновляем его содержимое
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setDuration(newFilm.getDuration());
        oldFilm.setName(newFilm.getName());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        log.info("Обновили фильм с id = {}", newFilm.getId());
        return oldFilm;
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film findFilmById(long id) {
        return films.get(id);
    }

    @Override
    protected void validate(Film film) {
        LocalDate minDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(minDate)) {
            log.error("При попытке создания фильма указана дата раньше {}", minDate);
            throw new ValidationException("Дата должна быть не раньше 28 декабря 1895 года");
        }
    }
}
