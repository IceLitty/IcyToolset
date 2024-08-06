package com.gmail.litalways.toolset.exception;

/**
 * @author IceRain
 * @since 2024/8/6
 */
public class UserCancelActionException extends Exception {

    public UserCancelActionException() {
    }

    public UserCancelActionException(String message) {
        super(message);
    }

    public UserCancelActionException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserCancelActionException(Throwable cause) {
        super(cause);
    }

    public UserCancelActionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
