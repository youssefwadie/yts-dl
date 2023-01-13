package com.github.youssefwadie.ytsdl.exceptions;

public class UnableToParseException extends IllegalStateException {
    public UnableToParseException(String msg) {
        super(msg);
    }
    public UnableToParseException(Throwable cause) {
        super(cause);
    }
}
