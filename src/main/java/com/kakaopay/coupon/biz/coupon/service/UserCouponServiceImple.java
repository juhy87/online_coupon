package com.kakaopay.coupon.biz.coupon.service;

import com.kakaopay.coupon.biz.constant.ApiError;
import com.kakaopay.coupon.biz.constant.CouponStatus;
import com.kakaopay.coupon.biz.coupon.entity.Coupon;
import com.kakaopay.coupon.biz.coupon.entity.UserCoupon;
import com.kakaopay.coupon.biz.coupon.repository.CouponRepository;
import com.kakaopay.coupon.biz.coupon.repository.UserCouponRepository;
import com.kakaopay.coupon.biz.exception.ApiRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service("userCouponService")
public class UserCouponServiceImple implements UserCouponService {

    @Autowired
    UserCouponRepository userCouponRepository;

    @Autowired
    CouponRepository couponRepository;


    @Override
    public List<Coupon> searchCoupone(Long userId) {

        List<UserCoupon> userCouponList =userCouponRepository.findByUserId(userId);
        List<Long> codeIdList = userCouponList.stream().map(s->s.getCouponId()).collect(Collectors.toList());
        List<Coupon> couponList = couponRepository.findByIdIn(codeIdList);
        return couponList;
    }

    @Override
    public List<Coupon> getExpiredCoupon(Long userId, LocalDate startDate, LocalDate endDate) {
        return this.searchCoupone(userId).stream().filter(s-> s.getExpiredDate()
                .isAfter(startDate.minusDays(1)) && s.getExpiredDate().isBefore(endDate.plusDays(1))).collect(Collectors.toList());
    }


    @Override
    public Coupon issueCoupone(Long userId) {
        Coupon coupon = couponRepository.findFirstByStatusAndExpiredDateAfter(CouponStatus.NOTYETUSED, LocalDate.now().minusDays(1));
        if(coupon == null) {
            throw new ApiRuntimeException(ApiError.No_COUPON);
        }

        coupon.setStatus(CouponStatus.USED);
        coupon = couponRepository.save(coupon);

        UserCoupon userCoupon = userCouponRepository.save(UserCoupon.builder()
                .couponId(coupon.getId())
                .userId(userId).build());
        return coupon;
    }

    @Override
    public void cancelCoupone(Long userId, String code) {

        Coupon coupon = couponRepository.findFirstByCode(code);

        if(coupon == null){
            throw new ApiRuntimeException(ApiError.Wrong_COUPON_CODE);
        }else if (!userCouponRepository.existsByUserIdAndAndCouponId(userId, coupon.getId())){
            throw new ApiRuntimeException(ApiError.Not_USER_COUPON_CODE);
        }else if(coupon.getExpiredDate().isBefore(LocalDate.now())) {
            throw new ApiRuntimeException(ApiError.CAN_NOT_CACEL_BY_EXPIRED);
        }
        try{
            userCouponRepository.deleteByUserIdAndCouponId(userId, coupon.getId());
            coupon.setStatus(CouponStatus.NOTYETUSED);
            couponRepository.save(coupon);

        }catch (ValidationException e) {
            System.out.println(e);
        }

    }
}
