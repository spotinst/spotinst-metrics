package com.spotinst.metrics.commons.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * * Validates that index query param is defined as allowed in the config files
 *
 *
 *  @author Yarden Eisenberg
 *  @since 07/04/2021
 *
 */
@Documented
@Constraint(validatedBy = {OverriddenIndexValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Repeatable(OverriddenIndexValidation.List.class)
@Retention(RUNTIME)
public @interface OverriddenIndexValidation {

    String message() default "index";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({FIELD})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        OverriddenIndexValidation[] value();
    }
}