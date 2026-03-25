package io.ssafy.p.j14c103.homerun.api.controller.image;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.image.ImageStorageService;
import io.ssafy.p.j14c103.homerun.api.service.image.response.ImageDownloadResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ImageController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
@TestPropertySource(properties = "oci.enabled=true")
class ImageControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImageStorageService imageStorageService;

    @DisplayName("이미지 다운로드 응답에 헤더와 바이너리를 담아 반환한다")
    @Test
    void download_success() throws Exception {
        final byte[] content = new byte[]{1, 2, 3};

        given(imageStorageService.download("test.jpg"))
            .willReturn(ImageDownloadResponse.of("test.jpg", "image/jpeg", 3L, content));

        mockMvc.perform(get("/api/v1/images")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .param("objectName", "test.jpg"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "image/jpeg"))
            .andExpect(header().string("Content-Length", "3"))
            .andExpect(header().string("Content-Disposition", Matchers.containsString("test.jpg")))
            .andExpect(content().bytes(content))
            .andDo(document("image/download/success",
                    requestHeaders(authorizationHeader()),
                    queryParameters(
                            parameterWithName("objectName").description("다운로드할 이미지 오브젝트 이름")
                    ),
                    responseHeaders(
                            headerWithName("Content-Type").description("응답 콘텐츠 타입"),
                            headerWithName("Content-Length").description("응답 바이트 길이"),
                            headerWithName("Content-Disposition").description("다운로드 파일명 정보")
                    )
            ));
    }
}
