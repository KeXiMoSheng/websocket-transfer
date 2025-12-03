package com.jun.websockettransfer.controller;

/**
 * @Author: jun
 * @Date: 2025/12/2 17:13 （日期和时间）
 */
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: jun
 * @Date: 2025/12/2 17:13 （日期和时间）
 */
@RestController
@RequestMapping("/websocket")
public class WebsocketController {
    @RequestMapping("/test")
    public String test() {
        return "test";
    }
}
