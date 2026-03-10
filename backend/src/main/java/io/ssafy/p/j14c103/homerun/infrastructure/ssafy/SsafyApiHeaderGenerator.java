package io.ssafy.p.j14c103.homerun.infrastructure.ssafy;

import io.ssafy.p.j14c103.homerun.config.SsafyApiProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SsafyApiHeaderGenerator {

    private static final String INSTITUTION_CODE = "00100";
    private static final String FINTECH_APP_NO = "001";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

    private final SsafyApiProperties properties;
    private final AtomicLong sequence;

    public SsafyApiHeaderGenerator(final SsafyApiProperties properties) {
        this.properties = properties;
        this.sequence = new AtomicLong(0);
    }

    public Map<String, String> generate(final String apiName, final String userKey) {
        if (apiName == null || apiName.isBlank()) {
            throw new IllegalArgumentException("API 이름은 필수입니다.");
        }

        final LocalDateTime now = LocalDateTime.now();
        final Map<String, String> header = new LinkedHashMap<>();

        header.put("apiName", apiName);
        header.put("transmissionDate", now.format(DATE_FORMATTER));
        header.put("transmissionTime", now.format(TIME_FORMATTER));
        header.put("institutionCode", INSTITUTION_CODE);
        header.put("fintechAppNo", FINTECH_APP_NO);
        header.put("apiServiceCode", apiName);
        header.put("institutionTransactionUniqueNo", generateTransactionUniqueNo(now));
        header.put("apiKey", properties.getApiKey());

        if (userKey != null && !userKey.isBlank()) {
            header.put("userKey", userKey);
        }

        return header;
    }

    private String generateTransactionUniqueNo(final LocalDateTime now) {
        final String dateTimePart = now.format(DATE_FORMATTER) + now.format(TIME_FORMATTER);
        final long seq = sequence.incrementAndGet() % 1_000_000;
        return dateTimePart + String.format("%06d", seq);
    }
}
