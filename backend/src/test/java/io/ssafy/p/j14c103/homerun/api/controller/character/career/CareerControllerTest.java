package io.ssafy.p.j14c103.homerun.api.controller.character.career;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.character.career.CareerQueryService;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobTypeOptionsResponse;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CareerController.class)
class CareerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CareerQueryService careerQueryService;

    @DisplayName("직업 선택지 조회 응답을 반환한다.")
    @Test
    void getJobTypes() throws Exception {
        // given
        JobTypeOptionsResponse response = JobTypeOptionsResponse.from(List.of(
            JobTypeOptionsResponse.JobTypeOptionResponse.of(JobType.SMALL_BIZ, "중소기업 직장인", 40, 80, 50, 60, 30),
            JobTypeOptionsResponse.JobTypeOptionResponse.of(JobType.MID_BIZ, "중견기업 직장인", 60, 70, 70, 50, 50),
            JobTypeOptionsResponse.JobTypeOptionResponse.of(JobType.LARGE_BIZ, "대기업 직장인", 80, 60, 90, 40, 70),
            JobTypeOptionsResponse.JobTypeOptionResponse.of(JobType.STARTUP, "스타트업 직장인", 55, 55, 35, 85, 80),
            JobTypeOptionsResponse.JobTypeOptionResponse.of(JobType.FREELANCER, "프리랜서", 50, 50, 20, 90, 85)
        ));
        given(careerQueryService.getJobTypeOptions()).willReturn(response);

        // when & then
        mockMvc.perform(get("/games/job-types"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jobTypes[0].jobType").value("SMALL_BIZ"))
            .andExpect(jsonPath("$.jobTypes[0].label").value("중소기업 직장인"))
            .andExpect(jsonPath("$.jobTypes[0].salaryGauge").value(40))
            .andExpect(jsonPath("$.jobTypes[3].jobType").value("STARTUP"))
            .andExpect(jsonPath("$.jobTypes[3].growthSpeedGauge").value(85))
            .andExpect(jsonPath("$.jobTypes[4].jobType").value("FREELANCER"))
            .andExpect(jsonPath("$.jobTypes[4].difficultyGauge").value(85));
    }
}
