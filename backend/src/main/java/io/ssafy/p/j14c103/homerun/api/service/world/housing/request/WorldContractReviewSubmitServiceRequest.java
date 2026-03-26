package io.ssafy.p.j14c103.homerun.api.service.world.housing.request;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorldContractReviewSubmitServiceRequest {

    private Long sessionId;
    private Long propertyId;
    private List<String> checkedTraps;

    @Builder(access = AccessLevel.PRIVATE)
    private WorldContractReviewSubmitServiceRequest(
        final Long sessionId,
        final Long propertyId,
        final List<String> checkedTraps
    ) {
        validateIds(sessionId, propertyId);
        validateCheckedTraps(checkedTraps);

        this.sessionId = sessionId;
        this.propertyId = propertyId;
        this.checkedTraps = List.copyOf(checkedTraps);
    }

    public static WorldContractReviewSubmitServiceRequest of(
        final Long sessionId,
        final Long propertyId,
        final List<String> checkedTraps
    ) {
        return WorldContractReviewSubmitServiceRequest.builder()
            .sessionId(sessionId)
            .propertyId(propertyId)
            .checkedTraps(checkedTraps)
            .build();
    }

    private void validateIds(final Long sessionId, final Long propertyId) {
        if (sessionId == null || sessionId <= 0 || propertyId == null || propertyId <= 0) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateCheckedTraps(final List<String> checkedTraps) {
        if (checkedTraps == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
