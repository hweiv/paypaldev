package com.jimi.exception;


import com.jimi.enums.CustomExceptionType;

/**
 * @author trjie
 */
public class CustomException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    private final Integer code;

    private final String message;

    public CustomException(CustomExceptionType exceptionType, String message){
        this.code = exceptionType.getCode();
        this.message = message;
    }

    public Integer getCode(){
        return this.code;
    }

    public String getMessage(){
        return this.message;
    }
}
