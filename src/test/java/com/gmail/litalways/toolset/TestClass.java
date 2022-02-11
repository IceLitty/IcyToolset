package com.gmail.litalways.toolset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * @author IceRain
 * @since 2022/01/19
 */
public class TestClass {

    @Test
    @Tag("test")
    public void testVoid() {
        Assertions.assertEquals(10, Integer.sum(5, 5));
    }

}
