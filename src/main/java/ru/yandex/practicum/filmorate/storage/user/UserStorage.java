package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User create(User data);

    User update(User data);

    Collection<User> findAll();

    Optional<User> findUserById(long id);
}
