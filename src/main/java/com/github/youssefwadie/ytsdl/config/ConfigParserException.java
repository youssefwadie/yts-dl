package com.github.youssefwadie.ytsdl.config;

public class ConfigParserException extends IllegalStateException {
    public ConfigParserException(String message) {
        super(message);
    }
    public ConfigParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
