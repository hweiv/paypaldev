package com.jimi.controller;

import com.jimi.common.ApiResult;
import com.jimi.entity.Account;
import com.jimi.enums.CustomExceptionType;
import com.jimi.exception.CustomException;
import com.jimi.utils.MD5Utils;
import com.jimi.utils.PayalUtils;
import com.jimi.utils.jwt.TokenUtils;
import com.jimi.utils.staticvar.StaticKeys;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

/**
 * @version v2.3
 * @ClassName:LoginController.java
 * @author: http://www.wgstart.com
 * @date: 2019年11月16日
 * @Description: LoginController.java
 * @Copyright: 2017-2022 wgcloud. All rights reserved.
 */
@Controller
@RequestMapping(value = "/paypal/home")
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Value("${login.username}")
    private String username;

    @Value("${login.password}")
    private String password;

    /**
     * 登出系统
     *
     * @param request
     * @return
     */
    @RequestMapping("/logout")
    @ResponseBody
    public ApiResult loginOut(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
        return ApiResult.success();
    }

    @ApiIgnore
    @RequestMapping("/unAuth")
    public ApiResult<Void> unAuth() {
        throw new CustomException(CustomExceptionType.USER_CERTIFICATION_EXCEPTION, "用户未登录");
    }

    @ApiIgnore
    @RequestMapping("/failAuth")
    public ApiResult<Void> failAuth() {
        throw new CustomException(CustomExceptionType.USER_CERTIFICATION_EXCEPTION, "用户未登录");
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    @ResponseBody
    public ApiResult doLogin(@Valid @RequestBody Account account ) {
        if (StringUtils.equals(password, account.getPassword()) && StringUtils.equals(username, account.getAccount())) {
            // 获取jwt生成的token
            String token = TokenUtils.token(account.getAccount(), account.getPassword());
            ApiResult<String> success = ApiResult.success(token);
            return success;
        }
        return ApiResult.error(401, "登录验证失败");
    }


}