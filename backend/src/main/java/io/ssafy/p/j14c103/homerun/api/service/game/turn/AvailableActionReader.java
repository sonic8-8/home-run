package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionCatalog.ActionDefinition;
import java.util.List;

public interface AvailableActionReader {

    List<ActionDefinition> read(GameSession gameSession);
}
