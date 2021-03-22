package com.kakaopay.coupon.biz.coupon.Task;

import com.kakaopay.coupon.biz.constant.CouponStatus;
import com.kakaopay.coupon.biz.coupon.entity.Coupon;
import com.kakaopay.coupon.biz.coupon.repository.CouponRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CouponSaveTask implements Runnable{

    CouponRepository couponRepository = null;

    ArrayList<String> couponCSVInfoList = null;

    @Override
    public void run() {
        List<Coupon> couponList = couponCSVInfoList.stream().map(s->{
            String[] strArr = s.split(",");
            return new Coupon(strArr[0], CouponStatus.NOTYETUSED, LocalDate.parse(strArr[1]) );
        }).collect(Collectors.toList());

        couponRepository.saveAll(couponList);
        System.out.println("쿠폰 저장 : " + couponList.size() + ", " + Thread.currentThread().getName());
    }
}
