package com.kakaopay.coupon.biz.user.service;

import com.kakaopay.coupon.biz.exception.ApiRuntimeException;
import com.kakaopay.coupon.biz.user.entity.User;

public interface UserService {

    public String login(String userId, String password) throws ApiRuntimeException;
    public User registerUser(String userId, String password) throws ApiRuntimeException;
    public User getUser(String userId);
}
