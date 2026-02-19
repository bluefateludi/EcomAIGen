package com.example.usercenterpractice.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.usercenterpractice.annotation.AuthCheck;
import com.example.usercenterpractice.common.BaseResponse;
import com.example.usercenterpractice.common.ResultUtils;
import com.example.usercenterpractice.constant.UserConstants;
import com.example.usercenterpractice.exception.BusinessException;
import com.example.usercenterpractice.exception.ErrorCode;
import com.example.usercenterpractice.exception.ThrowUtils;
import com.example.usercenterpractice.model.domain.ChatHistory;
import com.example.usercenterpractice.model.domain.User;
import com.example.usercenterpractice.model.dto.chathistory.ChatHistoryQueryRequest;
import com.example.usercenterpractice.service.ChatHistoryService;
import com.example.usercenterpractice.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@Slf4j
@RestController
@RequestMapping("/chat/history")
public class ChatHistoryController {



@Resource
private ChatHistoryService chatHistoryService;
@Resource
private UserService userService;
    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable String appId,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                              HttpServletRequest request) {
        log.info("接收到查询对话历史请求，appId={}, pageSize={}, lastCreateTime={}", appId, pageSize, lastCreateTime);
        ThrowUtils.throwIf(appId == null || appId.isEmpty(), ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        Long parsedAppId;
        try {
            parsedAppId = Long.parseLong(appId);
        } catch (NumberFormatException e) {
            log.error("应用ID格式错误，appId={}", appId);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID格式不正确");
        }
        User loginUser = userService.getLoginUser(request);
        log.info("当前登录用户: {}, userId={}", loginUser.getUserAccount(), loginUser.getId());
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(parsedAppId, pageSize, lastCreateTime, loginUser);
        log.info("查询结果: 总记录数={}, 当前页记录数={}", result.getTotal(), result.getRecords().size());
        return ResultUtils.success(result);
    }


    /**
     * 管理员分页查询所有对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        // 查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(result);
    }


}
