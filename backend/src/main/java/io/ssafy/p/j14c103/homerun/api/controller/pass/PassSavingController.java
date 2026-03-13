package io.ssafy.p.j14c103.homerun.api.controller.pass;

import io.ssafy.p.j14c103.homerun.api.controller.pass.request.PassSaveRequest;
import io.ssafy.p.j14c103.homerun.api.service.pass.PassSavingService;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassHistoryResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassWidgetResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pass")
@RequiredArgsConstructor
public class PassSavingController {

    private final PassSavingService passSavingService;

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
