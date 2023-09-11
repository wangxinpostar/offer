package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/addressBook")
public class AddressBookController {
    @Resource
    AddressBookService addressBookService;

    @Resource
    HttpServletRequest request;

    /**
     * 保存地址
     *
     * @param addressBook
     * @return {@code R<AddressBook>}
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        addressBook.setUserId((Long) request.getSession().getAttribute("user"));
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 更改地址
     *
     * @param addressBook
     * @return {@code R<AddressBook>}
     */
    @PutMapping
    public R<AddressBook> update(@RequestBody AddressBook addressBook) {
        addressBook.setUserId((Long) request.getSession().getAttribute("user"));
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 删除地址
     *
     * @param ids
     * @return {@code R<String>}
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        for (Long id : ids) {
            addressBookService.removeById(id);
        }
        return R.success("删除成功");
    }


    /**
     * 修改默认地址
     *
     * @param addressBook
     * @return {@code R<AddressBook>}
     */
    @Transactional
    @PutMapping("/default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId, request.getSession().getAttribute("user")).set(AddressBook::getIsDefault, 0);
        addressBookService.update(wrapper);

        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 根据id查询地址
     *
     * @param id
     * @return {@code R}
     */
    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook == null) {
            return R.error("地址不存在");
        }
        return R.success(addressBook);
    }

    /**
     * 查询默认地址
     *
     * @return {@code R<AddressBook>}
     */
    @GetMapping("/default")
    public R<AddressBook> getDefault() {
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId, request.getSession().getAttribute("user")).eq(AddressBook::getIsDefault, 1);
        AddressBook addressBook = addressBookService.getOne(wrapper);
        if (addressBook == null) {
            return R.error("地址不存在");
        }
        return R.success(addressBook);
    }

    /**
     * 查询用户所有地址
     *
     * @return {@code R<List<AddressBook>>}
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list() {

        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressBook::getUserId, request.getSession().getAttribute("user"));
        wrapper.orderByDesc(AddressBook::getIsDefault);

        List<AddressBook> list = addressBookService.list(wrapper);

        return R.success(list);
    }
}
