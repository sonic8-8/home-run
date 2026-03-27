package io.ssafy.p.j14c103.homerun.global;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
    private final Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private ListAppender<ILoggingEvent> listAppender;

    @AfterEach
    void tearDown() {
        if (listAppender != null) {
            logger.detachAppender(listAppender);
            listAppender.stop();
        }
    }

    @DisplayName("예상하지 못한 예외가 발생하면 500 ErrorResponse를 유지하고 ERROR 로그를 남긴다.")
    @Test
    void handleException() {
        // given
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        IllegalStateException exception = new IllegalStateException("boom");

        // when
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        assertThat(response.getBody().getErrors()).isEqualTo(List.of());

        assertThat(listAppender.list).hasSize(1);

        ILoggingEvent loggingEvent = listAppender.list.get(0);
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.ERROR);
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo("Unhandled exception occurred.");
        assertThat(loggingEvent.getThrowableProxy()).isNotNull();
        assertThat(loggingEvent.getThrowableProxy().getClassName()).isEqualTo(IllegalStateException.class.getName());
        assertThat(loggingEvent.getThrowableProxy().getMessage()).isEqualTo("boom");
    }
}
