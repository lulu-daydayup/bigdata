package org.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 *
 * @date 2023/1/9 11:01 PM
 **/
@RestController
public class HelloController {
    @RequestMapping("/hello")
    public String hello() {
        return "Hello Spring Boot";
    }
}
