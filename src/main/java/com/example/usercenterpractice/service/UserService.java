package com.example.usercenterpractice.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.usercenterpractice.model.domain.User;
import com.example.usercenterpractice.model.dto.user.UserLoginRequest;
import com.example.usercenterpractice.model.dto.user.UserQueryRequest;
import com.example.usercenterpractice.model.dto.user.UserRegisterRequest;
import com.example.usercenterpractice.model.vo.LoginUserVO;
import com.example.usercenterpractice.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 86150
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2026-01-30 12:00:46
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
    Long userRegister(UserRegisterRequest userRegisterRequest);
    /**
     * 用户登录
     * @param userLoginRequest
     * @return
     */
    LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);
    /**
     * 获取脱敏的已登录用户信息
     * @param  user
     * @return
     */
    LoginUserVO getLoginUserVO(User  user);
    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);
    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);
/**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    public UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息列表
     *
     * @param userList
     * @return
     */
    public List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);


    /**
     * 生成加密密码
     *
     * @param userPassword
     * @return
     */
    public String getEncryptPassword(String userPassword);


}
