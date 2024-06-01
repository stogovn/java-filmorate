package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User create(User data);

    User update(User data);

    Collection<User> findAll();

    User findUserById(long id);

    void validate(User user);

    JdbcTemplate getJdbcTemplate();
}
