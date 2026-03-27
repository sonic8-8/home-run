package io.ssafy.p.j14c103.homerun.api.service.user.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserAssetLinkResponse {

    @Getter(AccessLevel.NONE)
    private boolean assetLinked;
    private boolean mainAccountCreated;
    private boolean seedmoneyAccountCreated;
    private boolean summaryInitialized;

    @Builder
    private UserAssetLinkResponse(
            final boolean isAssetLinked,
            final boolean mainAccountCreated,
            final boolean seedmoneyAccountCreated,
            final boolean summaryInitialized
    ) {
        this.assetLinked = isAssetLinked;
        this.mainAccountCreated = mainAccountCreated;
        this.seedmoneyAccountCreated = seedmoneyAccountCreated;
        this.summaryInitialized = summaryInitialized;
    }

    public static UserAssetLinkResponse of(
            final boolean isAssetLinked,
            final boolean mainAccountCreated,
            final boolean seedmoneyAccountCreated,
            final boolean summaryInitialized
    ) {
        return UserAssetLinkResponse.builder()
                .isAssetLinked(isAssetLinked)
                .mainAccountCreated(mainAccountCreated)
                .seedmoneyAccountCreated(seedmoneyAccountCreated)
                .summaryInitialized(summaryInitialized)
                .build();
    }

    @JsonProperty("isAssetLinked")
    public boolean isAssetLinked() {
        return assetLinked;
    }
}
