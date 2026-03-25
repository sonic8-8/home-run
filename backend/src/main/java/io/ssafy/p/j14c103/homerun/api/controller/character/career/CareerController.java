package io.ssafy.p.j14c103.homerun.api.controller.character.career;

import io.ssafy.p.j14c103.homerun.api.service.character.career.CareerQueryService;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobTypeOptionsResponse;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class CareerController {

    private final CareerQueryService careerQueryService;

    @GetMapping("/job-types")
    public ApiResponse<JobTypeOptionsResponse> getJobTypes() {
        final JobTypeOptionsResponse response = careerQueryService.getJobTypeOptions();
        return ApiResponse.ok(response);
    }
}
