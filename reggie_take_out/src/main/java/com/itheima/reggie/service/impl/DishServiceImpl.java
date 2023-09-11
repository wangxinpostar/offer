package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Resource
    DishFlavorService dishFlavorService;

    /**
     * 新增菜品同时保存对应的口味
     *
     * @param dishDto
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {

        this.save(dishDto);

        Long dishId = dishDto.getId();

        List<DishFlavor> dishFlavors = dishDto.getFlavors().stream().peek(dishFlavor -> dishFlavor.setDishId(dishId)).collect(Collectors.toList());

        dishFlavorService.saveBatch(dishFlavors);
    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {

        Dish byId = this.getById(id);
        DishDto dishDto = new DishDto();

        BeanUtils.copyProperties(byId, dishDto);

        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, byId.getId());

        List<DishFlavor> list = dishFlavorService.list(lambdaQueryWrapper);

        dishDto.setFlavors(list);

        return dishDto;
    }

    @Override
    @Transactional
    public void updaeWithFlavor(DishDto dishDto) {

        this.updateById(dishDto);

        Long dishId = dishDto.getId();

        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);

        dishFlavorService.remove(lambdaQueryWrapper);

        List<DishFlavor> dishFlavors = dishDto.getFlavors().stream().peek(dishFlavor -> dishFlavor.setDishId(dishId)).collect(Collectors.toList());

        dishFlavorService.saveBatch(dishFlavors);
    }
}
