package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {
    @Resource
    private DishService dishService;
    @Resource
    private DishFlavorService dishFlavorService;
    @Resource
    private CategoryService categoryService;

    /**
     * 添加菜品
     *
     * @param dishDto
     * @return {@code R<String>}
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        return R.success("添加菜品成功");
    }

    /**
     * 查询菜品
     *
     * @param page
     * @param pageSize
     * @param name
     * @return {@code R<Page>}
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {


        Page<Dish> dishpageInfo = new Page<>(page, pageSize);
        Page<Dish> dishDtopageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Dish> DishWrapper = new LambdaQueryWrapper<>();

        DishWrapper.like(name != null, Dish::getName, name);

        DishWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(dishpageInfo, DishWrapper);

        BeanUtils.copyProperties(dishpageInfo, dishDtopageInfo, "records");

        List<Dish> collect = dishpageInfo.getRecords().stream().map(dish -> {

            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(dish, dishDto);

            Category byId = categoryService.getById(dish.getCategoryId());

            if (byId != null) {
                dishDto.setCategoryName(byId.getName());
            }
            return dishDto;

        }).collect(Collectors.toList());


        dishDtopageInfo.setRecords(collect);

        return R.success(dishDtopageInfo);
    }

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return {@code R<DishDto>}
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     * @return {@code R<String>}
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updaeWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }

    /**
     * 删除菜品
     *
     * @param ids
     * @return {@code R<String>}
     */
    @Transactional
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        for (Long id : ids) {
//        删除菜品
            dishService.removeById(id);

//        删除菜品口味
            LambdaQueryWrapper<DishFlavor> dishFlavorWrapper = new LambdaQueryWrapper<>();
            dishFlavorWrapper.eq(DishFlavor::getDishId, id);
            dishFlavorService.remove(dishFlavorWrapper);
        }
        return R.success("删除菜品成功");
    }

    /**
     * 根据菜品分类Id查询所有菜品
     *
     * @param
     * @return {@code R<List<Dish>>}
     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish) {
//
//        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<Dish>();
//
//        lambdaQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId()).eq(Dish::getStatus, 1);
//
//        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> list = dishService.list(lambdaQueryWrapper);
//
//        return R.success(list);
//
//    }
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {

        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<Dish>();

        lambdaQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId()).eq(Dish::getStatus, 1);

        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(lambdaQueryWrapper);

        List<DishDto> dishDtolist = list.stream().map(dishitem -> dishService.getByIdWithFlavor(dishitem.getId())).collect(Collectors.toList());

        return R.success(dishDtolist);

    }
}