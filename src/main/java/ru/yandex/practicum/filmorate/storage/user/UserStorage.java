package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User create(User data);

    User update(User data);

    Collection<User> findAll();

    void addFriend(Long id, Long friendId);

    Optional<User> findUserById(Long id);

    void deleteFriend(Long id, Long friendId);

    List<Optional<User>> getFriends(Long id);

    List<Optional<User>> getCommonFriends(Long id, Long otherId);
}
