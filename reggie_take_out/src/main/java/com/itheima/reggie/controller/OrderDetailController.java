package com.itheima.reggie.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("orderDetail")
public class OrderDetailController {
    @Resource
    private OrderDetailController orderDetailController;
}
