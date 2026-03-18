package io.ssafy.p.j14c103.homerun.domain.character;

public enum HealthRisk {
    STABLE(false, false),
    NEGOTIATION_PENALTY(false, false),
    MEDICAL_BILL_CANDIDATE(false, false),
    HOSPITALIZATION_CANDIDATE(true, false),
    FORCED_RESIGNATION_CANDIDATE(false, true);

    private final boolean hospitalizationCandidate;
    private final boolean forcedResignationCandidate;

    HealthRisk(
        final boolean hospitalizationCandidate,
        final boolean forcedResignationCandidate
    ) {
        this.hospitalizationCandidate = hospitalizationCandidate;
        this.forcedResignationCandidate = forcedResignationCandidate;
    }

    public boolean isHospitalizationCandidate() {
        return hospitalizationCandidate;
    }

    public boolean isForcedResignationCandidate() {
        return forcedResignationCandidate;
    }
}
