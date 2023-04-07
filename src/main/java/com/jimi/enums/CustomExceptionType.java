package com.jimi.enums;
/**
 * 异常类型
 * @author trjie
 */
public enum CustomExceptionType {

    USER_INPUT_EXCEPTION(400, "用户输入异常"),
    USER_CERTIFICATION_EXCEPTION(401, "用户认证失败"),
    USER_AUTHORITY_EXCEPTION(403,"用户无此权限"),
    USER_OPERATION_EXCEPTION(410, "用户操作异常"),
    DATA_OPERATION_EXCEPTION(420, "数据处理异常"),
    SYSTEM_EXCEPTION(500, "系统服务异常"),
    REDIS_EXCEPTION(501, "Redis服务异常"),
    REMOTE_SERVICE_EXCEPTION(510, "第三方服务调用异常"),
    UNKNOWN_EXCEPTION(999, "未知异常");

    CustomExceptionType(int code, String typeDes){
        this.code = code;
        this.typeDes = typeDes;
    }

    private final Integer code;

    private final String typeDes;

    public Integer getCode() {
        return this.code;
    }

    public String getTypeDes(){
        return this.typeDes;
    }
}
