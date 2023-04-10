package com.jimi.entity;

import com.jimi.common.constant.LoginType;
import org.apache.shiro.authc.UsernamePasswordToken;

/**  
 * <p>Title: CustomUserToken</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2020</p>  
 * <p>Company: 深圳市几米物联技术有限公司惠州分公司</p>  
 * @author trjie  
 * @date 2022年3月4日
 *
 */
public class CustomUserToken extends UsernamePasswordToken {
	
	private static final long serialVersionUID = 1L;
	
	private LoginType type;
	
    public CustomUserToken() {
        super();
    }
    public CustomUserToken(String username, String password, LoginType type, boolean rememberMe, String host) {
        super(username, password, rememberMe,  host);
        this.type = type;
    }
    public LoginType getType() {
        return type;
    }
    public void setType(LoginType type) {
        this.type = type;
    }
    /**
     * 免密登录
     * @param username
     */
    public CustomUserToken(String username) {
        super(username, "", false, null);
        this.type = LoginType.NO_PASSWORD;
    }

    /**
     * 账号密码登录
     * @param username
     * @param pwd
     */
    public CustomUserToken(String username, String pwd) {
        super(username, pwd, false, null);
        this.type = LoginType.PASSWORD;
    }
}
