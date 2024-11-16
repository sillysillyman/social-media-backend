package io.sillysillyman.api.util;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

public final class MockMvcTestUtil {

    private MockMvcTestUtil() {
    }

    public static void performPost(
        MockMvc mockMvc,
        String url,
        String content,
        ResultMatcher... matchers
    ) {
        try {
            mockMvc.perform(post(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                )
                .andExpectAll(matchers);
        } catch (Exception e) {
            throw new RuntimeException("POST 요청 실패: " + url, e);
        }
    }

    public static void performGet(MockMvc mockMvc, String url, ResultMatcher... matchers) {
        try {
            mockMvc.perform(get(url)).andExpectAll(matchers);
        } catch (Exception e) {
            throw new RuntimeException("GET 요청 실패: " + url, e);
        }
    }

    public static void performPut(
        MockMvc mockMvc,
        String url,
        String content,
        ResultMatcher... matchers
    ) {
        try {
            mockMvc.perform(put(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                )
                .andExpectAll(matchers);
        } catch (Exception e) {
            throw new RuntimeException("PUT 요청 실패: " + url, e);
        }
    }

    public static void performDelete(MockMvc mockMvc, String url, ResultMatcher... matchers) {
        try {
            mockMvc.perform(delete(url)).andExpectAll(matchers);
        } catch (Exception e) {
            throw new RuntimeException("DELETE 요청 실패: " + url, e);
        }
    }
}
