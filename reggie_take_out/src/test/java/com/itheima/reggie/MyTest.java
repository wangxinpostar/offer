package com.itheima.reggie;

import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class MyTest {
    @Resource
    SetmealService setmealService;
    @Resource
    SetmealDishService setmealDishService;

    @Test
    public void test1() {
        setmealService.list().forEach(System.out::println);
        setmealDishService.list().forEach(System.out::println);
    }

    @Test
    public void test2() {

    }

}
