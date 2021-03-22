package com.kakaopay.coupon.biz.coupon.service;

import com.kakaopay.coupon.biz.coupon.entity.Coupon;

import java.time.LocalDate;
import java.util.List;

public interface UserCouponService {

    public List<Coupon> searchCoupone(Long userId);
    public List<Coupon> getExpiredCoupon(Long userId, LocalDate startDate, LocalDate endDate);
    public Coupon issueCoupone(Long userId);
    public void cancelCoupone(Long userId, String code);

}
