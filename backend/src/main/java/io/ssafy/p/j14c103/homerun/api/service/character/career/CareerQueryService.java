package io.ssafy.p.j14c103.homerun.api.service.character.career;

import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobTypeOptionsResponse;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CareerQueryService {

    public JobTypeOptionsResponse getJobTypeOptions() {
        return JobTypeOptionsResponse.from(List.of(
            JobTypeOptionsResponse.JobTypeOptionResponse.of(
                JobType.SMALL_BIZ,
                "중소기업 직장인",
                40,
                80,
                50,
                60,
                30
            ),
            JobTypeOptionsResponse.JobTypeOptionResponse.of(
                JobType.MID_BIZ,
                "중견기업 직장인",
                60,
                70,
                70,
                50,
                50
            ),
            JobTypeOptionsResponse.JobTypeOptionResponse.of(
                JobType.LARGE_BIZ,
                "대기업 직장인",
                80,
                60,
                90,
                40,
                70
            ),
            JobTypeOptionsResponse.JobTypeOptionResponse.of(
                JobType.STARTUP,
                "스타트업 직장인",
                55,
                55,
                35,
                85,
                80
            ),
            JobTypeOptionsResponse.JobTypeOptionResponse.of(
                JobType.FREELANCER,
                "프리랜서",
                50,
                50,
                20,
                90,
                85
            )
        ));
    }
}
