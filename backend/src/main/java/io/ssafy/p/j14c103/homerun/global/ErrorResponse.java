package io.ssafy.p.j14c103.homerun.global;

public record ErrorResponse(
    String code,
    String message
) {

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(
            errorCode.getCode(),
            errorCode.getMessage()
        );
    }

    public static ErrorResponse of(
        String code,
        String message
    ) {
        return new ErrorResponse(code, message);
    }
}
