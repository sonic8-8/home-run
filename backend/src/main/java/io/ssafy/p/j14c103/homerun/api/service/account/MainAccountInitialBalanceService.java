package io.ssafy.p.j14c103.homerun.api.service.account;

import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class MainAccountInitialBalanceService {

    private static final long BASE_SEED_OFFSET = 50_000_000L;
    private static final int MIN_BALANCE = 3_000_000;
    private static final int MAX_BALANCE = 10_000_000;
    private static final int BALANCE_UNIT = 10_000;
    private static final int BALANCE_STEP_COUNT = ((MAX_BALANCE - MIN_BALANCE) / BALANCE_UNIT) + 1;
    private static final String RANDOM_KEY = "MAIN_CURRENT_BALANCE_V1";

    public int generateInitialBalance(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        return MIN_BALANCE + randomOf(userId, RANDOM_KEY).nextInt(BALANCE_STEP_COUNT) * BALANCE_UNIT;
    }

    private Random randomOf(final Long userId, final String key) {
        final long seed = (BASE_SEED_OFFSET + userId) * 31 + key.hashCode();
        return new Random(seed);
    }
}
