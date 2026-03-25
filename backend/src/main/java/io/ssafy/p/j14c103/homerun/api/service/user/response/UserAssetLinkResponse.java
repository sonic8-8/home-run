package io.ssafy.p.j14c103.homerun.api.service.user.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserAssetLinkResponse {

    @JsonProperty("isAssetLinked")
    private boolean isAssetLinked;
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
        this.isAssetLinked = isAssetLinked;
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
}
