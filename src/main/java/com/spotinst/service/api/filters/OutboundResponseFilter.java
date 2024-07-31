package com.spotinst.service.api.filters;

import com.spotinst.commons.errors.ErrorCodesCommon;
import com.spotinst.commons.errors.bl.BlError;
import com.spotinst.commons.errors.bl.BlValidationError;
import com.spotinst.commons.response.ResponseStatus;
import com.spotinst.commons.response.api.*;
import com.spotinst.commons.response.api.items.IServiceResponseItem;
import com.spotinst.commons.response.api.items.ServiceOkInnerResponse;
import com.spotinst.commons.response.api.items.ServiceResponseItems;
import com.spotinst.dropwizard.common.constants.ServiceConstants;
import com.spotinst.dropwizard.common.context.RequestsContextManager;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.jersey.validation.ValidationErrorMessage;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.message.internal.OutboundMessageContext;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Stas Radchenko
 * @since 12/30/20
 */
@Priority(ServiceConstants.Filters.PRIORITY_OUTBOUND_RESPONSE_FILTER)
public class OutboundResponseFilter implements ContainerResponseFilter {

    private static final Logger LOGGER                                          =
            LoggerFactory.getLogger(OutboundResponseFilter.class);
    private static final String UNHANDLED_CLIENT_ERROR_MESSAGE                  = "An unknown error occurred";
    private static final String VALIDATION_MESSAGE_SPLITTER                     = " ";
    private static final String VALIDATION_MESSAGE_DOUBLE_WHITESPACE_TO_REPLACE = "  ";
    private static final String VALIDATION_MESSAGE_FIELD_NAME_VALUE_TO_REMOVE   = ".object";
    private static final String VALIDATION_MESSAGE_BODY_FIELD_NAME_FORMAT       = "body:%s";
    private static final String VALIDATION_MESSAGE_QUERY_FIELD_NAME_FORMAT      = "query:%s";
    private static final String VALIDATION_MESSAGE_ERROR_MESSAGE_FORMAT         = "%s %s";
    private static final String VALIDATION_MESSAGE_EMPTY_FIELD_NAME              = "body:";
    private static final String VALIDATION_MESSAGE_GENERAL_BODY_ERROR_FIELD_NAME = "body";
    private static final String GENERAL_BODY_VALIDATION_MESSAGE                  = "The request body";
    private static final String QUERY_PARAM_VALIDATION_MESSAGE                   = "query param";
    private static final String SWAGGER                                          = "swagger";

    private boolean     logResponses;
    private Set<String> excludedPaths;

    /**
     * Adds extra headers to increase security, see {@link this#setFrameguardHeader}, {@link this#setIeNoOpenHeader}
     * and {@link this#setCorsHeaders} to see which headers it adds
     */
    private boolean useWebSecurityHeaders;

    /**
     * Sets which http error codes this filter can return,
     * when null or empty - all http error codes are allowed
     * <p>
     * If an http error code is not allowed we'll return a BAD_REQUEST instead
     */
    private Set<HttpStatus.Code> whitelistHttpErrorCodes;

    /**
     * Mapper between http error codes received/generated to the ones we want to return instead
     * i.e: bean validation generate http error code 422(Unprocessable Entity) but we want to
     * return 400(Bad Request) instead
     *
     * @author Caduri Katzav
     * @since 1.2.0, 4/7/19
     */
    private Map<Integer, Integer> errorCodesToMask;

    /**
     * List of Spoinst error codes we want to replace with General Error instead
     *
     * @author Caduri Katzav
     * @since 1.2.0, 4/7/19
     */
    private List<String> maskedSpotinstErrors;

    public OutboundResponseFilter() {
    }

