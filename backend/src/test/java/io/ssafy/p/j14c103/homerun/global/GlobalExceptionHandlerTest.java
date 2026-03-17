package io.ssafy.p.j14c103.homerun.global;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @DisplayName("IllegalArgumentException은 400 응답으로 변환한다.")
    @Test
    void handleIllegalArgumentException() {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("seedType은 비어 있을 수 없습니다.");

        // when
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(
            exception
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE.getCode());
        assertThat(response.getBody().message()).isEqualTo("seedType은 비어 있을 수 없습니다.");
    }
}
