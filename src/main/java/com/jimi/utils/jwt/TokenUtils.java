package com.jimi.utils.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jimi.entity.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * @desc   使用token验证用户是否登录
 * @author zm
 **/
@Component
@Slf4j
public class TokenUtils {
    //设置过期时间
    private static final long EXPIRE_DATE=1000*60*60*1; // 60分钟
    //token秘钥

//    private String tokenSecret;

    private static String secret;

    @Value("${jwt.token.secret}")
    public void setSecret(String tokenSecret) {
        secret = tokenSecret;
    }


    public static String token (String username,String password){

        String token = "";
        try {
            //过期时间
            Date date = new Date(System.currentTimeMillis()+EXPIRE_DATE);
            //秘钥及加密算法
            Algorithm algorithm = Algorithm.HMAC256(secret);
            //设置头部信息
            Map<String,Object> header = new HashMap<>();
            header.put("typ","JWT");
            header.put("alg","HS256");
            //携带username，password信息，生成签名
            token = JWT.create()
                    .withHeader(header)
                    .withClaim("username",username)
                    .withClaim("password",password).withExpiresAt(date)
                    .sign(algorithm);

        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }
        return token;
    }

    public static boolean verify(String token){
        /**
         * @desc   验证token，通过返回true
         * @params [token]需要校验的串
         **/
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return true;
        }catch (Exception e){
            log.error("校验失败");
            return  false;
        }
    }

    //获取token内的携带的用户名信息
    public static String getUserNameByToken(String token){
        DecodedJWT decodedJWT = JWT.decode(token);
        return decodedJWT.getClaim("userName").asString();
    }

    //获取token内的携带的用户信息
    public static Account getUserByToken(String token){
        DecodedJWT decodedJWT = JWT.decode(token);
        Account account = new Account();
        account.setPassword(decodedJWT.getClaim("password").asString());
        account.setAccount(decodedJWT.getClaim("username").asString());
        return account;
    }

    public static void main(String[] args) {
        String username ="zhangsan";
        String password = "123";
        String token = token(username,password);
        System.out.println(token);
//        boolean b = verify("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwYXNzd29yZCI6IjEyMyIsImV4cCI6MTY1NzA5ODE4MCwidXNlcm5hbWUiOiJ6aGFuZ3NhbiJ9.W-IgXJmNBrboXlzT_PtPkTavYhgRn9ZwkVpJoJLU6ks");
        boolean b = verify(token);
        Claim username1 = JWT.decode(token).getClaim("username");
        Claim password1 = JWT.decode(token).getClaim("password");
        System.out.println("我是从token中获取的信息"+username1.asString());
        System.out.println("我是从token中获取的信息"+password1.asString());

        System.out.println(b);
    }


}
