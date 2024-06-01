package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.DbUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private UserStorage dbUserStorage;

    public User create(User user) {
        return dbUserStorage.create(user);
    }

    public User update(User newUser) {
        validateId(newUser.getId());
        return dbUserStorage.update(newUser);
    }

    public Collection<User> findAll() {
        return dbUserStorage.findAll();
    }

    public void addFriend(long id, long friendId) {
        validateId(id);
        validateId(friendId);

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
        List<String> existingStatus = dbUserStorage
                .getJdbcTemplate()
                .queryForList(querySelect, String.class, id, friendId);

        if (existingStatus.isEmpty()) {
            // Если записи нет, вставляем новую с состоянием unconfirmed
            dbUserStorage.getJdbcTemplate().update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(queryInsert);
                stmt.setLong(1, id);
                stmt.setLong(2, friendId);
                stmt.setString(3, "unconfirmed");
                return stmt;
            });
        } else if (existingStatus.getFirst().equals("unconfirmed")) {
            // Если запись существует с состоянием unconfirmed, обновляем на confirmed
            dbUserStorage.getJdbcTemplate().update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(queryUpdate);
                stmt.setLong(1, id);
                stmt.setLong(2, friendId);
                return stmt;
            });
        }
    }

    public User findUserById(long id) {
        validateId(id);
        return dbUserStorage.findUserById(id);
    }

    public void deleteFriend(long id, long friendId) {
        validateId(id);
        validateId(friendId);
        final String sqlQuery = """
                DELETE FROM friendship AS u
                WHERE accepting_user_id = ? AND requesting_user_id = ?
                """;
        dbUserStorage.getJdbcTemplate().update(sqlQuery, id, friendId);
    }

    public List<User> getFriends(Long id) {
        validateId(id);
        final String sqlQuery = """
                SELECT * FROM users AS u
                JOIN friendship AS f ON u.user_id = f.requesting_user_id
                WHERE f.accepting_user_id = ?
                """;
        return dbUserStorage.getJdbcTemplate().query(sqlQuery, DbUserStorage::makeUser, id);
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        validateId(id);
        validateId(otherId);
        final String query = """
                SELECT u.* FROM users AS u
                JOIN friendship f1 ON u.user_id = f1.requesting_user_id AND f1.accepting_user_id = ?
                JOIN friendship f2 ON u.user_id = f2.requesting_user_id AND f2.accepting_user_id = ?
                """;
        return dbUserStorage.getJdbcTemplate().query(query, DbUserStorage::makeUser, id, otherId);
    }

    private void validateId(Long id) {
        final String sqlQuery = "SELECT * FROM users WHERE user_id = ?";
        final List<User> users = dbUserStorage.getJdbcTemplate().query(sqlQuery, DbUserStorage::makeUser, id);
        if (users.size() != 1) {
            throw new NotFoundException("user id = " + id);
        }
    }
}
