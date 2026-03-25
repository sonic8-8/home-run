package io.ssafy.p.j14c103.homerun.api.service.character.career;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobTypeOptionsResponse;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CareerQueryServiceTest {

    @Autowired
    private CareerQueryService careerQueryService;

    @DisplayName("직업 선택지 메타데이터를 반환한다.")
    @Test
    void getJobTypeOptions() {
        // when
        final JobTypeOptionsResponse response = careerQueryService.getJobTypeOptions();

        // then
        assertThat(response.jobTypes())
            .extracting(
                JobTypeOptionsResponse.JobTypeOptionResponse::jobType,
                JobTypeOptionsResponse.JobTypeOptionResponse::label,
                option -> option.stats().salary(),
                option -> option.stats().health(),
                option -> option.stats().stability(),
                option -> option.stats().growthSpeed(),
                option -> option.stats().difficulty()
            )
            .containsExactly(
                tuple(JobType.LARGE_BIZ, "대기업 직장인", 80, 60, 90, 40, 70),
                tuple(JobType.MID_BIZ, "중견기업 직장인", 60, 70, 70, 50, 50),
                tuple(JobType.SMALL_BIZ, "중소기업 직장인", 40, 80, 50, 60, 30),
                tuple(JobType.STARTUP, "스타트업 직장인", 55, 55, 35, 85, 80),
                tuple(JobType.FREELANCER, "프리랜서", 50, 50, 20, 90, 85)
            );
    }
}
