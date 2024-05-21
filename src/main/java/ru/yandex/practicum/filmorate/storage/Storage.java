package ru.yandex.practicum.filmorate.storage;

import java.util.Map;

public abstract class Storage<T> {
    protected long getNextId(Map<Long, T> map) {
        long currentMaxId = map.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    protected abstract void validate(T t);
}
