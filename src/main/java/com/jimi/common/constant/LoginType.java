package com.jimi.common.constant;

/**  
 * <p>Title: LoginType</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2020</p>  
 * <p>Company: 深圳市几米物联技术有限公司惠州分公司</p>  
 * @author trjie  
 * @date 2022年3月4日
 *
 */
public enum LoginType {
	
	PASSWORD("password"), // 密码登录
    NO_PASSWORD("no_password"); // 免密登录

    private final String code;// 状态值

    private LoginType(String code) {
        this.code = code;
    }
    public String getCode () {
        return code;
    }
}
