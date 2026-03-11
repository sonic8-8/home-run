package io.ssafy.p.j14c103.homerun.global;

import lombok.Getter;

@Getter
public class HomerunException extends RuntimeException {

    private final ErrorCode errorCode;

    private HomerunException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public static HomerunException from(ErrorCode errorCode) {
        return new HomerunException(errorCode);
    }
}
