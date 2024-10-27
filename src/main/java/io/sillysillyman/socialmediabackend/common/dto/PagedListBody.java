package io.sillysillyman.socialmediabackend.common.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PagedListBody<T>(
    List<T> content,
    int number,
    int totalPages,
    int pageSize,
    long totalElements
) {

    public static <T> PagedListBody<T> from(Page<T> page) {
        return new PagedListBody<>(
            page.getContent(),
            page.getNumber(),
            page.getTotalPages(),
            page.getSize(),
            page.getTotalElements()
        );
    }
}
