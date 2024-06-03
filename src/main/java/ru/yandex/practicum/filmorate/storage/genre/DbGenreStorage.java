package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mapper.Mapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class DbGenreStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public Genre getGenreById(long id) {
        final String sqlQuery = "SELECT * FROM genres WHERE genre_id = ?";

        final List<Genre> genres = jdbcTemplate.query(sqlQuery, Mapper::makeGenre, id);
        if (genres.size() != 1) {
            throw new NotFoundException("genre id = " + id);
        }
        return genres.getFirst();
    }

    public List<Genre> getGenres() {
        final String sqlQuery = "SELECT * FROM genres";
        return jdbcTemplate.query(sqlQuery, Mapper::makeGenre);
    }
}
