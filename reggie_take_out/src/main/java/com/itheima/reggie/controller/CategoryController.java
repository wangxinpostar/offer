package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理
 *
 * @author wangxinpo
 * @date 2023/09/02
 */
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    /**
     * 添加分类
     *
     * @param category
     * @return {@code R<String>}
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        categoryService.save(category);
        return R.success("添加分类成功");
    }

    /**
     * 查询分类
     *
     * @param page
     * @param pageSize
     * @return {@code R<Page>}
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        Page<Category> categoryPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByAsc(Category::getSort);

        categoryService.page(categoryPage, lambdaQueryWrapper);

        return R.success(categoryPage);
    }

    /**
     * 删除分类
     *
     * @param ids
     * @return {@code R<String>}
     */
    @DeleteMapping
    public R<String> delete(Long ids) {
        categoryService.remove(ids);
        return R.success("删除分类成功");
    }

    @PutMapping
    public R<String> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return R.success("修改分类成功");
    }

    /**
     * 查询分类列表
     *
     * @param category
     * @return {@code R<List<Category>>}
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.eq(category.getType() != null, Category::getType, category.getType());

        lambdaQueryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(lambdaQueryWrapper);

        return R.success(list);
    }
}