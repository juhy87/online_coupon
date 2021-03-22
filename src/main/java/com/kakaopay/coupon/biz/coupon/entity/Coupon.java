package com.kakaopay.coupon.biz.coupon.entity;

import com.kakaopay.coupon.biz.constant.CouponStatus;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import javax.persistence.*;
import java.time.LocalDate;

import static com.kakaopay.coupon.biz.constant.Constants.*;

@Builder
@Data
@NoArgsConstructor
@Table(name="Coupon")
@Entity
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name="code", length = COUPON_SIZE, nullable = false)
    private String code;

    @Column(name="status", nullable = false)
    private CouponStatus status;

    @Column(name="expiredDate", nullable = false)
    private LocalDate expiredDate;


    public Coupon(Long id, String code, CouponStatus status, LocalDate expiredDate) {
        this.id = id;
        this.code = code;
        this.status = status;
        this.expiredDate = expiredDate;
    }

    public Coupon(CouponStatus status, int year, int month, int day) {
        this.id = id;
        String code01 = RandomStringUtils.random(5, "0123456789abcdefghijklmnopqrstuvwxyzABCDEFHIJKLMNOPSRQTUVWXYZ");
        String code02 = RandomStringUtils.random(6, "0123456789abcdefghijklmnopqrstuvwxyzABCDEFHIJKLMNOPSRQTUVWXYZ");
        String code03 = RandomStringUtils.random(8, "0123456789abcdefghijklmnopqrstuvwxyzABCDEFHIJKLMNOPSRQTUVWXYZ");
        this.code = String.format("%s-%s-%s", code01, code02, code03);
        this.status = status;
        this.expiredDate = LocalDate.of(year, month, day);
    }

}