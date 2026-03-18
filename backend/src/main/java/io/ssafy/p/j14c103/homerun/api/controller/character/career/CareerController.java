package io.ssafy.p.j14c103.homerun.api.controller.character.career;

import io.ssafy.p.j14c103.homerun.api.service.character.career.CareerQueryService;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobTypeOptionsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class CareerController {

    private final CareerQueryService careerQueryService;

    public CareerController(final CareerQueryService careerQueryService) {
        this.careerQueryService = careerQueryService;
    }

    @GetMapping("/job-types")
    public ResponseEntity<JobTypeOptionsResponse> getJobTypes() {
        final JobTypeOptionsResponse response = careerQueryService.getJobTypeOptions();
        return ResponseEntity.ok(response);
    }
}
