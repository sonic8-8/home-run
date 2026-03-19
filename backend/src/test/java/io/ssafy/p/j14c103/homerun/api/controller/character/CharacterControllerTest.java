package io.ssafy.p.j14c103.homerun.api.controller.character;

import io.ssafy.p.j14c103.homerun.api.service.character.CharacterQueryService;
import io.ssafy.p.j14c103.homerun.api.service.character.response.CharacterOptionsResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CharacterController.class)
@AutoConfigureMockMvc(addFilters = false)
class CharacterControllerTest {

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
        mockMvc.perform(get("/api/games/characters"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.characters[0].characterType").value("FEMALE"))
            .andExpect(jsonPath("$.data.characters[0].thumbnailUrl").value("/images/characters/female.png"))
            .andExpect(jsonPath("$.data.characters[1].characterType").value("MALE"))
            .andExpect(jsonPath("$.data.characters[1].thumbnailUrl").value("/images/characters/male.png"));
    }
}
