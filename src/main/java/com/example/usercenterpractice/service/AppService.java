package com.example.usercenterpractice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.usercenterpractice.model.domain.App;
import com.example.usercenterpractice.model.domain.User;
import com.example.usercenterpractice.model.dto.app.AppQueryRequest;
import com.example.usercenterpractice.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
* @author 86150
* @description 针对表【app(应用)】的数据库操作Service
* @createDate 2026-02-09 23:41:04
*/
public interface AppService extends IService<App> {


    QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取脱敏的应用信息
     *
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 获取脱敏的应用信息列表
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     *  聊天生成代码
     *
     * @param appId
     * @param message
     * @param loginUser
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    String deployApp(Long appId, User loginUser);
}
