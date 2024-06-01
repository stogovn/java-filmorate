package ru.yandex.practicum.filmorate.storage.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static utils.DataUtils.toLocalDate;

@Slf4j
@RequiredArgsConstructor
@Component
@Primary
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
        validate(user);
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
        User oldUser = findUserById(newUser.getId());
        if (!oldUser.getEmail().equals(newUser.getEmail())) {
            validate(newUser);
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
        return jdbcTemplate.query("SELECT * FROM users", DbUserStorage::makeUser);
    }

    @Override
    public User findUserById(long id) {
        final String sqlQuery = "SELECT * FROM users WHERE user_id = ?";
        final List<User> users = jdbcTemplate.query(sqlQuery, DbUserStorage::makeUser, id);
        if (users.size() != 1) {
            throw new NotFoundException("user id = " + id);
        }
        return users.getFirst();
    }

    @Override
    public void validate(User user) {
        final String sqlQuery = "SELECT user_email FROM users";
        final List<String> emails = jdbcTemplate.queryForList(sqlQuery, String.class);
        if (emails.contains(user.getEmail())) {
            log.error("При попытке обновления пользователя указан существующий email: {}", user.getEmail());
            throw new ValidationException("Этот email уже используется");
        }
    }

    public static User makeUser(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("user_id"))
                .name(rs.getString("user_name"))
                .login(rs.getString("user_login"))
                .email(rs.getString("user_email"))
                .birthday(toLocalDate(rs.getDate("user_birthday")))
                .build();
    }
}
