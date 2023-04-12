package com.jimi.interceptor;

import com.jimi.common.ApiResult;
import com.jimi.entity.Account;
import com.jimi.utils.UserUtils;
import com.jimi.utils.jwt.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class LoginInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    /*
    * 在请求处理之前进行调用(Controller方法调用之前)
    * 若返回true请求将会继续执行后面的操作
    * */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        // 如果不是映射到方法不拦截 直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        //验证token
        if (null == token || "".equals(token) || !TokenUtils.verify(token)) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            try (PrintWriter writer = response.getWriter()) {
                writer.print(ApiResult.error("登录验证失败"));
            } catch (Exception e) {
                logger.error("login token error is {}", e.getMessage());
            }
            return false;
        }
        //若token验证成功，把用户信息存储在ThreadLocal
        Account account = TokenUtils.getUserByToken(token);
        UserUtils.setLoginUser(account);
        return true;
    }
    
    /***
     * 请求处理之后进行调用，但是在视图被渲染之前（Controller方法调用之后）
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        logger.info("执行了拦截器的postHandle方法");
    }
    
    /***
     * 整个请求结束之后被调用，也就是在DispatchServlet渲染了对应的视图之后执行（主要用于进行资源清理工作）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清除线程变量
        UserUtils.removeUser();
    }

}
