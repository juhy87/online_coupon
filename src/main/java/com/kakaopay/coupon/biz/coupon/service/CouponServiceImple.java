package com.kakaopay.coupon.biz.coupon.service;

import com.kakaopay.coupon.biz.constant.CouponStatus;
import com.kakaopay.coupon.biz.coupon.entity.Coupon;
import com.kakaopay.coupon.biz.coupon.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("couponService")
public class CouponServiceImple implements CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Override
    public List<Coupon> createCoupons(long count, int year, int month, int day) {

        ArrayList<Coupon> coupons = new ArrayList<>();
        for(int i = 0; i < count; i++){
            coupons.add(new Coupon(CouponStatus.NOTYETUSED, year, month, day));
        }
        List<Coupon> couponList= couponRepository.saveAll(coupons);

        return couponList;
    }


}
