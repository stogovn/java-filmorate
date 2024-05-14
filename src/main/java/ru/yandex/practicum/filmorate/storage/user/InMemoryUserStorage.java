package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@Slf4j
@Component
public class InMemoryUserStorage extends Storage<User> implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        // проверяем выполнение необходимых условий
        if (user.getLogin().contains(" ")) {
            log.error("При попытке создания пользователя неверно указан login");
            throw new ValidationException("Логин не должен быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
            log.debug("Новому пользователю присвоилось имя = {}", user.getLogin());
        }
        validate(user);
        // формируем дополнительные данные
        user.setId(getNextId(users));
        user.setFriends(new HashSet<>());
        // сохраняем нового пользователя в памяти приложения
        users.put(user.getId(), user);
        log.info("Создался новый пользователь с id = {}", user.getId());
        return user;
    }

    @Override
    public User update(User newUser) {
        // проверяем необходимые условия
        if (newUser.getId() == null) {
            log.error("При попытке обновления пользователя не был указан id");
            throw new ValidationException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            validate(newUser);
            User oldUser = users.get(newUser.getId());
            // если пользователь найден и все условия соблюдены, обновляем его содержимое
            oldUser.setName(newUser.getName());
            oldUser.setBirthday(newUser.getBirthday());
            oldUser.setEmail(newUser.getEmail());
            oldUser.setLogin(newUser.getLogin());
            log.info("Обновили пользователя с id = {}", newUser.getId());
            return oldUser;
        }
        log.error("При попытке обновления пользователя указан не существующий id: {}", newUser.getId());
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        validateId(id);
        validateId(friendId);
        users.get(id).getFriends().add(friendId);
        log.info("Пользователю с id = {} добавился друг с id = {}", id, friendId);
        users.get(friendId).getFriends().add(id);
        log.info("Пользователю с id = {} добавился друг с id = {}", friendId, id);
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return users.values().stream()
                .filter(x -> x.getId().equals(id))
                .findFirst();
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        validateId(id);
        validateId(friendId);
        users.get(id).getFriends().removeIf(x -> x.equals(friendId));
        log.info("У пользователя с id = {} удалили друга с id = {}", id, friendId);
        users.get(friendId).getFriends().removeIf(x -> x.equals(id));
        log.info("У пользователя с id = {} удалили друга с id = {}", friendId, id);
    }

    @Override
    public List<Optional<User>> getFriends(Long id) {
        validateId(id);
        return users.get(id).getFriends().stream()
                .map(this::findUserById)
                .toList();
    }

    @Override
    public List<Optional<User>> getCommonFriends(Long id, Long otherId) {
        validateId(id);
        validateId(otherId);
        Set<Long> setId = users.get(id).getFriends();
        Set<Long> setOtherId = users.get(otherId).getFriends();
        setId.retainAll(setOtherId);
        log.info("У пользователей с id = {} и id = {} общие друзья: {}", id, otherId, setId);
        return setId.stream()
                .map(this::findUserById)
                .toList();
    }

    @Override
    protected void validate(User user) {
        for (User u : users.values()) {
            if (u.getEmail().equals(user.getEmail())) {
                log.error("При попытке обновления пользователя указан существующий email: {}", u.getEmail());
                throw new ValidationException("Этот email уже используется");
            }
        }
    }

    public void validateId(Long id) {
        if (!users.containsKey(id)) {
            log.error("Указан не существующий пользователь с id: {}", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
    }
}
