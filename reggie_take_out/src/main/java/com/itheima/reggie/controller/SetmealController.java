package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Resource
    SetmealService setmealService;
    @Resource
    CategoryService categoryService;
    @Resource
    SetmealDishService setmealDishService;
    @Resource
    DishService dishService;

    /**
     * 保存套餐
     *
     * @param setmealDto
     * @return {@code R<String>}
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveWithDish(setmealDto);
        return R.success("套餐保存成功");
    }

    /**
     * 获取套餐
     *
     * @param page
     * @param pageSize
     * @param name
     * @return {@code R<Page>}
     */
    @GetMapping("/page")
    public R<Page> list(int page, int pageSize, String name) {

        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<Setmeal>();

        lambdaQueryWrapper.like(name != null, Setmeal::getName, name);

        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(setmealPage, lambdaQueryWrapper);

        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");

        List<SetmealDto> collect = setmealPage.getRecords().stream().map(setmeal -> {

            SetmealDto setmealDto = new SetmealDto();

            BeanUtils.copyProperties(setmeal, setmealDto);

            Category byId = categoryService.getById(setmeal.getCategoryId());
            if (byId != null) {
                setmealDto.setCategoryName(byId.getName());
            }
            return setmealDto;

        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(collect);

        return R.success(setmealDtoPage);
    }

    /**
     * 删除套餐
     *
     * @param ids
     * @return {@code R<String>}
     */

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        setmealService.removeWithDish(ids);
        return R.success("删除套餐成功");
    }

    /**
     * 获取套餐列表
     *
     * @param setmeal
     * @return {@code R<List<Setmeal>>}
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {

        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId()).eq(setmeal.getStatus() != null, Setmeal::getStatus, 1);
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(lambdaQueryWrapper);
        return R.success(list);

    }

    /**
     * 获取套餐详情
     *
     * @param id
     * @return {@code R<List<DishDto>>}
     */
    @GetMapping("dish/{id}")
    public R<List<DishDto>> getSetmealDto(@PathVariable Long id) {

        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);

        List<SetmealDish> setmealDishlist = setmealDishService.list(lambdaQueryWrapper);


        List<DishDto> collect = setmealDishlist.stream().map(
                setmealDish -> {
                    DishDto dishDto = new DishDto();

                    Dish byId = dishService.getById(setmealDish.getDishId());

                    BeanUtils.copyProperties(byId, dishDto);

                    dishDto.setCopies(setmealDish.getCopies());

                    return dishDto;
                }
        ).collect(Collectors.toList());


        return R.success(collect);
    }
}
