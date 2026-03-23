package io.ssafy.p.j14c103.homerun.api.service.world.housing.request;

import java.util.List;

public class RealEstateMasterImportRequest {

    private final String legalDongXml;
    private final List<String> apartmentTradeResponses;

    private RealEstateMasterImportRequest(
        String legalDongXml,
        List<String> apartmentTradeResponses
    ) {
        this.legalDongXml = legalDongXml;
        this.apartmentTradeResponses = apartmentTradeResponses;
    }

    public static RealEstateMasterImportRequest of(
        String legalDongXml,
        List<String> apartmentTradeResponses
    ) {
        return new RealEstateMasterImportRequest(legalDongXml, apartmentTradeResponses);
    }

    public String getLegalDongXml() {
        return legalDongXml;
    }

    public List<String> getApartmentTradeResponses() {
        return apartmentTradeResponses;
    }
}
