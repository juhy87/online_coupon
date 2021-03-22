package com.kakaopay.coupon.biz.coupon.response;

import com.kakaopay.coupon.biz.coupon.entity.Coupon;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class GetCouponResponse {
    private String code;
    private LocalDate expiredDate;

    public GetCouponResponse(Coupon coupon) {
        this.code = coupon.getCode();
        this.expiredDate = coupon.getExpiredDate();
    }
}
