package com.kakaopay.coupon.biz.coupon.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kakaopay.coupon.biz.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CreateCouponRequest {

    @Min(1)
    private long count;

    @Max(9999)
    @Min(2021)
    private int year;

    @Max(12)
    @Min(1)
    private int month;

    @Max(31)
    @Min(1)
    private int day;

    @JsonIgnore
    private String type;



}
