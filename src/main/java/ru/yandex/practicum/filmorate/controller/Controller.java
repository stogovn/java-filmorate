package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public abstract class Controller<T> {
    // вспомогательный метод для генерации идентификатора для контроллеров
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
