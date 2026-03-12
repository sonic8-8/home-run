package io.ssafy.p.j14c103.homerun.global;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "USER_001", "이메일은 필수입니다."),
    EMAIL_BLANK(HttpStatus.BAD_REQUEST, "USER_002", "이메일은 공백일 수 없습니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "USER_003", "이메일 형식이 올바르지 않습니다."),

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "GLOBAL_001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_002", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
