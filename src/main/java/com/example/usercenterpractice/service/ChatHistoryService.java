package com.example.usercenterpractice.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.usercenterpractice.model.domain.ChatHistory;
import com.example.usercenterpractice.model.domain.User;
import com.example.usercenterpractice.model.dto.chathistory.ChatHistoryQueryRequest;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

/**
* @author 86150
* @description 针对表【chat_history(对话历史)】的数据库操作Service
* @createDate 2026-02-11 17:23:17
*/
public interface ChatHistoryService extends IService<ChatHistory> {

    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    boolean deleteByAppId(Long appId);

    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

    /**
     * 加载对话历史到记忆中（支持控制是否跳过最新记录）
     *
     * @param appId      应用 ID
     * @param chatMemory 聊天记忆对象
     * @param maxCount   最大加载数量
     * @param skipLatest 是否跳过最新一条记录（true=跳过，false=包含）
     * @return 实际加载的数量
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount, boolean skipLatest);
}
