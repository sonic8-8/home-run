package io.ssafy.p.j14c103.homerun.api.controller.game.session.request;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidCreateGameSessionRequestValidator.class)
public @interface ValidCreateGameSessionRequest {

    String message() default "{validation.game.session.create.jobType.notNull}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
