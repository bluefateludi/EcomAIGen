package com.example.usercenterpractice.ai;

import com.example.usercenterpractice.ai.model.HtmlCodeResult;
import com.example.usercenterpractice.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;
    @Test
    void generateHtmlCode() {
         HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode( 1L,"请生成一个网站，代码不超过20行");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {

        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(  "请生成一个生日祝福网站，代码不超过50行");
        Assertions.assertNotNull(result);
    }
}