package io.sillysillyman.socialmediabackend.common.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PagedListResponse<T>(
    List<T> content,
    int number,
    int totalPages,
    int pageSize,
    long totalElements
) {

    public static <T> PagedListResponse<T> from(Page<T> page) {
        return new PagedListResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getTotalPages(),
            page.getSize(),
            page.getTotalElements()
        );
    }
}
