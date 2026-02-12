package com.example.usercenterpractice.ai.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.usercenterpractice.ai.model.HtmlCodeResult;
import com.example.usercenterpractice.ai.model.MultiFileCodeResult;
import com.example.usercenterpractice.model.enums.CodeGenTypeEnum;

import java.io.File;

@Deprecated
public class CodeFileSaver {
    //文件保存根目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir")+"/tmp/code_output";
/**
 * 保存 htmlCodeResult
 */
public static File saveHtmlCodeResult(HtmlCodeResult result) {
    String baseDirPath=buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
    writeToFlie(baseDirPath,"index.html",result.getHtmlCode());
    return new File(baseDirPath);
}

    /**
     * 保存multipleCodeResult
     * @param result
     * @return
     */

    public static File saveMultiFileCodeResult(MultiFileCodeResult result) {
        String baseDirPath=buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeToFlie(baseDirPath,"index.html",result.getHtmlCode());
        writeToFlie(baseDirPath,"style.css",result.getCssCode());
        writeToFlie(baseDirPath,"script.js",result.getJsCode());
        return new File(baseDirPath);
    }

    /**
     * 构建唯一目录路径 tmp/code_output/bizType_雪花ID
     * @param bizType
     * @return
     */
    private static String buildUniqueDir(String bizType){
    String uniqueDirName = StrUtil.format("{}_{}",bizType ,IdUtil.getSnowflakeNextIdStr());

    String dirPath =FILE_SAVE_ROOT_DIR+ File.separator+uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }
    /*
    *写入单个文件
     */
    private static void writeToFlie(String dirPath,String filename,String content)
    {
    String filePath = dirPath+File.separator+filename;
    FileUtil.writeUtf8String(content,filePath);
    }

}
