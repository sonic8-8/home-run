package io.ssafy.p.j14c103.homerun.api.service.character.schedule;

import io.ssafy.p.j14c103.homerun.api.service.character.schedule.request.TurnSlotPreviewRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.schedule.response.TurnSlotPreviewResponse;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.TurnSlotPreviewPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.springframework.stereotype.Service;

@Service
public class TurnSlotPreviewService {

    private final TurnSlotPreviewPolicy turnSlotPreviewPolicy;

    public TurnSlotPreviewService() {
        this(new TurnSlotPreviewPolicy());
    }

    TurnSlotPreviewService(final TurnSlotPreviewPolicy turnSlotPreviewPolicy) {
        if (turnSlotPreviewPolicy == null) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }

        this.turnSlotPreviewPolicy = turnSlotPreviewPolicy;
    }

    public TurnSlotPreviewResponse preview(final TurnSlotPreviewRequest request) {
        if (request == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }

        return TurnSlotPreviewResponse.from(turnSlotPreviewPolicy.preview(
            request.gameStat(),
            request.toRequestedSlots()
        ));
    }
}
