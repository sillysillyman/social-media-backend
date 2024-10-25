package io.sillysillyman.socialmediabackend.common.dto;

import java.util.List;

public record PagedListBody<T>(
    List<T> data,
    int currentPage,
    int totalPages,
    int pageSize,
    long totalElements
) {

}
