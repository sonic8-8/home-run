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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(CareerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CareerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CareerQueryService careerQueryService;

    @DisplayName("직업 선택지 조회 응답을 반환한다.")
    @Test
    void getJobTypes() throws Exception {
        // given
        final JobTypeOptionsResponse response = JobTypeOptionsResponse.from(List.of(
            JobTypeOptionsResponse.JobTypeOptionResponse.of(JobType.LARGE_BIZ, "대기업 직장인", 80, 60, 90, 40, 70),
            JobTypeOptionsResponse.JobTypeOptionResponse.of(JobType.MID_BIZ, "중견기업 직장인", 60, 70, 70, 50, 50),
            JobTypeOptionsResponse.JobTypeOptionResponse.of(JobType.SMALL_BIZ, "중소기업 직장인", 40, 80, 50, 60, 30),
            JobTypeOptionsResponse.JobTypeOptionResponse.of(JobType.STARTUP, "스타트업 직장인", 55, 55, 35, 85, 80),
            JobTypeOptionsResponse.JobTypeOptionResponse.of(JobType.FREELANCER, "프리랜서", 50, 50, 20, 90, 85)
        ));
        given(careerQueryService.getJobTypeOptions()).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/games/job-types"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.jobTypes[0].jobType").value("LARGE_BIZ"))
            .andExpect(jsonPath("$.data.jobTypes[0].label").value("대기업 직장인"))
            .andExpect(jsonPath("$.data.jobTypes[0].stats.salary").value(80))
            .andExpect(jsonPath("$.data.jobTypes[3].jobType").value("STARTUP"))
            .andExpect(jsonPath("$.data.jobTypes[3].stats.growthSpeed").value(85))
            .andExpect(jsonPath("$.data.jobTypes[4].jobType").value("FREELANCER"))
            .andExpect(jsonPath("$.data.jobTypes[4].stats.difficulty").value(85));
    }
}
