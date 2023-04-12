package com.gmail.litalways.toolset.state;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.util.xmlb.annotations.Transient;
import lombok.Data;

/**
 * 单个脚本文件
 *
 * @author IceRain
 * @since 2023/04/11
 */
@Data
public class ScriptFile implements Cloneable {

    private String fileName;
    private String fileSuffix;
    private String script;

    public FileType getFileType() {
        return FileTypeManager.getInstance().getFileTypeByExtension(this.fileSuffix);
    }

    @Override
    @SuppressWarnings("UnnecessaryLocalVariable")
    public ScriptFile clone() {
        try {
            ScriptFile clone = (ScriptFile) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
