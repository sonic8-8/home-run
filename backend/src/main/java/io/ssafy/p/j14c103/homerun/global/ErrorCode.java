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
    USER_EMAIL_DUPLICATE(HttpStatus.CONFLICT, "USER_004", "이미 가입된 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_005", "존재하지 않는 사용자입니다."),
    USER_SSAFY_CONNECTION_REQUIRED(HttpStatus.BAD_REQUEST, "USER_006", "SSAFY 연동이 필요합니다."),
    USER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "USER_007", "사용자 ID는 필수입니다."),

    AUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_001", "이메일 또는 비밀번호가 올바르지 않습니다."),
    AUTH_REFRESH_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_002", "Refresh Token이 유효하지 않습니다."),
    AUTH_REFRESH_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_003", "Refresh Token이 만료되었습니다."),
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_004", "인증이 필요합니다."),
    AUTH_REFRESH_STATE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_005", "Refresh Token 상태가 올바르지 않습니다."),

    CHARACTER_GAME_ID_INVALID(HttpStatus.BAD_REQUEST, "CHAR_001", "게임 ID가 올바르지 않습니다."),
    CHARACTER_TURN_INVALID(HttpStatus.BAD_REQUEST, "CHAR_002", "턴 값이 올바르지 않습니다."),
    CHARACTER_STAT_INVALID(HttpStatus.BAD_REQUEST, "CHAR_003", "캐릭터 스탯 값이 올바르지 않습니다."),
    CHARACTER_STATE_UNINITIALIZED(HttpStatus.INTERNAL_SERVER_ERROR, "CHAR_004", "캐릭터 상태가 초기화되지 않았습니다."),
    CHARACTER_HOUSING_TYPE_UNSUPPORTED(HttpStatus.BAD_REQUEST, "CHAR_005", "지원하지 않는 주거 유형입니다."),
    CHARACTER_SEED_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "CHAR_006", "시작 데이터 유형은 필수입니다."),
    CHARACTER_SEED_TYPE_UNSUPPORTED(HttpStatus.BAD_REQUEST, "CHAR_007", "지원하지 않는 시작 데이터 유형입니다."),
    CHARACTER_JOB_TYPE_UNSUPPORTED(HttpStatus.BAD_REQUEST, "CHAR_008", "지원하지 않는 직업 유형입니다."),
    CHARACTER_REQUEST_INVALID(HttpStatus.BAD_REQUEST, "CHAR_009", "캐릭터 요청 값이 올바르지 않습니다."),
    CHARACTER_POLICY_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "CHAR_010", "캐릭터 정책 구성이 올바르지 않습니다."),
    CHARACTER_RESPONSE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "CHAR_011", "캐릭터 응답 데이터 구성이 올바르지 않습니다."),
    CHARACTER_ACTION_TYPE_UNSUPPORTED(HttpStatus.BAD_REQUEST, "CHAR_012", "지원하지 않는 행동 타입입니다."),

    HOUSING_PROPERTY_NOT_FOUND(HttpStatus.NOT_FOUND, "HOUSING_001", "존재하지 않는 부동산 매물입니다."),
    HOUSING_REGISTRY_SAMPLE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "HOUSING_002", "등기부 샘플 데이터 구성이 올바르지 않습니다."),
    HOUSING_REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "HOUSING_003", "존재하지 않는 지역입니다."),
    HOUSING_DISTRICT_NOT_FOUND(HttpStatus.NOT_FOUND, "HOUSING_004", "존재하지 않는 구입니다."),

    WORLD_SESSION_NOT_FOUND(HttpStatus.BAD_REQUEST, "WORLD_001", "존재하지 않는 게임 세션입니다."),
    WORLD_CYCLE_STATE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "WORLD_002", "세션의 경제 사이클 상태가 올바르지 않습니다."),
    WORLD_RESULT_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "WORLD_003", "월드 결과 데이터가 올바르지 않습니다."),

    SCHEDULE_ACTION_TYPE_INVALID(HttpStatus.BAD_REQUEST, "SCHEDULE_003", "행동 유형이 올바르지 않습니다."),
    SCHEDULE_ACTION_CATALOG_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "SCHEDULE_004", "행동 카탈로그 상태가 올바르지 않습니다."),
    SCHEDULE_KNOWLEDGE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "SCHEDULE_005", "지식 스탯 값이 올바르지 않습니다."),

    IMAGE_OBJECT_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "IMAGE_001", "objectName은 필수입니다."),
    IMAGE_DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_002", "이미지 다운로드에 실패했습니다."),
    OCI_CONFIGURATION_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "OCI_001", "OCI 설정이 올바르지 않습니다."),

    CARD_DUMMY_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CARD_001", "카드 더미 데이터 적재에 실패했습니다."),
    CARD_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "CARD_002", "카드명은 필수입니다."),

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "GLOBAL_001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_002", "서버 내부 오류가 발생했습니다."),
    GLOBAL_CONFIGURATION_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_003", "서버 설정이 올바르지 않습니다."),
    GLOBAL_SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_004", "데이터 직렬화 처리 중 오류가 발생했습니다."),
    GLOBAL_EXTERNAL_RESPONSE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_005", "외부 연동 응답이 올바르지 않습니다."),

    LOAN_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "LOAN_001", "대출 신청을 찾을 수 없습니다."),
    LOAN_NOT_APPROVED(HttpStatus.BAD_REQUEST, "LOAN_002", "승인되지 않은 대출입니다."),
    LOAN_ALREADY_REPAID(HttpStatus.BAD_REQUEST, "LOAN_003", "이미 상환된 대출입니다."),
    LOAN_SSAFY_DUPLICATE(HttpStatus.BAD_REQUEST, "LOAN_004", "싸피론은 세션당 1건만 가능합니다."),
    LOAN_EXCEED_LIMIT(HttpStatus.BAD_REQUEST, "LOAN_005", "승인 한도를 초과했습니다."),
    LOAN_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "LOAN_006", "대출 상품을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
