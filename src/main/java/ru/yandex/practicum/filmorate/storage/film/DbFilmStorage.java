package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.user.DbUserStorage;
import mapper.FilmExtractor;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@Component
@Getter
public class DbFilmStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final DbUserStorage userStorage;

    @Override
    public Film create(Film film) {
        validate(film);
        if (film.getMpa().getId() > 5) {
            throw new ValidationException("Неверно задан MPA");
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        final String sqlQuery = """
                INSERT INTO films (film_name, film_description, film_duration, film_releaseDate, mpa_id)
                VALUES (?,?,?,?,?)
                """;
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setLong(3, film.getDuration());
            stmt.setDate(4, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        long filmId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            final String genreInsertQuery = """
                    INSERT INTO film_genres (film_id, genre_id)
                    VALUES (?, ?)
                    """;
            jdbcTemplate.batchUpdate(genreInsertQuery, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Genre genre = (Genre) film.getGenres().toArray()[i];
                    if (genre.getId() > 6) {
                        throw new ValidationException("Такого жанра с id = " + genre.getId() + " нет");
                    }
                    ps.setLong(1, filmId);
                    ps.setLong(2, genre.getId());
                }

                @Override
                public int getBatchSize() {
                    return film.getGenres().size();
                }
            });
        }

        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            log.error("При попытке обновления фильма не был указан id");
            throw new ValidationException("Id должен быть указан");
        }
        validate(newFilm);
        final String sqlQuery = """
                UPDATE films
                SET film_name = ?, film_description = ?, film_duration = ?, film_releaseDate = ?, mpa_id = ?
                WHERE film_id = ?
                """;
        jdbcTemplate.update(
                sqlQuery,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getDuration(),
                newFilm.getReleaseDate(),
                newFilm.getMpa().getId(),
                newFilm.getId()
        );
        return findFilmById(newFilm.getId());
    }

    @Override
    public List<Film> findAll() {
        final String sqlQuery = """
                    SELECT f.*,
                           m.mpa_id, m.mpa_name,
                           g.genre_id, g.genre_name,
                           l.user_id AS like_user_id
                    FROM films f
                    JOIN mpa m ON f.mpa_id = m.mpa_id
                    LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                    LEFT JOIN genres g ON fg.genre_id = g.genre_id
                    LEFT JOIN likes l ON f.film_id = l.film_id
                """;
        return jdbcTemplate.query(sqlQuery, new FilmExtractor());
    }

    @Override
    public Film findFilmById(long id) {
        List<Film> films = findAll();
        Optional<Film> optionalFilm = films.stream()
                .filter(f -> f.getId() == id)
                .findFirst();

        if (optionalFilm.isPresent()) {
            return optionalFilm.get();
        } else {
            throw new NotFoundException("Film id = " + id + " not found");
        }
    }

    @Override
    public void addLike(Long id, Long userId) {
        findFilmById(id);
        userStorage.findUserById(userId);
        final String sqlQuery = """
                INSERT INTO likes (film_id, user_id)
                VALUES(?,?)
                """;
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery);
            stmt.setLong(1, id);
            stmt.setLong(2, userId);
            return stmt;
        });
        log.info("Фильму с id = {} поставил лайк пользователь с id = {}", id, userId);
    }

    @Override
    public void deleteLike(Long id, Long userId) {
        final String sqlQuery = """
                DELETE FROM likes
                WHERE film_id = ? AND user_id = ?
                """;
        jdbcTemplate.update(sqlQuery, id, userId);
        log.info("У фильма с id = {} пользователь с id = {} удалил лайк", id, userId);
    }

    @Override
    public List<Film> getPopularFilms(Long count) {
        final String sqlQuery = """
                SELECT f.*,
                       m.mpa_id, m.mpa_name,
                       g.genre_id, g.genre_name,
                       l.user_id AS like_user_id
                FROM (
                    SELECT f.film_id, COUNT(l.user_id) as like_count
                    FROM films f
                    LEFT JOIN likes l ON f.film_id = l.film_id
                    GROUP BY f.film_id
                    ORDER BY like_count DESC
                    LIMIT ?
                ) popular
                JOIN films f ON popular.film_id = f.film_id
                JOIN mpa m ON f.mpa_id = m.mpa_id
                LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                LEFT JOIN genres g ON fg.genre_id = g.genre_id
                LEFT JOIN likes l ON f.film_id = l.film_id
                ORDER BY popular.like_count DESC, f.film_id
                """;
        return jdbcTemplate.query(sqlQuery, new FilmExtractor(), count);
    }

    private void validate(Film film) {
        LocalDate minDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(minDate)) {
            log.error("При попытке создания фильма указана дата раньше {}", minDate);
            throw new ValidationException("Дата должна быть не раньше 28 декабря 1895 года");
        }
    }
}
