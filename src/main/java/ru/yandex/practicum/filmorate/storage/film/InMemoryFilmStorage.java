package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Slf4j
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
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            // если фильм найден и все условия соблюдены, обновляем его содержимое
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setDuration(newFilm.getDuration());
            oldFilm.setName(newFilm.getName());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            log.info("Обновили фильм с id = {}", newFilm.getId());
            return oldFilm;
        }
        log.error("При попытке обновления фильма указан не существующий id: {}", newFilm.getId());
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public void addLike(Long id, Long userId) {
        validateId(id);
        films.get(id).getLikes().add(userId);
        log.info("Фильму с id = {} поставил лайк пользователь с id = {}", id, userId);
    }

    @Override
    public void deleteLike(Long id, Long userId) {
        validateId(id);
        films.get(id).getLikes().removeIf(x -> x.equals(userId));
        log.info("У фильма с id = {} пользователь с id = {} удалил лайк", id, userId);
    }

    @Override
    public List<Film> getPopularFilms(Long count) {
        List<Film> sortedFilms = films.values().stream()
                .filter(o -> o.getLikes() != null)
                .sorted(Comparator.comparingInt(o -> o.getLikes().size())).toList().reversed();

        return sortedFilms.stream().limit(count).toList();
    }

    @Override
    protected void validate(Film film) {
        LocalDate minDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(minDate)) {
            log.error("При попытке создания фильма указана дата раньше {}", minDate);
            throw new ValidationException("Дата должна быть не раньше 28 декабря 1895 года");
        }
    }

    private void validateId(Long id) {
        if (!films.containsKey(id)) {
            log.error("Указан не существующий фильм с id: {}", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
    }
}
