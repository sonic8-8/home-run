package io.ssafy.p.j14c103.homerun.client.publicdata.realestate;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
public class ApartmentTradeXmlParser {

    public List<ApartmentTradeRow> parse(String xml) {
        try {
            Document document = parseDocument(xml);
            String resultCode = readText(document.getDocumentElement(), "resultCode");
            if (!"000".equals(resultCode)) {
                throw new HomerunException(ErrorCode.GLOBAL_SERIALIZATION_ERROR);
            }

            NodeList itemNodes = document.getElementsByTagName("item");
            List<ApartmentTradeRow> rows = new ArrayList<>();
            for (int i = 0; i < itemNodes.getLength(); i++) {
                Element item = (Element) itemNodes.item(i);

                rows.add(new ApartmentTradeRow(
                    readText(item, "sggCd"),
                    readText(item, "umdNm"),
                    readText(item, "aptNm"),
                    readText(item, "jibun"),
                    LocalDate.of(
                        parseInt(item, "dealYear"),
                        parseInt(item, "dealMonth"),
                        parseInt(item, "dealDay")
                    ),
                    toMoney(readText(item, "dealAmount")),
                    new BigDecimal(readText(item, "excluUseAr")),
                    parseInt(item, "floor"),
                    parseInt(item, "buildYear"),
                    "Y".equalsIgnoreCase(readText(item, "landLeaseholdGbn"))
                ));
            }

            return rows;
        } catch (HomerunException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new HomerunException(ErrorCode.GLOBAL_SERIALIZATION_ERROR, exception);
        }
    }

    private Document parseDocument(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);
        factory.setNamespaceAware(false);

        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private Money toMoney(String dealAmount) {
        String normalized = dealAmount.replace(",", "").trim();
        return Money.of(Long.parseLong(normalized) * 10_000L);
    }

    private int parseInt(Element parent, String tagName) {
        return Integer.parseInt(readText(parent, tagName));
    }

    private String readText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() == 0) {
            return "";
        }
        return nodeList.item(0).getTextContent().trim();
    }

    public record ApartmentTradeRow(
        String districtCode,
        String legalDongName,
        String apartmentName,
        String jibun,
        LocalDate dealDate,
        Money dealAmount,
        BigDecimal exclusiveArea,
        int floor,
        int buildYear,
        boolean landLeasehold
    ) {
    }
}
