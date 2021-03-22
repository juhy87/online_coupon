package com.kakaopay.coupon.biz.user.request;

import com.kakaopay.coupon.biz.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserRequest {

    @NotEmpty
    @NotBlank
    private String userId;

    @NotEmpty
    @NotBlank
//    @JsonIgnore
    private String password;

    public User toEntity(){
        return User.builder()
                .userId(this.userId)
                .password(this.password)
                .build();
    }

}
