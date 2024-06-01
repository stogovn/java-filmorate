package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.DbFilmStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.DbUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class FilmService {
    private FilmStorage dbFilmStorage;
    private UserStorage dbUserStorage;

    public Film create(Film film) {
        return dbFilmStorage.create(film);
    }

    public Film update(Film newFilm) {
        validateId(newFilm.getId());
        return dbFilmStorage.update(newFilm);
    }

    public Collection<Film> findAll() {
        return dbFilmStorage.findAll();
    }

    public void addLike(Long id, Long userId) {
        validateId(id);
        validateUserId(userId);
        final String sqlQuery = """
                INSERT INTO likes (film_id, user_id)
                VALUES(?,?)
                """;
        dbFilmStorage.getJdbcTemplate().update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery);
            stmt.setLong(1, id);
            stmt.setLong(2, userId);
            return stmt;
        });
        log.info("Фильму с id = {} поставил лайк пользователь с id = {}", id, userId);
    }

    public void deleteLike(Long id, Long userId) {
        validateId(id);
        validateUserId(userId);
        final String sqlQuery = """
                DELETE FROM likes
                WHERE film_id = ? AND user_id = ?
                """;
        dbFilmStorage.getJdbcTemplate().update(sqlQuery, id, userId);
        log.info("У фильма с id = {} пользователь с id = {} удалил лайк", id, userId);
    }

    public List<Film> getPopularFilms(Long count) {
        final String sqlQuery = """
                SELECT f.*, m.mpa_id, m.mpa_name
                FROM films f
                LEFT JOIN likes l ON f.film_id = l.film_id
                JOIN mpa m ON f.mpa_id = m.mpa_id
                GROUP BY f.film_id
                ORDER BY COUNT(l.user_id) DESC
                LIMIT ?
                """;
        return dbFilmStorage.getJdbcTemplate().query(sqlQuery, DbFilmStorage::makeFilm, count);
    }

    public Film findFilmById(long id) {
        validateId(id);
        return dbFilmStorage.findFilmById(id);
    }

    public Genre getGenreById(long id) {
        final String sqlQuery = "SELECT * FROM genres WHERE genre_id = ?";

        final List<Genre> genres = dbFilmStorage.getJdbcTemplate().query(sqlQuery, DbFilmStorage::makeGenre, id);
        if (genres.size() != 1) {
            throw new NotFoundException("genre id = " + id);
        }
        return genres.getFirst();
    }

    public List<Genre> getGenres() {
        final String sqlQuery = "SELECT * FROM genres";
        return dbFilmStorage.getJdbcTemplate().query(sqlQuery, DbFilmStorage::makeGenre);
    }

    public Mpa getMpaById(long id) {
        final String sqlQuery = "SELECT * FROM mpa WHERE mpa_id = ?";
        final List<Mpa> mpa = dbFilmStorage.getJdbcTemplate().query(sqlQuery, FilmService::makeMpa, id);
        if (mpa.size() != 1) {
            throw new NotFoundException("genre id = " + id);
        }
        return mpa.getFirst();
    }

    public List<Mpa> getMpa() {
        final String sqlQuery = "SELECT * FROM mpa";
        return dbFilmStorage.getJdbcTemplate().query(sqlQuery, FilmService::makeMpa);
    }

    private void validateId(Long id) {
        final String sqlQuery = """
                SELECT f.*, m.mpa_id, m.mpa_name
                FROM films f
                JOIN mpa m ON f.mpa_id = m.mpa_id
                WHERE f.film_id = ?
                """;
        final List<Film> films = dbFilmStorage.getJdbcTemplate().query(sqlQuery, DbFilmStorage::makeFilm, id);
        if (films.size() != 1) {
            throw new NotFoundException("Film id = " + id + " not found");
        }
    }

    private void validateUserId(Long id) {
        final String sqlQuery = "SELECT * FROM users WHERE user_id = ?";
        final List<User> users = dbUserStorage.getJdbcTemplate().query(sqlQuery, DbUserStorage::makeUser, id);
        if (users.size() != 1) {
            throw new NotFoundException("user id = " + id);
        }
    }


    private static Mpa makeMpa(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name"));
    }
}
