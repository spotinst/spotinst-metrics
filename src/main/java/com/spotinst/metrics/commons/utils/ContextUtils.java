package com.spotinst.metrics.commons.utils;

import com.spotinst.dropwizard.common.context.RequestsContextManager;
import com.spotinst.dropwizard.common.exceptions.bl.BlException;
import com.spotinst.commons.errors.ErrorCodesCommon;
import com.spotinst.metrics.bl.model.BlDimensionsValuesRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spotinst.metrics.bl.model.BlMetricStatisticsRequest;

/**
 * Created by zachi.nachshon on 6/12/17.
 */
public class ContextUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextUtils.class);

    public static void setCtxAccountOnRequest(BlMetricStatisticsRequest request) {
        String accountId = extractAccountIdFromContext();
        request.setAccountId(accountId);
    }

    public static void setCtxAccountOnRequest(BlDimensionsValuesRequest request) {
        String accountId = extractAccountIdFromContext();
        request.setAccountId(accountId);
    }

//    public static void setCtxAccountOnRequest(BlPercentilesRequest request) {
//        String accountId = extractAccountIdFromContext();
//        request.setAccountId(accountId);
//    }

    private static String extractAccountIdFromContext() {
        String accountId = RequestsContextManager.getContext().getAccountId();

        if(StringUtils.isEmpty(accountId)) {
            String errMsg = "Cannot determine account id from request context";
            LOGGER.error(errMsg);
            throw new BlException(ErrorCodesCommon.GENERAL_ERROR, errMsg);
        }

        return accountId;
    }
}
