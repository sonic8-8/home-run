package io.ssafy.p.j14c103.homerun.api.service.game.realestate;

import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

@Service
public class RealEstateRegistryRandomService {

    public int nextGapguIndex(final int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    public int nextEulguIndex(final int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }
}
