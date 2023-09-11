package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * @author wangxinpo
 * @description: 自定义填充公共字段
 * @date 2023/09/02
 */
@Component
public class MyMetaObjecthandler implements MetaObjectHandler {

    @Resource
    private HttpServletRequest request;

    /**
     * 插入时填充
     *
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        if (request.getSession().getAttribute("employee") != null) {
            metaObject.setValue("createUser", request.getSession().getAttribute("employee"));
            metaObject.setValue("updateUser", request.getSession().getAttribute("employee"));
        } else {
            metaObject.setValue("createUser", request.getSession().getAttribute("user"));
            metaObject.setValue("updateUser", request.getSession().getAttribute("user"));
        }
    }

    /**
     * 更新时填充
     *
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        if (request.getSession().getAttribute("employee") != null)
            metaObject.setValue("updateUser", request.getSession().getAttribute("employee"));
        else
            metaObject.setValue("updateUser", request.getSession().getAttribute("user"));
        metaObject.setValue("updateTime", LocalDateTime.now());
    }
}
