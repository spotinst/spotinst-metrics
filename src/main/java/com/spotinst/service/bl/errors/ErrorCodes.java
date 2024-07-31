package com.spotinst.service.bl.errors;

import com.spotinst.commons.errors.IErrorCode;

/**
 * Created by zachi.nachshon on 7/26/17.
 */
public enum ErrorCodes implements IErrorCode {

    FAILED_TO_GET_DUMMY("FAILED_TO_GET_DUMMY"),
    FAILED_TO_CREATE_DUMMY("FAILED_TO_CREATE_DUMMY"),
    FAILED_TO_UPDATE_DUMMY("FAILED_TO_UPDATE_DUMMY"),
    FAILED_TO_DELETE_DUMMY("FAILED_TO_DELETE_DUMMY");

    private final String reasonPhrase;

    ErrorCodes(final String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public String reasonPhrase() {
        return reasonPhrase;
    }
}
