package io.ssafy.p.j14c103.homerun.domain.gamesession.turn;

import java.util.Optional;

public interface TurnDraftRepository {

    void save(TurnDraft turnDraft);

    Optional<TurnDraft> findBySessionId(Long sessionId);

    void deleteBySessionId(Long sessionId);
}
