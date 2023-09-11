package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/shoppingCart")
@RestController
public class ShoppingCartController {
    @Resource
    private ShoppingCartService shoppingCartService;
    @Resource
    private HttpServletRequest httpServletRequest;

    /**
     * 添加购物车
     *
     * @param shoppingCart
     * @return {@code R<ShoppingCart>}
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        shoppingCart.setUserId((Long) httpServletRequest.getSession().getAttribute("user"));

        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, shoppingCart.getUserId());

        if (shoppingCart.getDishId() != null) {
            lambdaQueryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart one = shoppingCartService.getOne(lambdaQueryWrapper);
        if (one != null) {
            one.setNumber(one.getNumber() + 1);
            shoppingCartService.updateById(one);
        } else {
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            one = shoppingCart;
        }

        return R.success(one);
    }

    /**
     * 查询购物车
     *
     * @return {@code R<List<ShoppingCart>>}
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {

        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, httpServletRequest.getSession().getAttribute("user"));
        lambdaQueryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(lambdaQueryWrapper);

        return R.success(list);
    }

    /**
     * 清空购物车
     *
     * @return {@code R<String>}
     */
    @DeleteMapping("/clean")
    public R<String> clear() {
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, httpServletRequest.getSession().getAttribute("user"));

        shoppingCartService.remove(lambdaQueryWrapper);

        return R.success("清空成功");
    }

    /**
     * 删除购物车
     *
     * @param shoppingCart
     * @return {@code R<ShoppingCart>}
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {

        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, httpServletRequest.getSession().getAttribute("user"));

        if (shoppingCart.getDishId() != null) {
            lambdaQueryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart one = shoppingCartService.getOne(lambdaQueryWrapper);

        if (one == null)
            return R.error("购物车中没有该商品");

        if (one.getNumber() <= 1) {
            shoppingCartService.remove(lambdaQueryWrapper);
        }

        one.setNumber(one.getNumber() - 1);
        shoppingCartService.updateById(one);

        return R.success(one);
    }
}
