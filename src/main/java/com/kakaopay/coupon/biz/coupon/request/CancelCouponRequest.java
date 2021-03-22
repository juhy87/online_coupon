package com.kakaopay.coupon.biz.coupon.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CancelCouponRequest {

    @NotBlank
    private String code;



}
