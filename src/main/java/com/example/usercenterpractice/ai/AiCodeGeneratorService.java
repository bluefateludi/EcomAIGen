package com.example.usercenterpractice.ai;

import com.example.usercenterpractice.ai.model.HtmlCodeResult;
import com.example.usercenterpractice.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {



/**
 * 生成HTML代码
 *
 * @param userMessage 用户消息
 * @return 生成代码结果
 */
@SystemMessage(fromResource = "Prompt/codegen-html-system-prompt.txt")
HtmlCodeResult generateHtmlCode(@MemoryId Long memoryId, @UserMessage String userMessage);




    /**
 * 生成多文件代码
 *
 * @param userMessage 用户消息
 * @return  生成的代码结果
 */
@SystemMessage(fromResource = "Prompt/codegen-multi-file-system-prompt.txt")
MultiFileCodeResult generateMultiFileCode(String userMessage);

/**
 * 生成html代码(流式)
 * @param userMessage 用户输入
 * @return 生成的代码结果
 */
@SystemMessage(fromResource = "Prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);
    /**
     * 生成多文件代码(流式)
     * @param userMessage 用户输入
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "Prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);


    /**
     * 生成 Vue 项目代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成过程的流式响应
     */
    @SystemMessage(fromResource = "Prompt/codegen-vue-system-prompt.txt")
    Flux<String> generateVueProjectCodeStream(@MemoryId long appId, @UserMessage String userMessage);

}
