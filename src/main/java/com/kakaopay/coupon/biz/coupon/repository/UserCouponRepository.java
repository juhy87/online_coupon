package com.kakaopay.coupon.biz.coupon.repository;

import com.kakaopay.coupon.biz.coupon.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    List<UserCoupon> findByUserId(Long userId);
    boolean existsByUserIdAndAndCouponId(Long userId, Long couponId);

    @Transactional
    void deleteByUserIdAndCouponId(Long userId, Long couponId);

}
