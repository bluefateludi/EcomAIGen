package com.example.usercenterpractice.model.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 账号
     */
    @NotNull(message = "用户账号不能为空")
    @Length(min = 4, max = 16, message = "用户账号长度在4~16之间")
    private String userAccount;

    /**
     * 密码
     */
    @NotNull(message = "用户密码不能为空")
    @Length(min = 8, max = 16, message = "用户密码长度在6~16之间")
    private String userPassword;
}
