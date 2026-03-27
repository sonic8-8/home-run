package io.ssafy.p.j14c103.homerun.api.service.user.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserMeResponse {

    private Long userId;
    private String email;
    private String name;
    @Getter(AccessLevel.NONE)
    private boolean assetLinked;
    private Integer totalAssetAmount;
    private Integer netAssetAmount;

    @Builder
    private UserMeResponse(
            final Long userId,
            final String email,
            final String name,
            final boolean isAssetLinked,
            final Integer totalAssetAmount,
            final Integer netAssetAmount
    ) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.assetLinked = isAssetLinked;
        this.totalAssetAmount = totalAssetAmount;
        this.netAssetAmount = netAssetAmount;
    }

    public static UserMeResponse of(
            final Long userId,
            final String email,
            final String name,
            final boolean isAssetLinked,
            final Integer totalAssetAmount,
            final Integer netAssetAmount
    ) {
        return UserMeResponse.builder()
                .userId(userId)
                .email(email)
                .name(name)
                .isAssetLinked(isAssetLinked)
                .totalAssetAmount(totalAssetAmount)
                .netAssetAmount(netAssetAmount)
                .build();
    }

    @JsonProperty("isAssetLinked")
    public boolean isAssetLinked() {
        return assetLinked;
    }
}
