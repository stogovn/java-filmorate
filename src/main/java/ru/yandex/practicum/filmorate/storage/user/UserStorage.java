package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {
    User create(User data);

    User update(User data);

    Collection<User> findAll();

    User findUserById(long id);

    void addFriend(long id, long friendId);

    void deleteFriend(long id, long friendId);

    List<User> getFriends(Long id);

    List<User> getCommonFriends(Long id, Long otherId);
}
