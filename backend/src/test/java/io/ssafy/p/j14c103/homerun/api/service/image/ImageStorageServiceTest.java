package io.ssafy.p.j14c103.homerun.api.service.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.config.OciObjectStorageProperties;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ImageStorageServiceTest {

    private final ImageStorageService imageStorageService = new ImageStorageService(
            new OciObjectStorageProperties(
                    true,
                    "/mnt/c/key/oci_config",
                    "DEFAULT",
                    "ap-singapore-1",
                    "test-namespace",
                    "card-images",
                    null,
                    null,
                    null,
                    null,
                    null
            )
    );

    @DisplayName("objectName으로 OCI 퍼블릭 URL을 생성한다")
    @Test
    void getPublicUrl_success() {
        final String publicUrl = imageStorageService.getPublicUrl("cards/card 1.png");

        assertThat(publicUrl)
                .isEqualTo("https://objectstorage.ap-singapore-1.oraclecloud.com/n/test-namespace/b/card-images/o/cards/card%201.png");
    }

    @DisplayName("objectName이 비어 있으면 예외가 발생한다")
    @Test
    void getPublicUrl_blankObjectName_exception() {
        assertThatThrownBy(() -> imageStorageService.getPublicUrl(" "))
                .isInstanceOf(HomerunException.class)
                .hasMessage("objectName은 필수입니다.");
    }
}
