package com.jimi.controller;

import com.jimi.common.ApiResult;
import com.jimi.entity.Account;
import com.jimi.enums.CustomExceptionType;
import com.jimi.exception.CustomException;
import com.jimi.utils.MD5Utils;
import com.jimi.utils.jwt.TokenUtils;
import com.jimi.utils.staticvar.StaticKeys;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@RequestMapping(value = "/home")
@Slf4j
public class LoginController {
    @Value("${login.username}")
    private String username;

    @Value("${login.password}")
    private String password;

//    /**
//     * 转向到登录页面
//     *
//     * @param model
//     * @param request
//     * @return
//     */
//    @RequestMapping("toLogin")
//    public String toLogin(Model model, HttpServletRequest request) {
//        return "login/login";
//    }

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


//    /**
//     * 管理员登录验证
//     *
//     * @param model
//     * @param request
//     * @return
//     */
    /*
    @RequestMapping(value = "login")
    public String login(Model model, HttpServletRequest request) {
        String userName = request.getParameter("userName");
        String passwd = request.getParameter("md5pwd");
//        String code = request.getParameter(StaticKeys.SESSION_CODE);
        HttpSession session = request.getSession();
        try {
            if (!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(passwd)) {
                Account account = new Account();
                if (MD5Utils.GetMD5Code(password).equals(passwd) && username.equals(userName)) {
                    account.setAccount(password);
                    account.setPassword(username);
                    request.getSession().setAttribute(StaticKeys.LOGIN_KEY, account);
                    return "index";
                }
            }
        } catch (Exception e) {
            log.error("登录异常：", e);
        }
        model.addAttribute("error", "帐号或者密码错误");
        return "login/login";
    }
    */

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
        return ApiResult.error();
    }


}