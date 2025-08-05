package com.spotinst.metrics.commons.validators;

import com.spotinst.metrics.MetricsAppContext;
import com.spotinst.metrics.commons.configuration.ElasticIndex;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Yarden Eisenberg
 * @since 07/04/2021
 */
public class OverriddenIndexValidator implements ConstraintValidator<OverriddenIndexValidation, Object> {
    //region Constants
    private static final Logger LOGGER = LoggerFactory.getLogger(OverriddenIndexValidator.class);

    private static final String OVERRIDDEN_INDICES_PARAM           = "overriddenIndices";
    private static final String INDEX_MISMATCH_ERROR_TEMPLATE      = "{index}";
    private static final String INDEX_TYPE_MISMATCH_ERROR_TEMPLATE = "{index.list}";
    //endregion

    //region Override Methods
    @Override
    public void initialize(OverriddenIndexValidation constraintValidator) {
    }

    /**
     * @param object                     the object to validate - Valid type should be a non-comma seperated string
     * @param constraintValidatorContext context in which the constraint is evaluated
     * @return true if the object is a string and is found in elastisearch.indexPatterns.overriddenPatterns
     */
    @Override
    public boolean isValid(final Object object, ConstraintValidatorContext constraintValidatorContext) {
        boolean retVal = true;

        if (object != null) {
            if (object instanceof List) {
                setListExceptionMessage(constraintValidatorContext);
            }
            else if (object instanceof String index) {
                Set<String> allowedOverriddenPatterns =
                        MetricsAppContext.getInstance().getConfiguration().getElastic().getIndexes().stream()
                                         .map(ElasticIndex::getName).collect(Collectors.toSet());

                if (CollectionUtils.isEmpty(allowedOverriddenPatterns) ||
                    BooleanUtils.isFalse(allowedOverriddenPatterns.contains(index))) {
                    retVal = false;
                    setExceptionMessage(constraintValidatorContext, index, allowedOverriddenPatterns);
                }
            }
        }

        return retVal;
    }

    //endregion

    //region Private
    private void setListExceptionMessage(ConstraintValidatorContext constraintValidatorContext) {
        HibernateConstraintValidatorContext hibernateContext =
                constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class);
        hibernateContext.buildConstraintViolationWithTemplate(INDEX_TYPE_MISMATCH_ERROR_TEMPLATE)
                        .addConstraintViolation();
    }

    private void setExceptionMessage(ConstraintValidatorContext context, String index, Set<String> overriddenIndices) {
        HibernateConstraintValidatorContext hibernateContext =
                context.unwrap(HibernateConstraintValidatorContext.class);
        String errorMessage = String.format(
                "Index [%s]: is not a supported index format. Please use one of the allowed Index Patterns or add the pattern in the configuration: %s.",
                index, overriddenIndices);
        LOGGER.error(errorMessage);
        hibernateContext.addMessageParameter(OVERRIDDEN_INDICES_PARAM, overriddenIndices)
                        .buildConstraintViolationWithTemplate(INDEX_MISMATCH_ERROR_TEMPLATE).addConstraintViolation();
    }
    //endregion

}

