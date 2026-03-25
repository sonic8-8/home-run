package io.ssafy.p.j14c103.homerun.domain.world.housing;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RealEstateDocumentChecklistItem {

    private String trapId;
    private String label;
    private Boolean isTrapped;

    private RealEstateDocumentChecklistItem(
        final String trapId,
        final String label,
        final Boolean isTrapped
    ) {
        this.trapId = trapId;
        this.label = label;
        this.isTrapped = isTrapped;
    }

    public static RealEstateDocumentChecklistItem create(
        final String trapId,
        final String label,
        final Boolean isTrapped
    ) {
        return new RealEstateDocumentChecklistItem(trapId, label, isTrapped);
    }
}
