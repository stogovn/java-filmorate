package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static utils.DataUtils.toLocalDate;

@Slf4j
@RequiredArgsConstructor
@Component
@Primary
@Getter
public class DbFilmStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

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
    public Collection<Film> findAll() {
        final String sqlQuery = """
                SELECT f.*, m.mpa_id, m.mpa_name
                FROM films f
                JOIN mpa m ON f.mpa_id = m.mpa_id
                """;
        return jdbcTemplate.query(sqlQuery, DbFilmStorage::makeFilm);
    }

    @Override
    public Film findFilmById(long id) {
        final String sqlQuery = """
                SELECT f.*, m.mpa_id, m.mpa_name
                FROM films f
                JOIN mpa m ON f.mpa_id = m.mpa_id
                WHERE f.film_id = ?
                """;
        final List<Film> films = jdbcTemplate.query(sqlQuery, DbFilmStorage::makeFilm, id);
        if (films.size() != 1) {
            throw new NotFoundException("Film id = " + id + " not found");
        }

        Film film = films.getFirst();

        final String genreQuery = """
                SELECT g.genre_id, g.genre_name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.genre_id
                WHERE fg.film_id = ?
                """;
        Set<Genre> genres = new HashSet<>(jdbcTemplate.query(genreQuery, DbFilmStorage::makeGenre, id));
        film.setGenres(genres);
        return film;
    }

    @Override
    public void validate(Film film) {
        LocalDate minDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(minDate)) {
            log.error("При попытке создания фильма указана дата раньше {}", minDate);
            throw new ValidationException("Дата должна быть не раньше 28 декабря 1895 года");
        }
    }

    public static Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa;
        int mpaId = rs.getInt("mpa_id");
        String mpaName = rs.getString("mpa_name");
        if (mpaName != null) {
            mpa = new Mpa(mpaId, mpaName);
        } else {
            mpa = new Mpa(mpaId);
        }
        return Film.builder()
                .id(rs.getLong("film_id"))
                .name(rs.getString("film_name"))
                .description(rs.getString("film_description"))
                .duration(rs.getLong("film_duration"))
                .releaseDate(toLocalDate(rs.getDate("film_releaseDate")))
                .mpa(mpa)
                .build();
    }

    public static Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("genre_id"), rs.getString("genre_name"));
    }
}
