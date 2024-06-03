package mapper;

import org.springframework.jdbc.core.ResultSetExtractor;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static utils.DataUtils.toLocalDate;

public class FilmExtractor implements ResultSetExtractor<List<Film>> {
    @Override
    public List<Film> extractData(ResultSet rs) throws SQLException {
        Map<Long, Film> filmMap = new LinkedHashMap<>();

        while (rs.next()) {
            long filmId = rs.getLong("film_id");
            Film film = filmMap.get(filmId);

            if (film == null) {
                Mpa mpa = new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name"));
                film = Film.builder()
                        .id(filmId)
                        .name(rs.getString("film_name"))
                        .description(rs.getString("film_description"))
                        .duration(rs.getLong("film_duration"))
                        .releaseDate(toLocalDate(rs.getDate("film_releaseDate")))
                        .mpa(mpa)
                        .genres(new LinkedHashSet<>())
                        .likes(new LinkedHashSet<>())
                        .build();
                filmMap.put(filmId, film);
            }
            int genreId = rs.getInt("genre_id");
            if (!rs.wasNull()) {
                String genreName = rs.getString("genre_name");
                Genre genre = new Genre(genreId, genreName);
                film.getGenres().add(genre);
            }
            long likeUserId = rs.getLong("like_user_id");
            if (!rs.wasNull()) {
                film.getLikes().add(likeUserId);
            }
        }
        return new ArrayList<>(filmMap.values());
    }
}