    /**
     * Initialize all private members by input from user
     * to initialize whitelistHttpErrorCodes we use the mapping between http error description to code
     * to convert the input to codes, same as for errorCodesToMask
     *
     * @param excludedPaths                      which paths we don't want this filter to run on
     * @param logResponses                       whether we want to log the response object
     * @param useWebSecurityHeaders              whether we should add extra security headers to the response
     * @param whitelistHttpErrorCodeDescriptions list of whitelist http error code description
     * @param errorCodesToMask                   list of error codes with the error code we want to replace them with in the response
     * @param maskedSpotinstErrors               list of spotinst error codes we want to mask
     */
    private OutboundResponseFilter(Set<String> excludedPaths, boolean logResponses, boolean useWebSecurityHeaders,
                                   Set<HttpStatus.Code> whitelistHttpErrorCodeDescriptions,
                                   Map<Integer, Integer> errorCodesToMask, List<String> maskedSpotinstErrors) {
        this.excludedPaths = excludedPaths;
        this.logResponses = logResponses;
        this.useWebSecurityHeaders = useWebSecurityHeaders;
        this.maskedSpotinstErrors = maskedSpotinstErrors;
        this.whitelistHttpErrorCodes = whitelistHttpErrorCodeDescriptions;
        this.errorCodesToMask = errorCodesToMask;
    }

    /**
     * Use Builder to create instance of OutboundResponseFilter
     *
     * @return a class that helps create an instance of OutboundResponseFilter
     */
    public static OutboundResponseFilter.Builder builder() {
        return new OutboundResponseFilter.Builder();
    }

