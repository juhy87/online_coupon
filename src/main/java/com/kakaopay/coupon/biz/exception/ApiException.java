package com.kakaopay.coupon.biz.exception;

import com.kakaopay.coupon.biz.constant.ApiError;
import lombok.Getter;

public class ApiException extends Exception {

    @Getter
    private ApiError error;

    public ApiException(ApiError error) {
        this(error, null);
    }

    public ApiException(ApiError error, String message) {
        super(message);
        this.error = error;
    }
}