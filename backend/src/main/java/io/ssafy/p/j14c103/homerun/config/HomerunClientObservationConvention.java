package io.ssafy.p.j14c103.homerun.config;

import io.micrometer.common.KeyValues;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention;
import org.springframework.util.Assert;

final class HomerunClientObservationConvention extends DefaultClientRequestObservationConvention {

    static final String CLIENT_TAG = "client";

    private final String client;

    HomerunClientObservationConvention(final String client) {
        Assert.hasText(client, "client must not be blank");
        this.client = client;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(final ClientRequestObservationContext context) {
        return super.getLowCardinalityKeyValues(context)
                .and(CLIENT_TAG, client);
    }
}
