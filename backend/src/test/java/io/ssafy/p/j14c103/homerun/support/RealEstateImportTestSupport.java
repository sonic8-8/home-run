package io.ssafy.p.j14c103.homerun.support;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.real-estate-import.enabled=true")
public abstract class RealEstateImportTestSupport extends IntegrationTestSupport {
}
