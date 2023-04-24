package com.gmail.litalways.toolset.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BeanUtils配置项-类
 *
 * @author IceRain
 * @since 2023/04/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MainSettingsClassName {

    private String simpleClassName;
    private String qualifierClassName;
    private String methodName;

}
