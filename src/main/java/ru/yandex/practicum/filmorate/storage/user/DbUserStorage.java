package ru.yandex.practicum.filmorate.storage.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mapper.Mapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;


@Slf4j
@RequiredArgsConstructor
@Component
@Getter
public class DbUserStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        if (user.getLogin().contains(" ")) {
            log.error("При попытке создания пользователя неверно указан login");
            throw new ValidationException("Логин не должен быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
            log.debug("Новому пользователю присвоилось имя = {}", user.getLogin());
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        final String sqlQuery = """
                INSERT INTO users (user_name, user_login, user_email, user_birthday)
                VALUES (?,?,?,?)
                """;
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getEmail());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            log.error("При попытке обновления пользователя не был указан id");
            throw new ValidationException("Id должен быть указан");
        }
        String sqlQuery =
                "UPDATE users SET user_name = ?, user_login = ?, user_email = ?, user_birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(
                sqlQuery,
                newUser.getName(),
                newUser.getLogin(),
                newUser.getEmail(),
                newUser.getBirthday(),
                newUser.getId()
        );
        return findUserById(newUser.getId());
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users", Mapper::makeUser);
    }

    @Override
    public User findUserById(long id) {
        final String sqlQuery = "SELECT * FROM users WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sqlQuery, Mapper::makeUser, id);
        if (users.isEmpty()) {
            throw new NotFoundException("user id = " + id);
        }
        return users.getFirst();
    }

    @Override
    public void addFriend(long id, long friendId) {
        findUserById(id);
        findUserById(friendId);
        final String querySelect = """
                SELECT status FROM friendship
                WHERE (accepting_user_id = ? AND requesting_user_id = ?)
                """;

        final String queryInsert = """
                INSERT INTO friendship (accepting_user_id, requesting_user_id, status)
                VALUES (?,?,?)
                """;

        final String queryUpdate = """
                UPDATE friendship
                SET status = 'confirmed'
                WHERE accepting_user_id = ? AND requesting_user_id = ?
                """;

        // Проверка существования записи и её статуса
        List<String> existingStatus = jdbcTemplate
                .queryForList(querySelect, String.class, id, friendId);

        if (existingStatus.isEmpty()) {
            // Если записи нет, вставляем новую с состоянием unconfirmed
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(queryInsert);
                stmt.setLong(1, id);
                stmt.setLong(2, friendId);
                stmt.setString(3, "unconfirmed");
                return stmt;
            });
        } else if (existingStatus.getFirst().equals("unconfirmed")) {
            // Если запись существует с состоянием unconfirmed, обновляем на confirmed
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(queryUpdate);
                stmt.setLong(1, id);
                stmt.setLong(2, friendId);
                return stmt;
            });
        }
    }

    @Override
    public void deleteFriend(long id, long friendId) {
        findUserById(id);
        findUserById(friendId);
        final String sqlQuery = """
                DELETE FROM friendship AS u
                WHERE accepting_user_id = ? AND requesting_user_id = ?
                """;
        jdbcTemplate.update(sqlQuery, id, friendId);
    }

    @Override
    public List<User> getFriends(Long id) {
        findUserById(id);
        final String sqlQuery = """
                SELECT * FROM users AS u
                JOIN friendship AS f ON u.user_id = f.requesting_user_id
                WHERE f.accepting_user_id = ?
                """;
        return jdbcTemplate.query(sqlQuery, Mapper::makeUser, id);
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        findUserById(id);
        findUserById(otherId);
        final String query = """
                SELECT u.* FROM users AS u
                JOIN friendship f1 ON u.user_id = f1.requesting_user_id AND f1.accepting_user_id = ?
                JOIN friendship f2 ON u.user_id = f2.requesting_user_id AND f2.accepting_user_id = ?
                """;
        return jdbcTemplate.query(query, Mapper::makeUser, id, otherId);
    }
}
