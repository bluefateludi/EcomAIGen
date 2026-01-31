package com.example.usercenterpractice.controller;

import com.example.usercenterpractice.common.BaseResponse;
import com.example.usercenterpractice.common.ResultUtils;
import com.example.usercenterpractice.exception.ErrorCode;
import com.example.usercenterpractice.exception.ThrowUtils;
import com.example.usercenterpractice.model.dto.UserRegisterRequest;
import com.example.usercenterpractice.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 注册结果
     */
    @PostMapping("register")
    @Valid
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        long result = userService.userRegister( userRegisterRequest);
        return ResultUtils.success(result);
    }
}
