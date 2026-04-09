package io.ssafy.p.j14c103.homerun.api.service.user;

import io.ssafy.p.j14c103.homerun.domain.account.AccountType;

public final class SsafyLinkSupport {

    private static final String LOCAL_MOCK_USER_KEY_PREFIX = "local-mock-user-";

    private SsafyLinkSupport() {
    }

    public static String createLocalMockUserKey(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        return LOCAL_MOCK_USER_KEY_PREFIX + userId;
    }

    public static boolean isLocalMockUserKey(final String userKey) {
        if (userKey == null || userKey.isBlank()) {
            return false;
        }
        return userKey.startsWith(LOCAL_MOCK_USER_KEY_PREFIX);
    }

    public static String createLocalMockAccountNumber(
            final Long userId,
            final AccountType accountType
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (accountType == null) {
            throw new IllegalArgumentException("계좌 유형은 필수입니다.");
        }
        return "LOCAL-" + accountType.name() + "-" + userId;
    }
}
