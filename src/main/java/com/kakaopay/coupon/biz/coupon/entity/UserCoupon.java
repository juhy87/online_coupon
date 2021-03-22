package com.kakaopay.coupon.biz.coupon.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Builder
@Data
@NoArgsConstructor
@Table(name="UserCoupon")
@Entity
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name="userId", nullable = false)
    private Long userId;

    @Column(name="status", nullable = false)
    private Long couponId;

    public UserCoupon(Long id, Long userId, Long couponId) {
        this.id = id;
        this.userId = userId;
        this.couponId = couponId;
    }



}
