package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

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
        return dbUserStorage.update(newUser);
    }

    public Collection<User> findAll() {
        return dbUserStorage.findAll();
    }

    public void addFriend(long id, long friendId) {
        dbUserStorage.addFriend(id, friendId);
    }

    public User findUserById(long id) {
        return dbUserStorage.findUserById(id);
    }

    public void deleteFriend(long id, long friendId) {
        dbUserStorage.deleteFriend(id, friendId);
    }

    public List<User> getFriends(Long id) {
        return dbUserStorage.getFriends(id);
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        return dbUserStorage.getCommonFriends(id, otherId);
    }
}
