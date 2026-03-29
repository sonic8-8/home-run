package io.ssafy.p.j14c103.homerun.api.controller.game.realestate.request;

import jakarta.validation.constraints.NotNull;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.request.SubmitContractReviewServiceRequest;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubmitContractReviewRequest {

    @NotNull(message = "{validation.game.realestate.contract.checkedTraps.notNull}")
    private List<String> checkedTraps;

    @Builder(access = AccessLevel.PRIVATE)
    private SubmitContractReviewRequest(final List<String> checkedTraps) {
        this.checkedTraps = checkedTraps;
    }

    public static SubmitContractReviewRequest of(final List<String> checkedTraps) {
        return SubmitContractReviewRequest.builder()
            .checkedTraps(checkedTraps)
            .build();
    }

    public SubmitContractReviewServiceRequest toServiceRequest() {
        return SubmitContractReviewServiceRequest.of(checkedTraps);
    }
}
