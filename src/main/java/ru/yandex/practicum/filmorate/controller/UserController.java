package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController extends Controller<User> {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        // проверяем выполнение необходимых условий
        if (user.getLogin().contains(" ")) {
            log.error("При попытке создания пользователя неверно указан login");
            throw new ValidationException("Логин не должен быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
            log.debug("Новому пользователю присвоилось имя = {}", user.getLogin());
        }
        // формируем дополнительные данные
        user.setId(getNextId(users));
        // сохраняем нового пользователя в памяти приложения
        users.put(user.getId(), user);
        log.info("Создался новый пользователь с id = {}", user.getId());
        return user;
    }



    @PutMapping
    public User update(@RequestBody User newUser) {
        // проверяем необходимые условия
        if (newUser.getId() == null) {
            log.error("При попытке обновления пользователя не был указан id");
            throw new ValidationException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            for (User u : users.values()) {
                if (u.getEmail().equals(newUser.getEmail())) {
                    log.error("При попытке обновления пользователя указан существующий email: {}", u.getEmail());
                    throw new ValidationException("Этот email уже используется");
                }
            }
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
        throw new ValidationException("Пост с id = " + newUser.getId() + " не найден");
    }
}
