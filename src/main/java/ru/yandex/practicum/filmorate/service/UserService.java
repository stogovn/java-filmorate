package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private InMemoryUserStorage userStorage;

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public void addFriend(long id, long friendId) {
        validateId(id);
        validateId(friendId);
        userStorage.getUsers().get(id).getFriends().add(friendId);
        log.info("Пользователю с id = {} добавился друг с id = {}", id, friendId);
        userStorage.getUsers().get(friendId).getFriends().add(id);
        log.info("Пользователю с id = {} добавился друг с id = {}", friendId, id);
    }

    public Optional<User> findUserById(long id) {
        return userStorage.findUserById(id);
    }

    public void deleteFriend(long id, long friendId) {
        validateId(id);
        validateId(friendId);
        userStorage.getUsers().get(id).getFriends().removeIf(x -> x.equals(friendId));
        log.info("У пользователя с id = {} удалили друга с id = {}", id, friendId);
        userStorage.getUsers().get(friendId).getFriends().removeIf(x -> x.equals(id));
        log.info("У пользователя с id = {} удалили друга с id = {}", friendId, id);
    }

    public List<Optional<User>> getFriends(Long id) {
        validateId(id);
        return userStorage.getUsers().get(id).getFriends().stream()
                .map(this::findUserById)
                .toList();
    }

    public List<Optional<User>> getCommonFriends(Long id, Long otherId) {
        validateId(id);
        validateId(otherId);
        Set<Long> setId = userStorage.getUsers().get(id).getFriends();
        Set<Long> setOtherId = userStorage.getUsers().get(otherId).getFriends();
        setId.retainAll(setOtherId);
        log.info("У пользователей с id = {} и id = {} общие друзья: {}", id, otherId, setId);
        return setId.stream()
                .map(this::findUserById)
                .toList();
    }

    private void validateId(Long id) {
        if (!userStorage.getUsers().containsKey(id)) {
            log.error("Указан не существующий пользователь с id: {}", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
    }
}
