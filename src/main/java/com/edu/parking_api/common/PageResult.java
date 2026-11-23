package com.edu.parking_api.common;

import java.util.List;
import java.util.function.Function;

/**
 * Página de resultados sin dependencias de Spring, para que el dominio y la
 * capa de aplicación no conozcan Spring Data.
 */
public record PageResult<T>(
        List<T> items,
        int page,
        int size,
        long totalItems
) {

    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalItems / size);
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(items.stream().map(mapper).toList(), page, size, totalItems);
    }
}
