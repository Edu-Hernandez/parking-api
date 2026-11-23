package com.edu.parking_api.common;

/**
 * Reglas comunes de paginación: valores fuera de rango se ajustan en vez de fallar.
 */
public final class Paging {

    public static final int MAX_PAGE_SIZE = 100;

    private Paging() {}

    public static int page(int page) {
        return Math.max(page, 0);
    }

    public static int size(int size) {
        return Math.clamp(size, 1, MAX_PAGE_SIZE);
    }
}
