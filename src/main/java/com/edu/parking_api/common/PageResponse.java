package com.edu.parking_api.common;

import java.util.List;

/**
 * Formato JSON de una lista paginada. Jackson solo serializa los componentes
 * del record, por eso totalPages es un campo y no un método calculado.
 */
public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {

    public static <T> PageResponse<T> from(PageResult<T> result) {
        return new PageResponse<>(
                result.items(),
                result.page(),
                result.size(),
                result.totalItems(),
                result.totalPages()
        );
    }
}
