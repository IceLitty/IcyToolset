package com.gmail.litalways.toolset.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author IceRain
 * @since 2024/8/1
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProxyException extends Exception {

    private String message;
    private Throwable cause;

    public ProxyException(String message) {
        super(message);
    }

    public ProxyException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.cause = cause;
    }

}
