package com.example.usercenterpractice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenterpractice.constant.UserConstants;
import com.example.usercenterpractice.exception.BusinessException;
import com.example.usercenterpractice.exception.ErrorCode;
import com.example.usercenterpractice.exception.ThrowUtils;
import com.example.usercenterpractice.mapper.UserMapper;
import com.example.usercenterpractice.model.domain.User;
import com.example.usercenterpractice.model.dto.user.UserLoginRequest;
import com.example.usercenterpractice.model.dto.user.UserQueryRequest;
import com.example.usercenterpractice.model.dto.user.UserRegisterRequest;
import com.example.usercenterpractice.model.vo.LoginUserVO;
import com.example.usercenterpractice.model.vo.UserVO;
import com.example.usercenterpractice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 86150
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2026-01-30 12:00:46
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Override
    public Long userRegister(UserRegisterRequest userRegisterRequest) {
        // 获取请求参数
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 非空校验
        ThrowUtils.throwIf(
                StrUtil.isBlank(userAccount) || StrUtil.isBlank(userPassword) || StrUtil.isBlank(checkPassword),
                ErrorCode.PARAMS_ERROR);
        // 长度校验
        ThrowUtils.throwIf(userAccount.length() < 4 || userPassword.length() < 8 || checkPassword.length() < 8,
                ErrorCode.PARAMS_ERROR, "用户账号或密码过短");
        // 检验账号是否包含特殊字符
        ThrowUtils.throwIf(userAccount.matches(UserConstants.SPECIAL_CHARACTERS_REGEX), ErrorCode.PARAMS_ERROR,
                "账号不能包含特殊字符");
        ThrowUtils.throwIf(userPassword.matches(UserConstants.SPECIAL_CHARACTERS_REGEX), ErrorCode.PARAMS_ERROR,
                "密码不能包含特殊字符");
        // 判断两次密码是否相同
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次密码不一致");
        // 检测账号是否已经存在
        long count = this.count(new LambdaQueryWrapper<User>().eq(User::getUserAccount, userAccount));
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号已存在");
        /// 密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(userAccount); // 默认使用账号作为昵称
        user.setUserRole(UserConstants.DEFAULT_ROLE);
        boolean save = this.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "注册失败");
        return user.getId();
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 1.获取请求参数，并且检验
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        // 非空校验
        ThrowUtils.throwIf(StrUtil.isBlank(userAccount) || StrUtil.isBlank(userPassword), ErrorCode.PARAMS_ERROR);
        // 检验账号是否包含特殊字符
        ThrowUtils.throwIf(userAccount.matches(UserConstants.SPECIAL_CHARACTERS_REGEX), ErrorCode.PARAMS_ERROR,
                "账号不能包含特殊字符");
        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(UserConstants.USER_LOGIN_STATE, user);
        // 4. 获得脱敏后的用户信息
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstants.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstants.USER_LOGIN_STATE);
        ThrowUtils.throwIf(userObj == null, ErrorCode.OPERATION_ERROR, "未登录");
        // 移除登录态
        request.getSession().removeAttribute(UserConstants.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        QueryWrapper<User> wrapper = new QueryWrapper<User>()
                .eq(id != null && id > 0, "id", id)
                .eq(StrUtil.isNotBlank(userRole), "userRole", userRole)
                .like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount)
                .like(StrUtil.isNotBlank(userName), "userName", userName)
                .like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);

        // 处理排序
        if (StrUtil.isNotBlank(sortField)) {
            if ("ascend".equals(sortOrder)) {
                wrapper.orderByAsc(sortField);
            } else {
                wrapper.orderByDesc(sortField);
            }
        }

        return wrapper;

    }

    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        return DigestUtils.md5DigestAsHex((UserConstants.SALT + userPassword).getBytes());
    }

}
