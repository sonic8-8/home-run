package io.ssafy.p.j14c103.homerun.api.controller.game.start;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.game.start.GameStartLocationService;
import io.ssafy.p.j14c103.homerun.api.service.game.start.response.DistrictListResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.start.response.RegionListResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.start.response.TargetPropertyListResponse;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GameStartLocationController.class)
@AutoConfigureMockMvc(addFilters = false)
class GameStartLocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameStartLocationService gameStartLocationService;

    @DisplayName("지역 목록 조회 응답을 반환한다.")
    @Test
    void getRegions() throws Exception {
        // given
        final RegionListResponse response = RegionListResponse.from(List.of(
            RegionListResponse.RegionResponse.of("11", "서울특별시"),
            RegionListResponse.RegionResponse.of("24", "광주광역시")
        ));
        given(gameStartLocationService.getRegions()).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/games/regions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.regions.length()").value(2))
            .andExpect(jsonPath("$.data.regions[0].regionCode").value("11"))
            .andExpect(jsonPath("$.data.regions[0].name").value("서울특별시"))
            .andExpect(jsonPath("$.data.regions[1].regionCode").value("24"))
            .andExpect(jsonPath("$.data.regions[1].name").value("광주광역시"));
    }

    @DisplayName("지역별 구 목록 조회 응답을 반환한다.")
    @Test
    void getDistricts() throws Exception {
        // given
        final DistrictListResponse response = DistrictListResponse.of("11", List.of(
            DistrictListResponse.DistrictResponse.of("11680", "강남구"),
            DistrictListResponse.DistrictResponse.of("11710", "송파구")
        ));
        given(gameStartLocationService.getDistricts("11")).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/games/regions/11/districts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.regionCode").value("11"))
            .andExpect(jsonPath("$.data.districts.length()").value(2))
            .andExpect(jsonPath("$.data.districts[0].districtCode").value("11680"))
            .andExpect(jsonPath("$.data.districts[0].name").value("강남구"))
            .andExpect(jsonPath("$.data.districts[1].districtCode").value("11710"))
            .andExpect(jsonPath("$.data.districts[1].name").value("송파구"));
    }

    @DisplayName("지역과 구 기준의 목표 매물 목록 조회 응답을 반환한다.")
    @Test
    void getTargetProperties() throws Exception {
        // given
        final TargetPropertyListResponse response = TargetPropertyListResponse.from(List.of(
            TargetPropertyListResponse.TargetPropertyResponse.of(
                1L,
                "헬리오시티",
                1_550_000_000L,
                BigDecimal.valueOf(37.4979512),
                BigDecimal.valueOf(127.1127134)
            ),
            TargetPropertyListResponse.TargetPropertyResponse.of(
                2L,
                "잠실엘스",
                2_300_000_000L,
                BigDecimal.valueOf(37.5133012),
                BigDecimal.valueOf(127.1029384)
            )
        ));
        given(gameStartLocationService.getTargetProperties("11", "11710")).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/games/regions/11/districts/11710/properties"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.properties.length()").value(2))
            .andExpect(jsonPath("$.data.properties[0].propertyId").value(1L))
            .andExpect(jsonPath("$.data.properties[0].name").value("헬리오시티"))
            .andExpect(jsonPath("$.data.properties[0].recentPrice").value(1550000000L))
            .andExpect(jsonPath("$.data.properties[0].latitude").value(37.4979512))
            .andExpect(jsonPath("$.data.properties[0].longitude").value(127.1127134))
            .andExpect(jsonPath("$.data.properties[1].propertyId").value(2L))
            .andExpect(jsonPath("$.data.properties[1].name").value("잠실엘스"))
            .andExpect(jsonPath("$.data.properties[1].recentPrice").value(2300000000L));
    }

    @DisplayName("존재하지 않는 지역 코드는 404와 HOUSING_REGION_NOT_FOUND를 반환한다.")
    @Test
    void getDistrictsWithUnknownRegion() throws Exception {
        // given
        given(gameStartLocationService.getDistricts("99"))
            .willThrow(new HomerunException(ErrorCode.HOUSING_REGION_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/games/regions/99/districts"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("HOUSING_003"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 지역입니다."));
    }

    @DisplayName("지역과 맞지 않는 구 코드는 404와 HOUSING_DISTRICT_NOT_FOUND를 반환한다.")
    @Test
    void getTargetPropertiesWithMismatchedDistrict() throws Exception {
        // given
        given(gameStartLocationService.getTargetProperties("11", "24110"))
            .willThrow(new HomerunException(ErrorCode.HOUSING_DISTRICT_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/games/regions/11/districts/24110/properties"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("HOUSING_004"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 구입니다."));
    }
}
