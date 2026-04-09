package io.ssafy.p.j14c103.homerun.api.service.world;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Profile("!test")
@RequiredArgsConstructor
public class WorldContentBootstrapService implements ApplicationRunner {

    private final WorldHousingSeedService worldHousingSeedService;
    private final WorldContentSeedService worldContentSeedService;

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        worldHousingSeedService.seed();
        worldContentSeedService.seed();
        log.info("World content bootstrap completed.");
    }
}
