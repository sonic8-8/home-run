package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionCatalog;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionCatalog.ActionDefinition;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActionCatalogAvailableActionReader implements AvailableActionReader {

    private final ActionCatalog actionCatalog;

    @Override
    public List<ActionDefinition> read(final GameSession gameSession) {
        return actionCatalog.getDefinitions();
    }
}
