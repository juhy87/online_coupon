package com.kakaopay.coupon.biz.exception;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kakaopay.coupon.biz.constant.ApiError;

public class APIErrorResponse {
    private ApiError apiError;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object details;

    public APIErrorResponse(ApiError apiError, String message, Object details) {
        this.apiError = apiError;
        this.message = message;
        this.details = details;
    }

    @JsonGetter
    private String getCode() {
        return apiError.getCode();
    }

    @JsonGetter
    private String getDescription() {
        return apiError.getDescription();
    }
}
