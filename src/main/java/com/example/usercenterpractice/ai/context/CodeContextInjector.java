package com.example.usercenterpractice.ai.context;

import com.example.usercenterpractice.constant.AppConstant;
import com.example.usercenterpractice.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 代码上下文注入服务
 * 用于在编辑模式下将已生成的代码注入到 AI 上下文中
 *
 * @author 86150
 */
@Slf4j
@Service
public class CodeContextInjector {

    /**
     * 为编辑模式注入已生成的代码上下文
     *
     * @param appId        应用 ID
     * @param codeGenType  代码生成类型
     * @param userMessage  用户原始消息
     * @param editMode     是否为编辑模式
     * @return 增强后的消息（编辑模式下包含现有代码上下文）
     */
    public String injectExistingCode(Long appId, CodeGenTypeEnum codeGenType,
                                      String userMessage, boolean editMode) {
        if (!editMode) {
            return userMessage;
        }

        try {
            String existingCode = readExistingCode(appId, codeGenType);
            if (existingCode == null || existingCode.isEmpty()) {
                // 如果没有现有代码，返回原始消息（可能首次生成或代码已删除）
                log.info("appId: {} 没有找到现有代码，将以全量生成模式处理", appId);
                return userMessage;
            }

            // 构建带上下文的消息
            return buildEditMessage(userMessage, existingCode, codeGenType);
        } catch (Exception e) {
            log.error("读取现有代码失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 失败时返回原始消息，回退到全量生成模式
            return userMessage;
        }
    }

    /**
     * 读取已生成的代码
     */
    private String readExistingCode(Long appId, CodeGenTypeEnum codeGenType) throws IOException {
        String sourceDirName = codeGenType.getValue() + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;

        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            return null;
        }

        return switch (codeGenType) {
            case HTML -> readHtmlCode(sourceDir);
            case MULTI_FILE -> readMultiFileCode(sourceDir);
            case VUE_PROJECT -> readVueProjectCode(sourceDir);
            default -> null;
        };
    }

    /**
     * 读取 HTML 代码
     */
    private String readHtmlCode(File sourceDir) throws IOException {
        File indexHtml = new File(sourceDir, "index.html");
        if (!indexHtml.exists()) {
            return null;
        }
        return Files.readString(indexHtml.toPath());
    }

    /**
     * 读取多文件代码（读取所有主要文件）
     */
    private String readMultiFileCode(File sourceDir) throws IOException {
        StringBuilder codeBuilder = new StringBuilder();

        // 读取 index.html
        File indexHtml = new File(sourceDir, "index.html");
        if (indexHtml.exists()) {
            codeBuilder.append("【index.html】\n");
            codeBuilder.append(Files.readString(indexHtml.toPath())).append("\n\n");
        }

        // 读取 style.css
        File styleCss = new File(sourceDir, "style.css");
        if (styleCss.exists()) {
            codeBuilder.append("【style.css】\n");
            codeBuilder.append(Files.readString(styleCss.toPath())).append("\n\n");
        }

        // 读取 script.js
        File scriptJs = new File(sourceDir, "script.js");
        if (scriptJs.exists()) {
            codeBuilder.append("【script.js】\n");
            codeBuilder.append(Files.readString(scriptJs.toPath())).append("\n\n");
        }

        return codeBuilder.toString();
    }

    /**
     * 读取 Vue 项目代码（读取关键文件）
     */
    private String readVueProjectCode(File sourceDir) throws IOException {
        StringBuilder codeBuilder = new StringBuilder();

        // 读取 src/App.vue
        File appVue = new File(sourceDir, "src/App.vue");
        if (appVue.exists()) {
            codeBuilder.append("【src/App.vue】\n");
            codeBuilder.append(Files.readString(appVue.toPath())).append("\n\n");
        }

        // 读取 src/main.js
        File mainJs = new File(sourceDir, "src/main.js");
        if (mainJs.exists()) {
            codeBuilder.append("【src/main.js】\n");
            codeBuilder.append(Files.readString(mainJs.toPath())).append("\n\n");
        }

        // 读取 src/router/index.js
        File routerIndex = new File(sourceDir, "src/router/index.js");
        if (routerIndex.exists()) {
            codeBuilder.append("【src/router/index.js】\n");
            codeBuilder.append(Files.readString(routerIndex.toPath())).append("\n\n");
        }

        return codeBuilder.toString();
    }

    /**
     * 构建编辑模式的消息
     */
    private String buildEditMessage(String userMessage, String existingCode, CodeGenTypeEnum codeGenType) {
        // 限制代码长度，避免超过 token 限制
        String codeToInclude = existingCode;
        int maxLength = 8000;  // 限制代码长度
        if (existingCode.length() > maxLength) {
            codeToInclude = existingCode.substring(0, maxLength) + "\n\n...(代码过长，已截断)";
        }

        return """
                【当前已生成的代码】

                %s

                【用户修改请求】

                %s

                请根据用户请求进行增量修改：
                1. 只修改用户要求的部分
                2. 保持其他部分不变
                3. 输出完整的代码
                """.formatted(codeToInclude, userMessage);
    }

    /**
     * 检查应用是否有已生成的代码
     */
    public boolean hasExistingCode(Long appId, CodeGenTypeEnum codeGenType) {
        String sourceDirName = codeGenType.getValue() + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        File sourceDir = new File(sourceDirPath);
        return sourceDir.exists() && sourceDir.isDirectory();
    }
}
