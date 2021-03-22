package com.kakaopay.coupon.biz.coupon.response;

import com.kakaopay.coupon.biz.constant.ResponseStatus;
import lombok.Getter;

@Getter
public class CreateResponse {
    private final ResponseStatus result;

    public CreateResponse(ResponseStatus result) {
        this.result = result;
    }
}
