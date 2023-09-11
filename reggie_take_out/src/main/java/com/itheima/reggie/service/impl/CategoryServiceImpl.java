package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Resource
    DishService dishService;
    @Resource
    SetmealService setmealService;

    /**
     * 删除分类
     *
     * @param id
     */
    @Override
    public void remove(Long id) {
//        查询分类是否关联菜品
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();

        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);

        int count1 = dishService.count(dishLambdaQueryWrapper);

        if (count1 > 0) {
//          该分类下有菜品，不能删除
            throw new CustomException("该分类下有菜品，不能删除");
        }


//        查询分类是否关联套餐

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();

        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);

        int count2 = setmealService.count(setmealLambdaQueryWrapper);

        if (count2 > 0) {
//          该分类下有套餐，不能删除
            throw new CustomException("该分类下有套餐，不能删除");
        }

//        正常删除
        super.removeById(id);
    }
}
