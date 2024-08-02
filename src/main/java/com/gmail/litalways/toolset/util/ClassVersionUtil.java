package com.gmail.litalways.toolset.util;

import com.intellij.pom.java.LanguageLevel;
import lombok.Getter;

/**
 * @author IceRain
 * @since 2024/8/1
 */
public class ClassVersionUtil {

    /**
     * 校验java版本是否支持该类文件
     *
     * @param jdkVersion java版本
     * @param classMajor class版本
     * @return 是/否/未知
     */
    public static Boolean checkCapability(int jdkVersion, int classMajor) {
        for (ClassVersion version : ClassVersion.values()) {
            if (version.getJavaVersion() == jdkVersion) {
                return version.getMinSupportMajor() <= classMajor && version.getMaxSupportMajor() >= classMajor;
            }
        }
        return null;
    }

    /**
     * 靠偏移量直接猜测兼容性
     */
    public static boolean guessCapability(int jdkVersion, int classMajor) {
        return jdkVersion + 44 >= classMajor;
    }

    /**
     * <a href="https://docs.oracle.com/javase/specs/jvms/se22/html/jvms-4.html">jvm spec</a> <br/>
     * JavaVersion int same as {@link LanguageLevel}
     */
    @Getter
    enum ClassVersion {
        JAVA_1_3(3, 47, 45, 47),
        JAVA_1_4(4, 48, 45, 48),
        JAVA_5(5, 49, 45, 49),
        JAVA_6(6, 50, 45, 50),
        JAVA_7(7, 51, 45, 51),
        JAVA_8(8, 52, 45, 52),
        JAVA_9(9, 53, 45, 53),
        JAVA_10(10, 54, 45, 54),
        JAVA_11(11, 55, 45, 55),
        JAVA_12(12, 56, 45, 56),
        JAVA_13(13, 57, 45, 57),
        JAVA_14(14, 58, 45, 58),
        JAVA_15(15, 59, 45, 59),
        JAVA_16(16, 60, 45, 60),
        JAVA_17(17, 61, 45, 61),
        JAVA_18(18, 62, 45, 62),
        JAVA_19(19, 63, 45, 63),
        JAVA_20(20, 64, 45, 64),
        JAVA_21(21, 65, 45, 65),
        JAVA_22(22, 66, 45, 66),
        ;
        private final int javaVersion;
        private final int majorVersion;
        private final int minSupportMajor;
        private final int maxSupportMajor;
        ClassVersion(int javaVersion, int majorVersion, int minSupportMajor, int maxSupportMajor) {
            this.javaVersion = javaVersion;
            this.majorVersion = majorVersion;
            this.minSupportMajor = minSupportMajor;
            this.maxSupportMajor = maxSupportMajor;
        }
    }

}
