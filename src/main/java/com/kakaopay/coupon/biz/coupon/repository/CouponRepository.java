package com.kakaopay.coupon.biz.coupon.repository;

import com.kakaopay.coupon.biz.constant.CouponStatus;
import com.kakaopay.coupon.biz.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Coupon findFirstByCode(String code);
    Coupon findFirstByStatusAndExpiredDateAfter(CouponStatus couponStatus, LocalDate localDate);
    List<Coupon> findByIdIn(List<Long> codeIdList);
}
