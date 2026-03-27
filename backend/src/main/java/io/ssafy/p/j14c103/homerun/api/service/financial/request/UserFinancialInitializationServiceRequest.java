package io.ssafy.p.j14c103.homerun.api.service.financial.request;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserFinancialInitializationServiceRequest {

    private List<NamedAmountItem> depositItems;
    private List<NamedAmountItem> loanItems;

    @Builder
    private UserFinancialInitializationServiceRequest(
            final List<NamedAmountItem> depositItems,
            final List<NamedAmountItem> loanItems
    ) {
        this.depositItems = normalizeNamedItems(depositItems);
        this.loanItems = normalizeNamedItems(loanItems);
    }

    private List<NamedAmountItem> normalizeNamedItems(final List<NamedAmountItem> items) {
        if (items == null) {
            return List.of();
        }
        return List.copyOf(items);
    }

    @Getter
    @NoArgsConstructor
    public static class NamedAmountItem {

        private String name;
        private Integer amount;

        @Builder
        private NamedAmountItem(final String name, final Integer amount) {
            this.name = name;
            this.amount = amount;
        }
    }
}
