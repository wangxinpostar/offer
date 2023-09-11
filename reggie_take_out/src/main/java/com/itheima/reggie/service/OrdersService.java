package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;

/**
 * @author wangx
 * @description 针对表【orders(订单表)】的数据库操作Service
 * @createDate 2023-09-04 16:40:30
 */
public interface OrdersService extends IService<Orders> {
    /**
     * 提交订单
     *
     * @param orders
     */
    public void submit(Orders orders);
}
