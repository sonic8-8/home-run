package io.ssafy.p.j14c103.homerun.api.service.world.housing.request;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorldPurchaseValidationServiceRequest {

    private Long sessionId;
    private Long propertyId;

    @Builder(access = AccessLevel.PRIVATE)
    private WorldPurchaseValidationServiceRequest(
        final Long sessionId,
        final Long propertyId
    ) {
        validateIds(sessionId, propertyId);

        this.sessionId = sessionId;
        this.propertyId = propertyId;
    }

    public static WorldPurchaseValidationServiceRequest of(
        final Long sessionId,
        final Long propertyId
    ) {
        return WorldPurchaseValidationServiceRequest.builder()
            .sessionId(sessionId)
            .propertyId(propertyId)
            .build();
    }

    private void validateIds(final Long sessionId, final Long propertyId) {
        if (sessionId == null || sessionId <= 0 || propertyId == null || propertyId <= 0) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
