package com.kakaopay.coupon.biz.user.repository;

import com.kakaopay.coupon.biz.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByUserId(String userId);

}
