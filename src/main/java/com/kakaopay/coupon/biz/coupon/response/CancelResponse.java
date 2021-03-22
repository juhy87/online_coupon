package com.kakaopay.coupon.biz.coupon.response;

import com.kakaopay.coupon.biz.constant.ResponseStatus;
import lombok.Getter;

@Getter
public class CancelResponse {
    private final ResponseStatus result;

    public CancelResponse(ResponseStatus result) {
        this.result = result;
    }
}
