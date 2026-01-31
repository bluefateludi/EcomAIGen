package com.example.usercenterpractice.model.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserRegisterRequest {
    private static final long serialVersionUID = 3191241716373120793L;
/**
 * 用户账号
 */
@NotNull(message = "用户账号不能为空")
@Length(min = 4, max = 16, message = "用户账号长度在4~16之间")
private String userAccount;
/**
 * 用户密码
 */
@NotNull(message = "用户密码不能为空")
@Length(min = 6, max = 16, message = "用户密码长度在6~16之间")
private String userPassword;

/**
 * 校验密码
 */
@NotNull(message = "校验密码不能为空")
private String checkPassword;

}
