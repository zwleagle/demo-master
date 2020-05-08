package com.zwl.demo.security.compoment;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zwl.demo.security.authentication.SecurityAuthenticationFailureHandler;
import com.zwl.demo.security.exception.VerificationCodeException;
import com.zwl.demo.security.utils.ContentCachRequestWrapper;
import com.zwl.demo.security.utils.RequestWrapper;
import org.apache.commons.io.IOUtils;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class VerificationCodeFilter extends OncePerRequestFilter {

    private AuthenticationFailureHandler authenticationFailureHandler = new SecurityAuthenticationFailureHandler();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 非登录请求不校验验证码
        if (!"/admin/login".equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
        } else {
            try {

                //RequestWrapper requestWrapper = new RequestWrapper((HttpServletRequest)request);

                ContentCachRequestWrapper requestWrapper = new ContentCachRequestWrapper((HttpServletRequest) request);

                verificationCode(requestWrapper);



                filterChain.doFilter(requestWrapper, response);
            } catch (VerificationCodeException e) {
                authenticationFailureHandler.onAuthenticationFailure(request, response, e);
            }
        }


    }


    public void verificationCode (ContentCachRequestWrapper httpServletRequest) throws VerificationCodeException, IOException {

        String body = IOUtils.toString(httpServletRequest.getBody(), httpServletRequest.getCharacterEncoding());


        JSONObject jsonObject =   JSONUtil.parseObj(body);
        String requestCode =  jsonObject.get("captcha").toString();

       // String requestCode = httpServletRequest.getParameter("captcha");
        HttpSession session = httpServletRequest.getSession();
        String savedCode = (String) session.getAttribute("captcha");
        if (!StringUtils.isEmpty(savedCode)) {
            // 随手清除验证码，不管是失败还是成功，所以客户端应在登录失败时刷新验证码
            session.removeAttribute("captcha");
        }
        // 校验不通过抛出异常
        if (StringUtils.isEmpty(requestCode) || StringUtils.isEmpty(savedCode) || !requestCode.equals(savedCode)) {
            throw new VerificationCodeException();
        }
    }
}
