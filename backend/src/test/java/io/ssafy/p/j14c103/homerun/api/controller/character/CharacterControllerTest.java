package io.ssafy.p.j14c103.homerun.api.controller.character;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import io.ssafy.p.j14c103.homerun.api.service.character.CharacterQueryService;
import io.ssafy.p.j14c103.homerun.api.service.character.response.CharacterOptionsResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CharacterController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class CharacterControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CharacterQueryService characterQueryService;

    @DisplayName("캐릭터 선택지 조회 응답을 반환한다.")
    @Test
    void getCharacters() throws Exception {
        // given
        final CharacterOptionsResponse response = CharacterOptionsResponse.from(List.of(
                CharacterOptionsResponse.CharacterOptionResponse.of(CharacterType.FEMALE, "/images/characters/female.png"),
                CharacterOptionsResponse.CharacterOptionResponse.of(CharacterType.MALE, "/images/characters/male.png")
        ));
        given(characterQueryService.getCharacterOptions()).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/games/characters")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.characters[0].characterType").value("FEMALE"))
            .andExpect(jsonPath("$.data.characters[0].thumbnailUrl").value("/images/characters/female.png"))
            .andExpect(jsonPath("$.data.characters[1].characterType").value("MALE"))
            .andExpect(jsonPath("$.data.characters[1].thumbnailUrl").value("/images/characters/male.png"))
            .andDo(document("character/options/success",
                    requestHeaders(authorizationHeader()),
                    apiResponseFields(
                            "캐릭터 선택지 목록",
                            fieldWithPath("characters").type(JsonFieldType.ARRAY).description("캐릭터 선택지 목록"),
                            fieldWithPath("characters[].characterType").type(JsonFieldType.STRING).description("캐릭터 타입"),
                            fieldWithPath("characters[].thumbnailUrl").type(JsonFieldType.STRING).description("캐릭터 썸네일 이미지 URL")
                    )
            ));
    }
}
