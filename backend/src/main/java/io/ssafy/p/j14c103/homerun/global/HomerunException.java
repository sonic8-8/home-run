package io.ssafy.p.j14c103.homerun.global;

import lombok.Getter;

@Getter
public class HomerunException extends RuntimeException {

    private final ErrorCode errorCode;

    public HomerunException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public HomerunException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
