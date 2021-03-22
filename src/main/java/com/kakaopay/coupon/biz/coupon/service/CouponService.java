package com.kakaopay.coupon.biz.coupon.service;

import com.kakaopay.coupon.biz.coupon.entity.Coupon;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CouponService {

    public List<Coupon> createCoupons(long count, int year, int month, int day);
    public void createCoupons(MultipartFile file);


}
