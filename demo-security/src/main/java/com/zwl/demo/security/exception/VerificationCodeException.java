package com.zwl.demo.security.exception;


import org.springframework.security.core.AuthenticationException;

public class VerificationCodeException extends AuthenticationException {

    public VerificationCodeException() {
        super("图片验证码校验错误");
    }
}
