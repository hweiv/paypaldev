package com.jimi.utils;

import com.jimi.entity.Account;

public abstract class UserUtils {

    //线程变量，存放user实体类信息，即使是静态的与其他线程也是隔离的
    private static final ThreadLocal<Account> userThreadLocal = new ThreadLocal<>();

    //获取当前登录用户
    public static Account getLoginUser() {
        return userThreadLocal.get();
    }

    public static void setLoginUser(Account account) {
        userThreadLocal.set(account);
    }

    public static void removeUser(){
        userThreadLocal.remove();
    }

}
