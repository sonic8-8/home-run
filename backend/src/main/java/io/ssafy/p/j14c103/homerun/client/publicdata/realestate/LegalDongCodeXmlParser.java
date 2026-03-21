package io.ssafy.p.j14c103.homerun.client.publicdata.realestate;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
public class LegalDongCodeXmlParser {

    public List<LegalDongCodeRow> parse(String xml) {
        try {
            Document document = parseDocument(xml);
            String resultCode = readText(document.getDocumentElement(), "resultCode");
            if (!"INFO-0".equals(resultCode)) {
                throw new HomerunException(ErrorCode.GLOBAL_SERIALIZATION_ERROR);
            }

            NodeList rowNodes = document.getElementsByTagName("row");
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
                    "000".equals(umdCode),
                    readText(row, "locallow_nm"),
                    readText(row, "locatadd_nm")
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
