package com.cloud.userauth.infrastructure.oauth2.sas.config.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = LoopbackTokenEndpointValidator.class)
@Target({
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface LoopbackTokenEndpoint {

    String message() default
            "must use a loopback host and the exact /oauth2/token path";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
