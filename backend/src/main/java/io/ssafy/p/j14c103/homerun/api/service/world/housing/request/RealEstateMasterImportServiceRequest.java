package io.ssafy.p.j14c103.homerun.api.service.world.housing.request;

import java.util.List;

public class RealEstateMasterImportServiceRequest {

    private final String legalDongXml;
    private final List<String> apartmentTradeXmls;

    private RealEstateMasterImportServiceRequest(
        String legalDongXml,
        List<String> apartmentTradeXmls
    ) {
        this.legalDongXml = legalDongXml;
        this.apartmentTradeXmls = apartmentTradeXmls;
    }

    public static RealEstateMasterImportServiceRequest of(
        String legalDongXml,
        List<String> apartmentTradeXmls
    ) {
        return new RealEstateMasterImportServiceRequest(legalDongXml, apartmentTradeXmls);
    }

    public String getLegalDongXml() {
        return legalDongXml;
    }

    public List<String> getApartmentTradeXmls() {
        return apartmentTradeXmls;
    }
}
