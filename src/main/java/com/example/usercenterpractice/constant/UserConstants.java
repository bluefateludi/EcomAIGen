package com.example.usercenterpractice.constant;

/**
 * 用户常量
 */
public class UserConstants {

    /**
     * 密码加密盐值
     */
    public static final String SALT = "brown" ;

    /**
     * 默认用户角色
     */
    public static final String DEFAULT_ROLE = "user";

    /**
     * 默认管理员角色
     */
    public static final String ADMIN_ROLE = "admin";

    /**
     * 特殊字符正则表达式
     */
    public static final String SPECIAL_CHARACTERS_REGEX = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";

    /**
     * 登录态
     */
    public static final String USER_LOGIN_STATE = "userLoginState";
    // 私有构造，防止实例化
    private UserConstants() {
    }
}
