package com.example.usercenterpractice.model.domain;

import cn.hutool.crypto.asymmetric.KeyType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 应用
 * 
 * @TableName app
 */
@TableName(value = "app")
@Data
public class App {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 应用名称
     */
    @TableField(value = "appName")
    private String appName;

    /**
     * 应用封面
     */
    @TableField(value = "cover")
    private String cover;

    /**
     * 应用初始化的 prompt
     */
    @TableField(value = "initPrompt")
    private String initPrompt;

    /**
     * 代码生成类型（枚举）
     */
    @TableField(value = "codeGenType")
    private String codeGenType;

    /**
     * 部署标识
     */
    @TableField(value = "deployKey")
    private String deployKey;

    /**
     * 部署时间
     */
    @TableField(value = "deployedTime")
    private Date deployedTime;

    /**
     * 优先级
     */
    @TableField(value = "priority")
    private Integer priority;

    /**
     * 创建用户id
     */
    @TableField(value = "userId")
    private Long userId;

    /**
     * 编辑时间
     */
    @TableField(value = "editTime")
    private Date editTime;

    /**
     * 创建时间
     */
    @TableField(value = "createTime")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "updateTime")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableField(value = "isDelete")
    private Integer isDelete;
}