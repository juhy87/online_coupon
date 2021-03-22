package com.kakaopay.coupon.biz.user.controller;

import com.kakaopay.coupon.biz.constant.ResponseStatus;
import com.kakaopay.coupon.biz.coupon.response.CreateResponse;
import com.kakaopay.coupon.biz.user.request.UserRequest;
import com.kakaopay.coupon.biz.user.response.AuthenticationResponse;
import com.kakaopay.coupon.biz.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class UserController {

    @Autowired
    public UserService userService;

    @PostMapping(value ="/auth")
    public Object Authentication(@RequestBody @Valid UserRequest userRequest, Errors errors) {

        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }

        String jwt = userService.login(userRequest.getUserId(), userRequest.getPassword());

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }



    @PostMapping(value ="/signin")
    public Object signIn(@RequestBody @Valid UserRequest user, Errors errors) {
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }
        userService.registerUser(user.getUserId(), user.getPassword());
        return ResponseEntity.ok(new CreateResponse(ResponseStatus.SUCCESS));
    }
}
