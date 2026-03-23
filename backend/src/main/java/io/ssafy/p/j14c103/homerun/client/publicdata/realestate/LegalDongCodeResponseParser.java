package io.ssafy.p.j14c103.homerun.client.publicdata.realestate;

import io.ssafy.p.j14c103.homerun.config.ConditionalOnRealEstateImportEnabled;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.io.StringReader;
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
public class LegalDongCodeResponseParser {

    private static final String SUCCESS_RESULT_CODE = "INFO-0";
    private static final String NO_DATA_RESULT_CODE = "INFO-3";
    private static final String DISTRICT_LEVEL_UMD_CODE = "000";

    public List<LegalDongCodeRow> parse(String xml) {
        try {
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

            NodeList rowNodes = root.getElementsByTagName("row");
            List<LegalDongCodeRow> rows = new ArrayList<>();
            for (int i = 0; i < rowNodes.getLength(); i++) {
                Element row = (Element) rowNodes.item(i);

                String sidoCode = readText(row, "sido_cd");
                String sggCode = readText(row, "sgg_cd");
                String umdCode = readText(row, "umd_cd");

                rows.add(new LegalDongCodeRow(
                    sidoCode,
                    sidoCode + sggCode,
                    readText(row, "region_cd"),
                    readText(row, "locathigh_cd"),
                    DISTRICT_LEVEL_UMD_CODE.equals(umdCode),
                    readText(row, "locallow_nm"),
                    readText(row, "locatadd_nm")
                ));
            }

            return rows;
        } catch (HomerunException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error(
                "법정동 코드 XML 파싱 실패 rawLength={}, rawPrefix={}, normalizedPrefix={}, rawLeadingCodePoints={}, normalizedLeadingCodePoints={}",
                lengthOf(xml),
                preview(xml, 200),
                preview(normalizeXml(xml), 200),
                leadingCodePoints(xml, 8),
                leadingCodePoints(normalizeXml(xml), 8),
                exception
            );
            throw new HomerunException(ErrorCode.GLOBAL_SERIALIZATION_ERROR, exception);
        }
    }

    private boolean isNoDataResponse(String resultCode, String resultMsg) {
        return NO_DATA_RESULT_CODE.equals(resultCode) || resultMsg.contains("데이터없음");
    }

    private HomerunException invalidResponse(String resultCode, String resultMsg) {
        log.warn(
            "법정동 코드 응답 resultCode가 예상과 다릅니다. resultCode={}, resultMsg={}",
            resultCode,
            resultMsg
        );
        return new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
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

    private String normalizeXml(String xml) {
        if (xml == null) {
            return "";
        }

        String normalized = xml.stripLeading();
        if (!normalized.isEmpty() && normalized.charAt(0) == '\uFEFF') {
            normalized = normalized.substring(1).stripLeading();
        }

        int firstAngleBracket = normalized.indexOf('<');
        if (firstAngleBracket > 0) {
            normalized = normalized.substring(firstAngleBracket);
        }

        return normalized;
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

    private String readText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() == 0) {
            return "";
        }
        return nodeList.item(0).getTextContent().trim();
    }

    public record LegalDongCodeRow(
        String regionCode,
        String districtCode,
        String legalDongCode,
        String parentLegalDongCode,
        boolean districtLevel,
        String legalDongName,
        String fullAddressName
    ) {
    }
}
