package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorldContentBootstrapServiceTest {

    @Mock
    private WorldHousingSeedService worldHousingSeedService;

    @Mock
    private WorldContentSeedService worldContentSeedService;

    @DisplayName("런타임 bootstrap은 주거 시드와 월드 콘텐츠 시드를 순서대로 적재한다")
    @Test
    void runSeedsHousingThenWorldContent() {
        // given
        final WorldContentBootstrapService bootstrapService = new WorldContentBootstrapService(
            worldHousingSeedService,
            worldContentSeedService
        );

        // when
        bootstrapService.run(null);

        // then
        final InOrder inOrder = org.mockito.Mockito.inOrder(worldHousingSeedService, worldContentSeedService);
        then(worldHousingSeedService).should(inOrder).seed();
        then(worldContentSeedService).should(inOrder).seed();
    }
}
