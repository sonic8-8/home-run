package io.ssafy.p.j14c103.homerun.api.controller.game.start;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.game.start.GameStartProfileService;
import io.ssafy.p.j14c103.homerun.api.service.game.start.response.ProfileOptionsResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
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

@WebMvcTest(GameStartProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class GameStartProfileControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameStartProfileService gameStartProfileService;

    @DisplayName("프로필 선택지 조회 응답을 반환한다.")
    @Test
    void getProfiles() throws Exception {
        // given
        final ProfileOptionsResponse response = ProfileOptionsResponse.from(List.of(
            ProfileOptionsResponse.ProfileOptionResponse.of(
                "JUNIOR_DEVELOPER",
                "신입 개발자",
                JobType.MID_BIZ,
                32_000_000L,
                10_000_000L,
                60,
                70,
                70,
                50,
                50
            ),
            ProfileOptionsResponse.ProfileOptionResponse.of(
                "CORPORATE_OFFICE_WORKER",
                "대기업 사무직",
                JobType.LARGE_BIZ,
                42_000_000L,
                10_000_000L,
                80,
                60,
                90,
                40,
                70
            ),
            ProfileOptionsResponse.ProfileOptionResponse.of(
                "IT_STARTUP",
                "IT 스타트업",
                JobType.STARTUP,
                31_000_000L,
                10_000_000L,
                55,
                55,
                35,
                85,
                80
            )
        ));
        given(gameStartProfileService.getProfileOptions()).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/games/profiles")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.profiles.length()").value(3))
            .andExpect(jsonPath("$.data.profiles[0].profileCode").value("JUNIOR_DEVELOPER"))
            .andExpect(jsonPath("$.data.profiles[0].name").value("신입 개발자"))
            .andExpect(jsonPath("$.data.profiles[0].jobType").value("MID_BIZ"))
            .andExpect(jsonPath("$.data.profiles[0].annualSalary").value(32000000))
            .andExpect(jsonPath("$.data.profiles[0].initialCash").value(10000000))
            .andExpect(jsonPath("$.data.profiles[0].stats.salary").value(60))
            .andExpect(jsonPath("$.data.profiles[0].stats.health").value(70))
            .andExpect(jsonPath("$.data.profiles[0].stats.stability").value(70))
            .andExpect(jsonPath("$.data.profiles[0].stats.growthSpeed").value(50))
            .andExpect(jsonPath("$.data.profiles[0].stats.difficulty").value(50))
            .andExpect(jsonPath("$.data.profiles[1].profileCode").value("CORPORATE_OFFICE_WORKER"))
            .andExpect(jsonPath("$.data.profiles[1].name").value("대기업 사무직"))
            .andExpect(jsonPath("$.data.profiles[1].jobType").value("LARGE_BIZ"))
            .andExpect(jsonPath("$.data.profiles[1].annualSalary").value(42000000))
            .andExpect(jsonPath("$.data.profiles[2].profileCode").value("IT_STARTUP"))
            .andExpect(jsonPath("$.data.profiles[2].name").value("IT 스타트업"))
            .andExpect(jsonPath("$.data.profiles[2].jobType").value("STARTUP"))
            .andExpect(jsonPath("$.data.profiles[2].annualSalary").value(31000000))
            .andDo(document("game-start/profiles/success",
                    requestHeaders(authorizationHeader()),
                    apiResponseFields(
                            "프로필 선택지 목록",
                            fieldWithPath("profiles").type(JsonFieldType.ARRAY).description("프로필 선택지 목록"),
                            fieldWithPath("profiles[].profileCode").type(JsonFieldType.STRING).description("프로필 코드"),
                            fieldWithPath("profiles[].name").type(JsonFieldType.STRING).description("프로필 이름"),
                            fieldWithPath("profiles[].jobType").type(JsonFieldType.STRING).description("직업 타입"),
                            fieldWithPath("profiles[].annualSalary").type(JsonFieldType.NUMBER).description("연봉"),
                            fieldWithPath("profiles[].initialCash").type(JsonFieldType.NUMBER).description("초기 현금"),
                            fieldWithPath("profiles[].stats").type(JsonFieldType.OBJECT).description("프로필 능력치 정보"),
                            fieldWithPath("profiles[].stats.salary").type(JsonFieldType.NUMBER).description("급여 능력치"),
                            fieldWithPath("profiles[].stats.health").type(JsonFieldType.NUMBER).description("체력 능력치"),
                            fieldWithPath("profiles[].stats.stability").type(JsonFieldType.NUMBER).description("안정성 능력치"),
                            fieldWithPath("profiles[].stats.growthSpeed").type(JsonFieldType.NUMBER).description("성장 속도 능력치"),
                            fieldWithPath("profiles[].stats.difficulty").type(JsonFieldType.NUMBER).description("난이도 능력치")
                    )
            ));
    }
}
