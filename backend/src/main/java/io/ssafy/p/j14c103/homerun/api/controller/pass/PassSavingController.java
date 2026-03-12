package io.ssafy.p.j14c103.homerun.api.controller.pass;

import io.ssafy.p.j14c103.homerun.api.dto.pass.PassHistoryResponse;
import io.ssafy.p.j14c103.homerun.api.dto.pass.PassSaveRequest;
import io.ssafy.p.j14c103.homerun.api.dto.pass.PassWidgetResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.PassSavingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pass")
public class PassSavingController {

    private final PassSavingService passSavingService;

    public PassSavingController(final PassSavingService passSavingService) {
        this.passSavingService = passSavingService;
    }

    @PostMapping("/save")
    public ResponseEntity<Void> save(@Valid @RequestBody final PassSaveRequest request) {
        passSavingService.save(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/widget")
    public ResponseEntity<PassWidgetResponse> getWidget(@RequestParam final Long userId) {
        final PassWidgetResponse response = passSavingService.getWidget(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<Page<PassHistoryResponse>> getHistory(
            @RequestParam final Long userId,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {
        final Page<PassHistoryResponse> response = passSavingService.getHistory(userId, page, size);
        return ResponseEntity.ok(response);
    }
}