    protected void preFilter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    }

    protected void postFilter() {
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        preFilter(requestContext, responseContext);

        if (requestContext.getUriInfo().getPath().contains(SWAGGER) == false) {
            buildResponse(requestContext, responseContext);
        }

        if (logResponses) {
            logResponse(requestContext, responseContext);
        }

        // Remove the current thread context
        RequestsContextManager.remove();

        postFilter();
    }

    private void buildResponse(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        if (shouldSkipResponseFilter(requestContext)) {
            return;
        }

        ServiceResponse response = new ServiceResponse();

        ServiceResponseRequestData requestData = buildRequestData(requestContext);
        response.setRequest(requestData);

        ServiceInnerResponse innerResponse;
        ResponseStatus       status;
        int                  responseStatus = responseContext.getStatus();
        String               reasonPhrase   = responseContext.getStatusInfo().getReasonPhrase();

        switch (responseStatus) {
            case HttpStatus.OK_200: {
                innerResponse = buildSuccessInnerResponse(responseContext, response);
                status = buildSuccessStatusResponse(responseStatus, reasonPhrase);
                break;
            }
            default: {
                HttpStatus.Code maskedStatusCode = getMaskedStatusCode(responseStatus);
                responseContext.setStatus(maskedStatusCode.getCode());

                innerResponse = buildFailedInnerResponse(responseContext, response, maskedStatusCode.getCode());
                status = buildFailedStatusResponse(maskedStatusCode.getCode(), maskedStatusCode.getMessage());
            }
        }

        // Set the entity(body)
        responseContext.setEntity(response);

        // Set default content type
        setContentType(responseContext);

        // Set status
        innerResponse.setStatus(status);

        // Set response time
        setResponseTime(responseContext);

        // Set request id
        setRequestId(responseContext, requestContext);

        // Set connection keep alive
        setConnectionKeepAlive(responseContext);

        // Set extra security headers
        if (this.useWebSecurityHeaders) {
            setFrameguardHeader(responseContext);
            setIeNoOpenHeader(responseContext);
            setCorsHeaders(responseContext);
        }
    }

    /**
     * Some error codes we want to masked because we don't want them to return to the user, i.e: 422
     * Here we first try to mask and then return the code converted to {@link HttpStatus.Code}
     *
     * @param responseStatus the response status code
     * @return the masked code or the original code as HttpStatus.Code
     */
    private HttpStatus.Code getMaskedStatusCode(int responseStatus) {
        HttpStatus.Code retVal = HttpStatus.getCode(responseStatus);

        if (retVal != null && errorCodesToMask != null && errorCodesToMask.containsKey(responseStatus)) {
            Integer newResponseStatus = errorCodesToMask.get(responseStatus);

            if (newResponseStatus != null) {
                retVal = HttpStatus.getCode(newResponseStatus);
            }
        }

        if (retVal == null) {
            retVal = HttpStatus.Code.BAD_REQUEST;
        }

        return retVal;
    }

    /**
     * Populates the status object by response's http error code and whether the error code is whitelisted or not
     *
     * @param responseStatus the response's http status code
     * @param reasonPhrase   the response's http status code description
     * @return the response status object populates with valid http error code to return
     * @author Caduri Katzav
     * @since 1.2.0, 4/7/19
     */
    private ResponseStatus buildFailedStatusResponse(int responseStatus, String reasonPhrase) {
        ResponseStatus retVal = new ResponseStatus();
        retVal.setMessage(reasonPhrase);
        retVal.setCode(responseStatus);

        if (errorCodesToMask != null && errorCodesToMask.containsKey(responseStatus)) {
            responseStatus = errorCodesToMask.get(responseStatus);
            HttpStatus.Code codeToReturn = HttpStatus.getCode(responseStatus);

            if (codeToReturn != null) {
                retVal.setCode(responseStatus);
                retVal.setMessage(codeToReturn.getMessage());
            }
        }

        if (isHttpErrorCodeWhitelisted(responseStatus) == false) {
            retVal.setCode(HttpStatus.BAD_REQUEST_400);
            retVal.setMessage(HttpStatus.Code.BAD_REQUEST.getMessage());
        }

        return retVal;
    }

    private ResponseStatus buildSuccessStatusResponse(int responseStatus, String reasonPhrase) {
        ResponseStatus retVal = new ResponseStatus();
        retVal.setCode(responseStatus);
        retVal.setMessage(reasonPhrase);

        return retVal;
    }

    private void logResponse(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        ContainerRequest request = (ContainerRequest) requestContext;
        String           path    = request.getRequestUri().getPath();
        String           query   = request.getRequestUri().getQuery();
        String           method  = requestContext.getMethod();

        String queryStr     = query != null ? query : "";
        int    statusCode   = responseContext.getStatusInfo().getStatusCode();
        String reasonPhrase = responseContext.getStatusInfo().getReasonPhrase();

        long rttInMs = -1;
        // Make sure context is valid since request can fail before lazy initialized using @ManagedAsync
        if (RequestsContextManager.getContext() != null &&
            RequestsContextManager.getContext().getRequestStartTimeInMs() != null) {
            Long requestStartTimeInMs = RequestsContextManager.getContext().getRequestStartTimeInMs();
            long end                  = System.currentTimeMillis();
            rttInMs = end - requestStartTimeInMs;
        }

        String format = "Response: %s %s %s %s %s %sms";
        String logMsg = String.format(format, method, path, queryStr, statusCode, reasonPhrase, rttInMs);

        LOGGER.info(logMsg);
    }

    private ServiceInnerResponse buildFailedInnerResponse(ContainerResponseContext responseContext,
                                                          ServiceResponse response, Integer responseStatusCode) {
        ServiceInnerResponse      innerResponse;
        ServiceErrorInnerResponse errorResponse;
        Object                    entity = responseContext.getEntity();

        if (isHttpErrorCodeWhitelisted(responseStatusCode) == false) {
            logGeneralErrorResponseMessage(responseContext);

            responseContext.setStatus(HttpStatus.BAD_REQUEST_400);

            BlError blError =
                    new BlError(ErrorCodesCommon.GENERAL_ERROR.reasonPhrase(), UNHANDLED_CLIENT_ERROR_MESSAGE);

            errorResponse = new ServiceErrorInnerResponse();
            errorResponse.addError(blError);
        }
        else if (entity instanceof ServiceErrorInnerResponse) {
            ServiceErrorInnerResponse serviceErrorInnerResponse = (ServiceErrorInnerResponse) entity;
            errorResponse = maskInnerSpotinstErrorResponse(serviceErrorInnerResponse);
        }
        else if (entity instanceof ValidationErrorMessage) {

            errorResponse = new ServiceErrorInnerResponse();

            ValidationErrorMessage validationErrorMessage = (ValidationErrorMessage) entity;

            if (validationErrorMessage.getErrors() != null) {

                List<BlError> validationErrors =
                        validationErrorMessage.getErrors().stream().map(this::toBlValidationError)
                                              .collect(Collectors.toList());
                errorResponse.addErrors(validationErrors);
            }
        }
        else if (entity instanceof BlError) {
            BlError blError = (BlError) entity;

            errorResponse = new ServiceErrorInnerResponse();
            errorResponse.addError(blError);
        }
        else if (entity instanceof ErrorMessage) {
            errorResponse = new ServiceErrorInnerResponse();

            ErrorMessage errorMessage = (ErrorMessage) entity;
            String       errCode      = errorMessage.getCode().toString();
            String       errMsg       = errorMessage.getMessage();
            BlError      blError      = new BlError(errCode, errMsg);

            errorResponse.addError(blError);
        }
        else {
            String innerErrorMessage = "Unhandled Exception! Need to investigate the request";

            logGeneralErrorResponseMessage(responseContext);

            LOGGER.error(innerErrorMessage);

            BlError blError =
                    new BlError(ErrorCodesCommon.GENERAL_ERROR.reasonPhrase(), UNHANDLED_CLIENT_ERROR_MESSAGE);

            errorResponse = new ServiceErrorInnerResponse();
            errorResponse.addError(blError);
        }

        innerResponse = errorResponse;

        response.setResponse(innerResponse);
        return innerResponse;
    }

    private BlError toBlValidationError(String validationErrorMessage) {
        BlError retVal = null;

        /*
           We have multiple options to parse the validation error message:
           - When its a general validation about the body, i.e "The request body may not be null", then return the message
             as is and set the field to "body"
           - When its a validation on query param, extract the field name from the message and create the error object,
             field name should start with "query:"
           - When its a validation on field in the body, extract the field name from the message and create the error
             object, field name should start with "body:"
         */
        if (validationErrorMessage.startsWith(GENERAL_BODY_VALIDATION_MESSAGE)) {
            retVal = new BlValidationError(ServiceConstants.REST.VALIDATION_ERROR, validationErrorMessage,
                                           VALIDATION_MESSAGE_GENERAL_BODY_ERROR_FIELD_NAME);
        }
        else if (validationErrorMessage.startsWith(QUERY_PARAM_VALIDATION_MESSAGE)) {
            validationErrorMessage =
                    StringUtils.replaceOnce(validationErrorMessage, QUERY_PARAM_VALIDATION_MESSAGE, "").trim();

            if (validationErrorMessage.contains(VALIDATION_MESSAGE_SPLITTER)) {
                String[] splitErrorMessage = validationErrorMessage.split(VALIDATION_MESSAGE_SPLITTER);
                String   fieldName         = splitErrorMessage[0];

                String validationField = String.format(VALIDATION_MESSAGE_QUERY_FIELD_NAME_FORMAT, fieldName);
                retVal = new BlValidationError(ServiceConstants.REST.VALIDATION_ERROR, validationErrorMessage,
                                               validationField);
            }
        }
        else if (validationErrorMessage.contains(VALIDATION_MESSAGE_SPLITTER)) {
            validationErrorMessage = validationErrorMessage
                    .replace(VALIDATION_MESSAGE_DOUBLE_WHITESPACE_TO_REPLACE, VALIDATION_MESSAGE_SPLITTER);
            String[] splitErrorMessage = validationErrorMessage.split(VALIDATION_MESSAGE_SPLITTER);
            String   fieldName         = splitErrorMessage[0];

            if (fieldName.endsWith(".")) {
                fieldName = fieldName + splitErrorMessage[1];
                String errorMessage = Arrays.stream(splitErrorMessage).skip(2)
                                            .collect(Collectors.joining(VALIDATION_MESSAGE_SPLITTER));

                validationErrorMessage =
                        String.format(VALIDATION_MESSAGE_ERROR_MESSAGE_FORMAT, fieldName, errorMessage);
            }

            if (fieldName.endsWith(VALIDATION_MESSAGE_FIELD_NAME_VALUE_TO_REMOVE)) {
                Integer lastIndex = fieldName.lastIndexOf(VALIDATION_MESSAGE_FIELD_NAME_VALUE_TO_REMOVE);
                fieldName = fieldName.substring(0, lastIndex);
            }

            String validationField = String.format(VALIDATION_MESSAGE_BODY_FIELD_NAME_FORMAT, fieldName);
            retVal = new BlValidationError(ServiceConstants.REST.VALIDATION_ERROR, validationErrorMessage,
                                           validationField);
        }

        // if we couldn't parse the error message return the message as is with empty field name
        if (retVal == null) {
            LOGGER.error(String.format("Failed to parse validation message '%s'", validationErrorMessage));

            retVal = new BlValidationError(ServiceConstants.REST.VALIDATION_ERROR, validationErrorMessage,
                                           VALIDATION_MESSAGE_EMPTY_FIELD_NAME);
        }

        return retVal;
    }

    /**
     * Clones service error inner response while check if we need to mask the error
     *
     * @param responseToMask the error inner response to clone
     * @return the cloned object with masked errors
     * @author Caduri Katzav
     * @since 1.2.0, 4/7/19
     */
    private ServiceErrorInnerResponse maskInnerSpotinstErrorResponse(ServiceErrorInnerResponse responseToMask) {
        ServiceErrorInnerResponse retVal = responseToMask;

        if (maskedSpotinstErrors != null && maskedSpotinstErrors.size() > 0) {
            retVal = new ServiceErrorInnerResponse();

            if (responseToMask.getErrors() != null) {

                for (BlError error : responseToMask.getErrors()) {

                    if (maskedSpotinstErrors.contains(error.getCode())) {
                        BlError blError = new BlError(ErrorCodesCommon.GENERAL_ERROR.reasonPhrase(),
                                                      UNHANDLED_CLIENT_ERROR_MESSAGE);
                        retVal.addError(blError);
                    }
                    else {
                        retVal.addError(error);
                    }
                }
            }
        }

        return retVal;
    }



    private ServiceInnerResponse buildSuccessInnerResponse(ContainerResponseContext responseContext,
                                                           ServiceResponse response) {
        ServiceInnerResponse innerResponse;
        Object               responseEntity = responseContext.getEntity();

        if (responseEntity instanceof ServiceResponseItems) {
            ServiceResponseItems responseItems = (ServiceResponseItems) responseEntity;
            String                     kind       = responseItems.getKind();
            List<IServiceResponseItem> items      = responseItems.getItems();
            Integer                    itemsCount = items != null ? items.size() : 0;

            ServiceOkInnerResponse okInnerResponse = new ServiceOkInnerResponse();
            okInnerResponse.setKind(kind);
            okInnerResponse.setItems(items);
            okInnerResponse.setCount(itemsCount);

            innerResponse = okInnerResponse;
        }
        else if (responseEntity instanceof ServiceResponseAffectedRows) {
            ServiceResponseAffectedRows responseAffectedRows = (ServiceResponseAffectedRows) responseEntity;
            String                      kind                 = responseAffectedRows.getKind();
            Integer                     affectedRows         = responseAffectedRows.getAffectedRows();

            ServiceAffectedRowsInnerResponse affectedRowsResp = new ServiceAffectedRowsInnerResponse();
            affectedRowsResp.setKind(kind);
            affectedRowsResp.setAffectedRows(affectedRows);

            innerResponse = affectedRowsResp;
        }
        else if (responseEntity instanceof ServiceResponseDelete) {
            ServiceResponseDelete serviceResponseDelete = (ServiceResponseDelete) responseEntity;
            String                kind                  = serviceResponseDelete.getKind();
            Boolean               removed               = serviceResponseDelete.getRemoved();

            ServiceRemovedInnerResponse removedResponse = new ServiceRemovedInnerResponse();

            removedResponse.setKind(kind);
            removedResponse.setRemoved(removed);

            innerResponse = removedResponse;
        }
        else {
            // Empty response
            innerResponse = new ServiceInnerResponse();
        }

        response.setResponse(innerResponse);

        return innerResponse;
    }

    private ServiceResponseRequestData buildRequestData(ContainerRequestContext requestContext) {
        ServiceResponseRequestData requestData = new ServiceResponseRequestData();

        // Attach request id to response header
        requestData.setId(requestContext.getHeaderString(ServiceConstants.Logging.HEADER_REQUEST_ID));
        requestData.setMethod(requestContext.getMethod());

        URI requestUri = ((ContainerRequest) requestContext).getRequestUri();

        // Attach query/path params
        if (requestUri.getQuery() != null) {
            String urlFormat = "%s?%s";
            requestData.setUrl(String.format(urlFormat, requestUri.getPath(), requestUri.getQuery()));
        }
        else {
            requestData.setUrl(requestUri.getPath());
        }

        // Attach current time
        requestData.setTimestamp(new Date());

        return requestData;
    }

    private void logGeneralErrorResponseMessage(ContainerResponseContext responseContext) {
        if (responseContext instanceof ContainerResponse) {
            String           uri            = "";
            ContainerRequest requestContext = ((ContainerResponse) responseContext).getRequestContext();
            if (requestContext != null) {
                URI requestUri = requestContext.getRequestUri();
                if (requestUri != null) {
                    uri = requestUri.toString();
                }
            }

            OutboundMessageContext ctxResponse = ((ContainerResponse) responseContext).getWrappedMessageContext();

            if (ctxResponse != null) {
                Object ctxObj = ctxResponse.getEntity();
                if (ctxObj != null) {
                    if (ctxObj instanceof ErrorMessage) {
                        // todo caduri - change format to json -
                        //  its already in json just check how elasticsearch eats it
                        //  we wanted to change to json in order for elasticsearch to better analyze it, do we have a grok in logstash that converts message to outer fields?
                        //  will check once we deploy the service
                        ErrorMessage errMsg = (ErrorMessage) ctxObj;
                        String format =
                                "\nService Error.\n  Code.....: %s\n  Uri......: %s\n  Message..: %s\n  Detail...: %s\n";
                        LOGGER.error(
                                String.format(format, errMsg.getCode(), uri, errMsg.getMessage(), errMsg.getDetails()));
                    }
                    else {
                        String format = "\nService Error.\n  Message..: %s\n";
                        LOGGER.error(String.format(format, ctxObj.toString()));
                    }
                }
            }
        }
    }

    /**
     * Calculate response time and add it as a header to response
     * Header name is {@link ServiceConstants.REST#HEADER_RESPONSE_TIME}
     *
     * @param responseContext the response object
     * @author Caduri Katzav
     * @since 1.2.0, 1/7/19
     */
    private void setResponseTime(ContainerResponseContext responseContext) {
        long rttInMs = 0;

        // Make sure context is valid since request can fail before lazy initialized using @ManagedAsync
        if (RequestsContextManager.getContext() != null &&
            RequestsContextManager.getContext().getRequestStartTimeInMs() != null) {
            Long requestStartTimeInMs = RequestsContextManager.getContext().getRequestStartTimeInMs();
            long end                  = System.currentTimeMillis();
            rttInMs = end - requestStartTimeInMs;
        }

        MultivaluedMap<String, Object> headers      = responseContext.getHeaders();
        String                         responseTime = String.format("%sms", rttInMs);
        headers.putSingle(ServiceConstants.REST.HEADER_RESPONSE_TIME, responseTime);
    }

    /**
     * Adds request id as a header to response
     * Header name is {@link ServiceConstants.Logging#HEADER_REQUEST_ID}
     *
     * @param responseContext the response object
     * @param requestContext  the request object
     * @author Caduri Katzav
     * @since 1.2.0, 1/7/19
     */
    private void setRequestId(ContainerResponseContext responseContext, ContainerRequestContext requestContext) {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        String requestId = requestContext.getHeaderString(ServiceConstants.Logging.HEADER_REQUEST_ID);
        headers.putSingle(ServiceConstants.Logging.HEADER_REQUEST_ID, requestId);
    }

    /**
     * Adds connection header to response - this tells the client the connection is alive and it don't need to create
     * new for each call
     *
     * @param responseContext the response object
     */
    private void setConnectionKeepAlive(ContainerResponseContext responseContext) {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        headers.putSingle(ServiceConstants.REST.HEADER_CONNECTION, ServiceConstants.REST.DEFAULT_CONNECTION);
    }

    /**
     * Adds Framguard as a security header to response
     * This header provides Clickjacking protection
     * Header name is {@link ServiceConstants.REST#HEDEAR_WEBSECURITY_FRAMEGUARD}
     *
     * @param responseContext the response object
     * @author Caduri Katzav
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Frame-Options">Framguard</a>
     * @since 1.2.0, 1/7/19
     */
    private void setFrameguardHeader(ContainerResponseContext responseContext) {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        headers.putSingle(ServiceConstants.REST.HEDEAR_WEBSECURITY_FRAMEGUARD,
                          ServiceConstants.REST.DEFAULT_FRAMEGUARD);
    }

    /**
     * Adds IeNoOpen as a security header to response
     * This disables the option to open a file directly on download in IE 8.
     * Header name is {@link ServiceConstants.REST#HEADER_WEBSECURITY_IE_NO_OPEN}
     *
     * @param responseContext the response object
     * @author Caduri Katzav
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers">IeNoOpen</a>
     * @since 1.2.0, 1/7/19
     */
    private void setIeNoOpenHeader(ContainerResponseContext responseContext) {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        headers.putSingle(ServiceConstants.REST.HEADER_WEBSECURITY_IE_NO_OPEN,
                          ServiceConstants.REST.DEFAULT_IE_NO_OPEN);
    }

    /**
     * Adds CORS security headers to response, see the provided link for further details details
     *
     * @param responseContext the response object
     * @author Caduri Katzav
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS">CORS</a>
     * @since 1.2.0, 1/7/19
     */
    private void setCorsHeaders(ContainerResponseContext responseContext) {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        headers.putSingle(ServiceConstants.REST.HEADER_ACCESS_CONTROL_ALLOW_ORIGIN,
                          ServiceConstants.REST.DEFAULT_ACCESS_CONTROL_ALLOW_ORIGIN);
        headers.putSingle(ServiceConstants.REST.HEADER_ACCESS_CONTROL_ALLOW_METHODS,
                          ServiceConstants.REST.DEFAULT_ACCESS_CONTROL_ALLOW_METHODS);
        headers.putSingle(ServiceConstants.REST.HEADER_ACCESS_CONTROL_ALLOW_HEADERS,
                          ServiceConstants.REST.DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS);
    }

    private void setContentType(ContainerResponseContext responseContext) {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        MediaType                      type    = responseContext.getMediaType();

        if (type == null ||
            type.getParameters().containsKey(ServiceConstants.REST.CONTENT_TYPE_APPLICATION_JSON) == false) {

            String contentType = String.format("%s; %s", ServiceConstants.REST.CONTENT_TYPE_APPLICATION_JSON,
                                               ServiceConstants.REST.CONTENT_TYPE_CHARSET_UTF_8);
            headers.putSingle(ServiceConstants.REST.HEADER_CONTENT_TYPE, contentType);
        }
    }

    private Boolean shouldSkipResponseFilter(ContainerRequestContext requestContext) {
        Boolean retVal = false;

        if (excludedPaths != null) {
            URI requestUri = ((ContainerRequest) requestContext).getRequestUri();

            if (requestUri != null) {
                String path = requestUri.getPath();

                if (path != null && path.isEmpty() == false) {
                    String pathLower = path.toLowerCase();
                    for (String excludedPath : excludedPaths) {
                        if (pathLower.contains(excludedPath.toLowerCase())) {
                            retVal = true;
                            break;
                        }
                    }
                }
            }
        }

        return retVal;
    }

    private Boolean isHttpErrorCodeWhitelisted(Integer responseStatus) {
        Boolean retVal;

        HttpStatus.Code httpErrorCode = HttpStatus.getCode(responseStatus);

        // http error code considered whitelisted if it answers 1 of these conditions
        // 1. we failed to cast responseStatus from Integer to HttpStatus.Code
        // 1. the error codes white list is null
        // 2. the error codes white list is empty
        // 3. the error codes white list contains the error code to check
        retVal = httpErrorCode == null || whitelistHttpErrorCodes == null || whitelistHttpErrorCodes.isEmpty() ||
                 whitelistHttpErrorCodes.contains(httpErrorCode);

        return retVal;
    }

    public static class Builder {
        private boolean     logResponses  = false;
        private Set<String> excludedPaths = new HashSet<>();

        /**
         * Adds extra headers to increase security, see {@link this#setFrameguardHeader}, {@link this#setIeNoOpenHeader}
         * and {@link this#setCorsHeaders} to see which headers it adds
         *
         * @author Caduri Katzav
         * @since 1.2.0, 4/7/19
         */
        private boolean useWebSecurityHeaders = false;

        /**
         * Sets which http error codes this filter can return,
         * when null or empty - all http error codes are allowed
         * <p>
         * If an http error code is not allowed we'll return a BAD_REQUEST instead
         *
         * @author Caduri Katzav
         * @since 1.2.0, 4/7/19
         */
        private Set<HttpStatus.Code> whitelistHttpErrorCodes = new HashSet<>();

        /**
         * Mapper between http error codes received/generated to the ones we want to return instead
         * i.e: bean validation generate http error code 422(Unprocessable Entity) but we want to
         * return 400(Bad Request) instead
         *
         * @author Caduri Katzav
         * @since 1.2.0, 4/7/19
         */
        private Map<Integer, Integer> errorCodesToMask = new HashMap<>();

        /**
         * List of Spoinst error codes we want to replace with General Error instead
         *
         * @author Caduri Katzav
         * @since 1.2.0, 4/7/19
         */
        private List<String> maskedSpotinstErrors = new LinkedList<>();

        Builder() {
        }

        public OutboundResponseFilter.Builder shouldLogResponse(boolean shouldLogResponses) {
            this.logResponses = shouldLogResponses;
            return this;
        }

        public OutboundResponseFilter.Builder addExcludedPath(String excludedPath) {
            this.excludedPaths.add(excludedPath);
            return this;
        }

        public OutboundResponseFilter.Builder addExcludedPaths(List<String> excludedPaths) {
            this.excludedPaths.addAll(excludedPaths);
            return this;
        }

        public OutboundResponseFilter.Builder shouldUseWebSecurityHeaders(boolean shouldUseWebSecurityHeaders) {
            this.useWebSecurityHeaders = shouldUseWebSecurityHeaders;
            return this;
        }

        public OutboundResponseFilter.Builder addWhitelistHttpErrorCode(Integer httpErrorCode) {
            this.whitelistHttpErrorCodes.add(HttpStatus.getCode(httpErrorCode));
            return this;
        }

        public OutboundResponseFilter.Builder addWhitelistHttpErrorCodes(List<Integer> httpErrorCodes) {
            httpErrorCodes.forEach(code -> {
                HttpStatus.Code httpErrorCode = HttpStatus.getCode(code);
                this.whitelistHttpErrorCodes.add(httpErrorCode);
            });

            return this;
        }

        public OutboundResponseFilter.Builder addErrorCodeToMask(Integer errorCodeToMask, Integer errorCodeToReturn) {
            this.errorCodesToMask.put(errorCodeToMask, errorCodeToReturn);
            return this;
        }

        public OutboundResponseFilter.Builder addErrorCodesToMask(
                Map<Integer, Integer> errorCodesToReturnByMaskedCode) {
            this.errorCodesToMask.putAll(errorCodesToReturnByMaskedCode);
            return this;
        }

        public OutboundResponseFilter.Builder addSpotinstErrorToMask(String spotinstErrorToMask) {
            this.maskedSpotinstErrors.add(spotinstErrorToMask);
            return this;
        }

        public OutboundResponseFilter.Builder addSpotinstErrorsToMask(List<String> spotinstErrorsToMask) {
            this.maskedSpotinstErrors.addAll(spotinstErrorsToMask);
            return this;
        }

        public OutboundResponseFilter build() {
            return new OutboundResponseFilter(excludedPaths, logResponses, useWebSecurityHeaders,
                                              whitelistHttpErrorCodes, errorCodesToMask, maskedSpotinstErrors);
        }
    }
}
