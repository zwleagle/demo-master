package com.zwl.demo.admin.controller;


import com.google.code.kaptcha.Producer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Controller
@Api(tags = "CaptchaController", description = "图片验证码")
@RequestMapping("/captcha")
public class CaptchaController {


    @Autowired
    private Producer captchaProducer;

    @ApiOperation(value = "获取图片验证码")
    @RequestMapping(value = "/captcha.jpg", method = RequestMethod.GET)
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {

        //设置内容类型
        response.setContentType("image/jpeg");

        //创建验证码文本
        String text = captchaProducer.createText();

        //将验证码文 本设置到session
        request.getSession().setAttribute("captcha", text);

        //创建验证码图片
        BufferedImage bufferedImage = captchaProducer.createImage(text);

        //获取响应输出流
        ServletOutputStream outputStream = response.getOutputStream();

        //将图片验证码数据写到响应输出流
        ImageIO.write(bufferedImage, "jpg", outputStream);


        try {
            outputStream.flush();
        }finally {
            outputStream.close();
        }


    }
}
