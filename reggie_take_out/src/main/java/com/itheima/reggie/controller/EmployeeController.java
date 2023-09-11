package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author wangxinpo
 * @description: 员工管理
 * @date 2023/09/02
 */
@Slf4j
@RestController
@RequestMapping("employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * @param httpServletRequest
     * @param employee
     * @return {@code R<Employee>}
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest httpServletRequest, @RequestBody Employee employee) {
        String password = DigestUtils.md5DigestAsHex(employee.getPassword().getBytes());

        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(lambdaQueryWrapper);

        if (emp == null) {
            return R.error("登陆失败");
        }

        if (!emp.getPassword().equals(password)) {
            return R.error("登陆失败");
        }

        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        httpServletRequest.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /**
     * @param httpServletRequest
     * @return {@code R<String>}
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest httpServletRequest) {
        httpServletRequest.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * @param httpServletRequest
     * @param employee
     * @return {@code R<String>}
     */
    @PostMapping
    public R<String> save(HttpServletRequest httpServletRequest, @RequestBody Employee employee) {
        log.info("employee: {}", employee);
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setCreateTime(LocalDateTime.now());

//        Long empID = (Long) httpServletRequest.getSession().getAttribute("employee");

//        employee.setUpdateUser(empID);
//        employee.setCreateUser(empID);

        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * @param page
     * @param pageSize
     * @param name
     * @return {@code R<Page>}
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page: {}, pageSize: {}, name: {}", page, pageSize, name);

        Page pageInfo = new Page(page, pageSize);

        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);

        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);

        employeeService.page(pageInfo, lambdaQueryWrapper);

        return R.success(pageInfo);
    }

    /**
     * @param httpServletRequest
     * @param employee
     * @return {@code R<String>}
     */
    @PutMapping
    public R<String> update(HttpServletRequest httpServletRequest, @RequestBody Employee employee) {
        log.info("employee: {}", employee);

//        Long empID = (Long) httpServletRequest.getSession().getAttribute("employee");
//
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empID);

        employeeService.updateById(employee);
        return R.success("修改员工成功");
    }

    /**
     * @param id
     * @return {@code R<Employee>}
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("员工不存在");
    }
}
