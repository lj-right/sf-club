package com.suifeng.practice.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 练习套卷controller
 */
@RestController
@RequestMapping("/practice/set")
@Slf4j
public class PracticeSetController {
    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }
}
