package io.ssafy.p.j14c103.homerun.client.ssafy;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.config.SsafyApiProperties;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SsafyApiHeaderGeneratorTest {

    private static final ZoneId SSAFY_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @DisplayName("SSAFY 헤더는 KST 기준 시각과 거래 고유번호를 생성한다.")
    @Test
    void generateUsesAsiaSeoulTime() {
        final SsafyApiHeaderGenerator generator = new SsafyApiHeaderGenerator(
                SsafyApiProperties.of("https://finopenapi.ssafy.io/ssafy/api/v1/edu", "test-api-key")
        );
        final LocalDateTime before = LocalDateTime.now(SSAFY_ZONE_ID).minusSeconds(1);

        final Map<String, String> header = generator.generate("createDemandDepositAccount", "test-user-key");

        final LocalDateTime after = LocalDateTime.now(SSAFY_ZONE_ID).plusSeconds(1);
        final LocalDateTime transmissionDateTime = LocalDateTime.parse(
                header.get("transmissionDate") + header.get("transmissionTime"),
                DATE_TIME_FORMATTER
        );

        assertThat(transmissionDateTime).isBetween(before, after);
        assertThat(header.get("institutionTransactionUniqueNo"))
                .startsWith(header.get("transmissionDate") + header.get("transmissionTime"));
        assertThat(header.get("apiKey")).isEqualTo("test-api-key");
        assertThat(header.get("userKey")).isEqualTo("test-user-key");
    }
}
