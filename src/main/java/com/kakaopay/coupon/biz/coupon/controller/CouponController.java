package com.kakaopay.coupon.biz.coupon.controller;

import com.kakaopay.coupon.biz.constant.ResponseStatus;
import com.kakaopay.coupon.biz.coupon.entity.Coupon;
import com.kakaopay.coupon.biz.coupon.request.CreateCouponRequest;
import com.kakaopay.coupon.biz.coupon.response.CancelResponse;
import com.kakaopay.coupon.biz.coupon.response.CreateResponse;
import com.kakaopay.coupon.biz.coupon.response.GetCouponResponse;
import com.kakaopay.coupon.biz.coupon.service.CouponService;
import com.kakaopay.coupon.biz.coupon.service.UserCouponService;
import com.kakaopay.coupon.biz.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CouponController {

    @Autowired
    public CouponService couponService;

    @Autowired
    public UserCouponService userCouponService;

    @PostMapping(value ="/createCoupon")
    public Object createCoupon(@RequestBody @Valid CreateCouponRequest createCouponRequest, Errors errors){

        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }

        long count = createCouponRequest.getCount();
        int year = createCouponRequest.getYear();
        int month = createCouponRequest.getMonth();
        int day = createCouponRequest.getDay();

        List<Coupon> couponList = couponService.createCoupons(count, year, month, day);

        return ResponseEntity.ok(new CreateResponse(ResponseStatus.SUCCESS));
    }

    @PostMapping(value ="/issueCoupon")
    public Object issueCoupon(Authentication authentication){

        Long userId = ((User)(authentication.getPrincipal())).getId();
        Coupon coupon = userCouponService.issueCoupone(userId);
        return ResponseEntity.ok(new GetCouponResponse(coupon));
    }

    @GetMapping(value ="/getCoupons")
    public Object getCoupons(Authentication authentication){

        Long userId = ((User)(authentication.getPrincipal())).getId();
        List<Coupon> userCouponList = userCouponService.searchCoupone(userId);
        return new ResponseEntity<List<GetCouponResponse>>(
                userCouponList.stream().map(s-> new GetCouponResponse(s)).collect(Collectors.toList())
                , HttpStatus.OK);

    }

    @GetMapping(value ="/getTodayExpiredCoupon")
    public Object gettodayExpiredCoupon(Authentication authentication){

        Long userId = ((User)(authentication.getPrincipal())).getId();
        List<Coupon> userCouponList = userCouponService.getExpiredCoupon(userId, LocalDate.now(), LocalDate.now());
        return new ResponseEntity<List<GetCouponResponse>>(
                userCouponList.stream().map(s-> new GetCouponResponse(s)).collect(Collectors.toList())
                , HttpStatus.OK);

    }

    @PostMapping(value ="/cancelCoupon")
    public Object cancelCoupon(Authentication authentication, @RequestBody @Valid Coupon cancelCouponRequest, Error error){

        Long userId = ((User)(authentication.getPrincipal())).getId();
        userCouponService.cancelCoupone(userId, cancelCouponRequest.getCode());
        return ResponseEntity.ok(new CancelResponse(ResponseStatus.SUCCESS));

    }
}
