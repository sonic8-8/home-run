package io.ssafy.p.j14c103.homerun.client.publicdata.realestate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.config.ConditionalOnRealEstateImportEnabled;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
@ConditionalOnRealEstateImportEnabled
@Slf4j
public class ApartmentTradeResponseParser {

    private static final String SUCCESS_RESULT_CODE = "000";
    private static final String NO_DATA_RESULT_CODE = "INFO-3";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public List<ApartmentTradeRow> parse(String payload) {
        String normalizedPayload = normalizePayload(payload);

        try {
            if (normalizedPayload.startsWith("{")) {
                return parseJson(normalizedPayload);
            }
            return parseXml(normalizedPayload);
        } catch (HomerunException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error(
                "아파트 실거래가 응답 파싱 실패 rawLength={}, rawPrefix={}, normalizedPrefix={}, rawLeadingCodePoints={}, normalizedLeadingCodePoints={}",
                lengthOf(payload),
                preview(payload, 200),
                preview(normalizedPayload, 200),
                leadingCodePoints(payload, 8),
                leadingCodePoints(normalizedPayload, 8),
                exception
            );
            throw new HomerunException(ErrorCode.GLOBAL_SERIALIZATION_ERROR, exception);
        }
    }

    private List<ApartmentTradeRow> parseXml(String xml) throws Exception {
        Document document = parseDocument(xml);
        Element root = document.getDocumentElement();
        String resultCode = readText(root, "resultCode");
        String resultMsg = readText(root, "resultMsg");

        if (isNoDataResponse(resultCode, resultMsg)) {
            return List.of();
        }
        if (!SUCCESS_RESULT_CODE.equals(resultCode)) {
            throw invalidResponse(resultCode, resultMsg);
        }

        NodeList itemNodes = root.getElementsByTagName("item");
        List<ApartmentTradeRow> rows = new ArrayList<>();
        for (int i = 0; i < itemNodes.getLength(); i++) {
            rows.add(toApartmentTradeRow((Element) itemNodes.item(i)));
        }
        return rows;
    }

    private List<ApartmentTradeRow> parseJson(String json) throws Exception {
        JsonNode root = OBJECT_MAPPER.readTree(json);
        JsonNode response = root.path("response");
        JsonNode header = response.path("header");
        String resultCode = readText(header, "resultCode");
        String resultMsg = readText(header, "resultMsg");

        if (isNoDataResponse(resultCode, resultMsg)) {
            return List.of();
        }
        if (!SUCCESS_RESULT_CODE.equals(resultCode)) {
            throw invalidResponse(resultCode, resultMsg);
        }

        JsonNode itemNode = response.path("body").path("items").path("item");
        if (itemNode.isMissingNode() || itemNode.isNull()) {
            return List.of();
        }

        List<ApartmentTradeRow> rows = new ArrayList<>();
        if (itemNode.isArray()) {
            for (JsonNode item : itemNode) {
                rows.add(toApartmentTradeRow(item));
            }
            return rows;
        }

        rows.add(toApartmentTradeRow(itemNode));
        return rows;
    }

    private boolean isNoDataResponse(String resultCode, String resultMsg) {
        return NO_DATA_RESULT_CODE.equals(resultCode) || resultMsg.contains("데이터없음");
    }

    private HomerunException invalidResponse(String resultCode, String resultMsg) {
        log.warn(
            "아파트 실거래가 응답 resultCode가 예상과 다릅니다. resultCode={}, resultMsg={}",
            resultCode,
            resultMsg
        );
        return new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
    }

    private String normalizePayload(String payload) {
        if (payload == null) {
            return "";
        }

        String normalized = payload.stripLeading();
        if (!normalized.isEmpty() && normalized.charAt(0) == '\uFEFF') {
            normalized = normalized.substring(1).stripLeading();
        }

        int firstJsonBracket = normalized.indexOf('{');
        int firstXmlBracket = normalized.indexOf('<');

        if (firstJsonBracket >= 0 && (firstXmlBracket < 0 || firstJsonBracket < firstXmlBracket)) {
            if (firstJsonBracket > 0) {
                return normalized.substring(firstJsonBracket);
            }
            return normalized;
        }

        if (firstXmlBracket > 0) {
            return normalized.substring(firstXmlBracket);
        }

        return normalized;
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

    private int lengthOf(String value) {
        return value == null ? 0 : value.length();
    }

    private String preview(String value, int limit) {
        if (value == null) {
            return "null";
        }

        String escaped = value
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t");

        if (escaped.length() <= limit) {
            return escaped;
        }
        return escaped.substring(0, limit) + "...";
    }

    private String leadingCodePoints(String value, int limit) {
        if (value == null || value.isEmpty()) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder("[");
        int count = 0;
        for (int index = 0; index < value.length() && count < limit; count++) {
            int codePoint = value.codePointAt(index);
            if (count > 0) {
                builder.append(", ");
            }
            builder.append("0x").append(Integer.toHexString(codePoint).toUpperCase());
            index += Character.charCount(codePoint);
        }
        builder.append("]");
        return builder.toString();
    }

    private Money toMoney(String dealAmount) {
        String normalized = dealAmount.replace(",", "").trim();
        return Money.of(Long.parseLong(normalized) * 10_000L);
    }

    private ApartmentTradeRow toApartmentTradeRow(Element item) {
        return new ApartmentTradeRow(
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
        );
    }

    private ApartmentTradeRow toApartmentTradeRow(JsonNode item) {
        return new ApartmentTradeRow(
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
        );
    }

    private int parseInt(Element parent, String tagName) {
        return Integer.parseInt(readText(parent, tagName));
    }

    private int parseInt(JsonNode parent, String tagName) {
        return Integer.parseInt(readText(parent, tagName));
    }

    private String readText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() == 0) {
            return "";
        }
        return nodeList.item(0).getTextContent().trim();
    }

    private String readText(JsonNode parent, String tagName) {
        JsonNode node = parent.path(tagName);
        if (node.isMissingNode() || node.isNull()) {
            return "";
        }
        return node.asText().trim();
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
