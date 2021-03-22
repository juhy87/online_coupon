package com.kakaopay.coupon.biz.exception;

import com.kakaopay.coupon.biz.constant.ApiError;
import lombok.Getter;

public class ApiRuntimeException extends RuntimeException {

    @Getter
    private ApiError error;

    public ApiRuntimeException(ApiError error) {
        this(error, error.getDescription());
    }

    public ApiRuntimeException(ApiError error, String message) {
        super(message);
        this.error = error;
    }
}