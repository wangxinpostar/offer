package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author wangx
 * @description 针对表【orders(订单表)】的数据库操作Service实现
 * @createDate 2023-09-04 16:40:30
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
        implements OrdersService {
    @Resource
    private HttpServletRequest request;

    @Resource
    private ShoppingCartService shoppingCartService;

    @Resource
    private UserService userService;

    @Resource
    private AddressBookService addressBookService;

    @Resource
    private OrderDetailService orderDetailService;

    @Transactional
    @Override
    public void submit(Orders orders) {
        Long userId = (Long) request.getSession().getAttribute("user");

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        if (list == null || list.size() == 0) {
            throw new RuntimeException("购物车为空");
        }
//查询用户数据
        User user = userService.getById(userId);
//查询地址数据
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());

        if (addressBook == null) {
            throw new RuntimeException("地址不存在");
        }

//        订单数据

        AtomicInteger amount = new AtomicInteger(0);

        String id = String.valueOf(IdWorker.getId());
        List<OrderDetail> orderdetails = list.stream().map(
                ShoppingCart -> {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrderId(Long.valueOf(id));
                    orderDetail.setNumber(ShoppingCart.getNumber());
                    orderDetail.setDishFlavor(ShoppingCart.getDishFlavor());
                    orderDetail.setDishId(ShoppingCart.getDishId());
                    orderDetail.setSetmealId(ShoppingCart.getSetmealId());
                    orderDetail.setName(ShoppingCart.getName());
                    orderDetail.setImage(ShoppingCart.getImage());
                    orderDetail.setAmount(ShoppingCart.getAmount());
                    amount.addAndGet(ShoppingCart.getAmount().multiply(new BigDecimal(ShoppingCart.getNumber())).intValue());
                    return orderDetail;
                }
        ).collect(Collectors.toList());

        orders.setNumber(id);
        orders.setId(Long.valueOf(id));
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setUserId(userId);
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(
                (addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName()) + (addressBook.getCityName() == null ? "" : addressBook.getCityName()) + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName()) + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        this.save(orders);

//        订单明细数据
        orderDetailService.saveBatch(orderdetails);

//        删除购物车数据
        shoppingCartService.remove(queryWrapper);

    }
}




