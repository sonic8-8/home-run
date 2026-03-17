package io.ssafy.p.j14c103.homerun.domain.world.housing;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RealEstateChecklistItem {
    //
    private String trapId;
    private String label;
    private Boolean isTrapped;

    private RealEstateChecklistItem(
        String trapId,
        String label,
        Boolean isTrapped
    ) {
        this.trapId = trapId;
        this.label = label;
        this.isTrapped = isTrapped;
    }

    public static RealEstateChecklistItem create(
        String trapId,
        String label,
        Boolean isTrapped
    ) {
        return new RealEstateChecklistItem(
            trapId,
            label,
            isTrapped
        );
    }
}
