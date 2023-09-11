package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wangx
 * @description 针对表【order_detail(订单明细表)】的数据库操作Mapper
 * @createDate 2023-09-04 16:40:29
 * @Entity com.itheima.reggie.OrderDetail
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

}




