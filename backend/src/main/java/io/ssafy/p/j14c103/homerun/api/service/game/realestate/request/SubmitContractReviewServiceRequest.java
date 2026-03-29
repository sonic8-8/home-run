package io.ssafy.p.j14c103.homerun.api.service.game.realestate.request;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubmitContractReviewServiceRequest {

    private List<String> checkedTraps;

    @Builder(access = AccessLevel.PRIVATE)
    private SubmitContractReviewServiceRequest(final List<String> checkedTraps) {
        validateCheckedTraps(checkedTraps);
        this.checkedTraps = List.copyOf(checkedTraps);
    }

    public static SubmitContractReviewServiceRequest of(final List<String> checkedTraps) {
        return SubmitContractReviewServiceRequest.builder()
            .checkedTraps(checkedTraps)
            .build();
    }

    private void validateCheckedTraps(final List<String> checkedTraps) {
        if (checkedTraps == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
