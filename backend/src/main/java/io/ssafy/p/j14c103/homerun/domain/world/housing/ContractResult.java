package io.ssafy.p.j14c103.homerun.domain.world.housing;

public enum ContractResult {
    // 치명적인 위험 없이 계약 가능한 상태
    SAFE,
    // 계약은 가능하지만 주의가 필요한 상태
    WARNING,
    // 계약을 진행하면 안 되는 실패 상태
    FAIL
}
