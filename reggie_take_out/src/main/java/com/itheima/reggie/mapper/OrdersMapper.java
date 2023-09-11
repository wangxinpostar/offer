package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wangx
 * @description 针对表【orders(订单表)】的数据库操作Mapper
 * @createDate 2023-09-04 16:40:30
 * @Entity com.itheima.reggie.Orders
 */

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

}




