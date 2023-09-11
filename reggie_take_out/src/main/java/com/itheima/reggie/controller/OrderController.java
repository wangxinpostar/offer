package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("order")
public class OrderController {
    @Resource
    private OrdersService orderService;
    @Resource
    private OrderDetailService orderDetailService;

    @PostMapping("submit")
    public R<String> submit(@RequestBody Orders orders) {
        orderService.submit(orders);
        return R.success("提交成功");
    }

    @GetMapping("/page")
    public R<Page<Orders>> page(int page, int pageSize, String number, String beginTime, String endTime) {

        //构造分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(number != null, Orders::getId, number);
        queryWrapper.ge(beginTime != null, Orders::getOrderTime, beginTime);
        queryWrapper.le(endTime != null, Orders::getOrderTime, endTime);
        //添加排序条件
        queryWrapper.orderByDesc(Orders::getCheckoutTime);

        //执行查询
        orderService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    @GetMapping("/userPage")
    public R<Page<OrdersDto>> page(int page, int pageSize, HttpServletRequest request) {

        Page<Orders> orders = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDto = new Page<>();

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, request.getSession().getAttribute("user"));
        queryWrapper.orderByDesc(Orders::getCheckoutTime);

        orderService.page(orders, queryWrapper);

        BeanUtils.copyProperties(orders, ordersDto, "records");

        List<OrdersDto> OrdersDtos = orders.getRecords().stream().map(order -> {
            OrdersDto ordersDtoit = new OrdersDto();
            BeanUtils.copyProperties(order, ordersDtoit);

            LambdaQueryWrapper<OrderDetail> queryWrapperit = new LambdaQueryWrapper<>();
            queryWrapperit.eq(OrderDetail::getOrderId, order.getId());

            List<OrderDetail> OrderDetaillist = orderDetailService.list(queryWrapperit);

            ordersDtoit.setOrderDetails(OrderDetaillist);
            ordersDtoit.setSumNum(OrderDetaillist.size());

            return ordersDtoit;
        }).collect(Collectors.toList());

        ordersDto.setRecords(OrdersDtos);

        return R.success(ordersDto);
    }
}
