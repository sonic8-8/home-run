package io.ssafy.p.j14c103.homerun.api.service.image;

import io.ssafy.p.j14c103.homerun.api.service.image.response.ImageDownloadResponse;
import io.ssafy.p.j14c103.homerun.client.image.ImageObjectClient;
import io.ssafy.p.j14c103.homerun.client.image.ImageObjectData;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ImageStorageServiceTest {

    @Mock
    private ImageObjectClient imageObjectClient;

    @InjectMocks
    private ImageStorageService imageStorageService;

    @DisplayName("objectName으로 이미지를 다운로드한다")
    @Test
    void download_success() {
        final byte[] content = new byte[]{1, 2, 3};

        given(imageObjectClient.download("test.jpg"))
                .willReturn(new ImageObjectData("test.jpg", "image/jpeg", 3L, content));

        final ImageDownloadResponse response = imageStorageService.download("test.jpg");

        assertThat(response.fileName()).isEqualTo("test.jpg");
        assertThat(response.contentType()).isEqualTo("image/jpeg");
        assertThat(response.contentLength()).isEqualTo(3L);
        assertThat(response.content()).containsExactly(1, 2, 3);
    }

    @DisplayName("objectName이 비어 있으면 예외가 발생한다")
    @Test
    void download_blankObjectName_exception() {
        assertThatThrownBy(() -> imageStorageService.download(" "))
                .isInstanceOf(HomerunException.class)
                .hasMessage("objectName은 필수입니다.");
    }
}
