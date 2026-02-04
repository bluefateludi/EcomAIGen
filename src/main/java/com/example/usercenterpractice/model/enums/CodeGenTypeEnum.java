package com.example.usercenterpractice.model.enums;

import cn.hutool.core.util.ObjUtil;
import com.example.usercenterpractice.ai.model.HtmlCodeResult;
import lombok.Getter;

import javax.swing.text.html.HTML;

@Getter
public enum CodeGenTypeEnum {

    HTML("原生HTML模式","html"),
    MULTI_FILE("原生多文件模式","multi_file");

private  final String text;
private  final String value;
CodeGenTypeEnum(String text, String value){
    this.text = text;
    this.value = value;
}
/**
 * 根据value获取枚举
 * @param value 枚举值的value
 * @ return 枚举值
 */
public static CodeGenTypeEnum getEnumByValue(String value){
    if(ObjUtil.isEmpty(value)){
        return null;
    }
    for(CodeGenTypeEnum anEnum: CodeGenTypeEnum.values()){
        if(anEnum.value.equals(value)){
            return anEnum;
        }
    }
    return null;
}
}
