package com.example.usercenterpractice.ai;

import com.example.usercenterpractice.ai.model.HtmlCodeResult;
import com.example.usercenterpractice.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;

public interface AiCodeGeneratorService {


/**
 * 生成HTML代码
 *
 * @param userMessage 用户消息
 * @return 生成代码结果
 */
@SystemMessage(fromResource = "Prompt/codegen-html-system-prompt.txt")
HtmlCodeResult generateHtmlCode(String userMessage);


/**
 * 生成多文件代码
 *
 * @param userMessage 用户消息
 * @return  生成的代码结果
 */
@SystemMessage(fromResource = "Prompt/codegen-multi-file-system-prompt.txt")
MultiFileCodeResult generateMultiFileCode(String userMessage);


}
