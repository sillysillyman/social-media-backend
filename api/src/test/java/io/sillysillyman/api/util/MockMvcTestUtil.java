package io.sillysillyman.api.util;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.util.function.Consumer;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public final class MockMvcTestUtil {

    private MockMvcTestUtil() {
    }

    public static MvcResult performPost(
        MockMvc mockMvc,
        String url,
        String content,
        ResultMatcher... matchers
    ) {
        try {
            return mockMvc.perform(post(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                )
                .andExpectAll(matchers)
                .andReturn();
        } catch (Exception e) {
            throw new RuntimeException("POST 요청 실패: " + url, e);
        }
    }

    public static void performPost(
        MockMvc mockMvc,
        String url,
        ResultMatcher... matchers
    ) {
        performPost(mockMvc, url, "", matchers);
    }

    public static void performPost(
        MockMvc mockMvc,
        String url,
        Consumer<MockHttpServletRequestBuilder> consumer,
        ResultMatcher... matchers
    ) {
        try {
            MockHttpServletRequestBuilder request = post(url);
            consumer.accept(request);
            mockMvc.perform(request).andExpectAll(matchers);
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
