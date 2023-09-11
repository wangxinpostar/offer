package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Resource
    SetmealDishService setmealDishService;

    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {

        this.save(setmealDto);

        setmealDishService.saveBatch(setmealDto.getSetmealDishes().stream().peek(setmealDish -> {
            setmealDish.setSetmealId(setmealDto.getId());
        }).collect(Collectors.toList()));

    }

    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        for (Long id : ids) {
            //判断套餐是否正在售卖中
            LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
            setmealLambdaQueryWrapper.eq(Setmeal::getId, id).eq(Setmeal::getStatus, 1);

            int count = this.count(setmealLambdaQueryWrapper);
            if (count > 0) {
                throw new CustomException("套餐正在售卖中，无法删除");
            }
            //删除套餐
            this.removeById(id);

            LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
            setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);
            //删除套餐和菜品的关联
            setmealDishService.remove(setmealDishLambdaQueryWrapper);

        }
    }
}
