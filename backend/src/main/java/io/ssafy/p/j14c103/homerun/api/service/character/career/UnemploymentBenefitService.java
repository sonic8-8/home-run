package io.ssafy.p.j14c103.homerun.api.service.character.career;

import io.ssafy.p.j14c103.homerun.api.service.character.career.request.UnemploymentBenefitServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.UnemploymentBenefitServiceResponse;
import io.ssafy.p.j14c103.homerun.domain.character.career.UnemploymentBenefitPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.springframework.stereotype.Service;

@Service
public class UnemploymentBenefitService {

    private static final String BENEFIT_GRANTED_MESSAGE = "실업 급여를 지급했습니다.";
    private static final String BENEFIT_SKIPPED_MESSAGE = "실업 급여 지급 대상이 아닙니다.";

    private final UnemploymentBenefitPolicy unemploymentBenefitPolicy;

    public UnemploymentBenefitService() {
        this(new UnemploymentBenefitPolicy());
    }

    UnemploymentBenefitService(final UnemploymentBenefitPolicy unemploymentBenefitPolicy) {
        if (unemploymentBenefitPolicy == null) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }

        this.unemploymentBenefitPolicy = unemploymentBenefitPolicy;
    }

    public UnemploymentBenefitServiceResponse consume(
        final UnemploymentBenefitServiceRequest request
    ) {
        if (request == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }

        final UnemploymentBenefitPolicy.UnemploymentBenefitResult result =
            unemploymentBenefitPolicy.calculate(request.gameCareer());
        if (!result.benefitGranted()) {
            return UnemploymentBenefitServiceResponse.of(
                false,
                0,
                request.gameCareer().getRemainingUnemploymentBenefitTurns(),
                BENEFIT_SKIPPED_MESSAGE
            );
        }

        request.gameCareer().consumeUnemploymentBenefit();
        return UnemploymentBenefitServiceResponse.of(
            true,
            result.benefitAmount(),
            request.gameCareer().getRemainingUnemploymentBenefitTurns(),
            BENEFIT_GRANTED_MESSAGE
        );
    }
}
