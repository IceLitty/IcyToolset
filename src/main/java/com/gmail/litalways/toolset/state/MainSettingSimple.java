package com.gmail.litalways.toolset.state;

import com.gmail.litalways.toolset.constant.KeyConstant;
import com.gmail.litalways.toolset.util.ConfigUtil;

/**
 * 简单配置对象
 *
 * @author IceRain
 * @since 2023/04/07
 */
public class MainSettingSimple {

    private final int language = 0;

    public int getLanguage() {
        return ConfigUtil.getInt(KeyConstant.CONFIG_SIMPLE_MAIN_LANGUAGE_KEY, this.language);
    }

    public void setLanguage(int language) {
        ConfigUtil.setInt(KeyConstant.CONFIG_SIMPLE_MAIN_LANGUAGE_KEY, language, this.language);
    }

}
