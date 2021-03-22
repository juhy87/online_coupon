package com.kakaopay.coupon.biz.user.service;

import com.kakaopay.coupon.biz.config.jwt.JwtUtil;
import com.kakaopay.coupon.biz.constant.ApiError;
import com.kakaopay.coupon.biz.exception.ApiRuntimeException;
import com.kakaopay.coupon.biz.user.entity.User;
import com.kakaopay.coupon.biz.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service("userService")
public class UserServiceImple implements UserService{

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String login(String userId, String password){

        if(!this.existUserId(userId)){
            throw new ApiRuntimeException(ApiError.ID_WRONG);
        }
        User user = this.getUser(userId);
        user = this.authenticate(user, password);
        return jwtUtil.generateToken(user);
    }

    @Override
    public User registerUser(String userId, String password){

        if(this.existUserId(userId)){
            throw new ApiRuntimeException(ApiError.ID_DUPLICATED);
        }

        return userRepository.save(User.builder()
                .userId(userId)
                .password(passwordEncoder.encode(password))
                .roles(Collections.singletonList("ROLE_USER"))
                .build());
    }

    public User authenticate(User user, String password) {

        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new ApiRuntimeException(ApiError.PASSWORD_WRONG);
        }
        return user;
    }

    @Override
    public User getUser(String userId){
        List<User> userList = userRepository.findByUserId(userId);
        if (userList != null && userList.size() > 0) {
            return userList.get(0);
        } else {
            return null;
        }
    }

    private boolean existUserId(String userId) {

        if(this.getUser(userId) == null){
            return false;
        }else{
            return true;
        }
    }
}
