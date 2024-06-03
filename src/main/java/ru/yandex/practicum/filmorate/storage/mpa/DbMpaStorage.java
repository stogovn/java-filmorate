package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mapper.Mapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class DbMpaStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public Mpa getMpaById(long id) {
        final String sqlQuery = "SELECT * FROM mpa WHERE mpa_id = ?";
        final List<Mpa> mpa = jdbcTemplate.query(sqlQuery, Mapper::makeMpa, id);
        if (mpa.size() != 1) {
            throw new NotFoundException("genre id = " + id);
        }
        return mpa.getFirst();
    }

    public List<Mpa> getMpa() {
        final String sqlQuery = "SELECT * FROM mpa";
        return jdbcTemplate.query(sqlQuery, Mapper::makeMpa);
    }


}
