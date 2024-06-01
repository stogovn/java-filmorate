package utils;

import java.util.Map;

public abstract class StorageUtils<T> {
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
