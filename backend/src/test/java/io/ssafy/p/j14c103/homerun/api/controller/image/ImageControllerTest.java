package io.ssafy.p.j14c103.homerun.api.controller.image;

import io.ssafy.p.j14c103.homerun.api.service.image.ImageStorageService;
import io.ssafy.p.j14c103.homerun.api.service.image.response.ImageDownloadResponse;
import io.ssafy.p.j14c103.homerun.global.GlobalExceptionHandler;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ImageStorageService imageStorageService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ImageController(imageStorageService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @DisplayName("이미지 다운로드 응답에 헤더와 바이너리를 담아 반환한다")
    @Test
    void download_success() throws Exception {
        final byte[] content = new byte[]{1, 2, 3};

        given(imageStorageService.download("test.jpg"))
                .willReturn(new ImageDownloadResponse("test.jpg", "image/jpeg", 3L, content));

        mockMvc.perform(get("/api/v1/images").param("objectName", "test.jpg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(header().string("Content-Length", "3"))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("test.jpg")))
                .andExpect(content().bytes(content));
    }
}
