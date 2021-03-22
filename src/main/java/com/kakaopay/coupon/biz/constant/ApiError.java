package com.kakaopay.coupon.biz.constant;

import lombok.Getter;

public enum ApiError {
    ERROR("E001", "오류"),
    ID_DUPLICATED("E002", "Id is duplicated."),
    ID_WRONG("E003", "There is no Id."),
    PASSWORD_WRONG("E004", "Password is Wrong."),
    No_COUPON("E005", "There is no coupon."),
    Wrong_COUPON_CODE("E006", "There is wrong coupon code."),
    Not_USER_COUPON_CODE("E007", "This is not user coupon code."),
    CAN_NOT_CACEL_BY_EXPIRED("E008", "This is expired coupon code, Can't cancel.");

    @Getter
    private final String code;

    @Getter
    private final String description;

    ApiError(String code, String description) {
        this.code = code;
        this.description = description;
    }
}