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
}
