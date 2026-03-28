package io.ssafy.p.j14c103.homerun.api.controller.game.session.request;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidCreateGameSessionRequestValidator
    implements ConstraintValidator<ValidCreateGameSessionRequest, CreateGameSessionRequest> {

    @Override
    public boolean isValid(
        final CreateGameSessionRequest request,
        final ConstraintValidatorContext context
    ) {
        if (request == null) {
            return true;
        }
        if (Boolean.TRUE.equals(request.getUseMyData())) {
            return true;
        }
        if (request.getJobType() != null) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
            .addPropertyNode("jobType")
            .addConstraintViolation();
        return false;
    }
}
