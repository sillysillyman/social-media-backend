package io.sillysillyman.core.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Consumer;
import org.springframework.data.domain.Page;

public final class TestUtils {

    private TestUtils() {
    }

    public static <T> void assertPageProperties(
        Page<T> page,
        int expectedContentSize,
        int expectedNumber,
        int expectedPageSize,
        long expectedTotalPageElements,
        Consumer<List<T>> contentAssertion
    ) {
        assertThat(page.getContent()).hasSize(expectedContentSize);
        assertThat(page.getNumber()).isEqualTo(expectedNumber);
        assertThat(page.getSize()).isEqualTo(expectedPageSize);
        assertThat(page.getTotalElements()).isEqualTo(expectedTotalPageElements);
        contentAssertion.accept(page.getContent());
    }
}
