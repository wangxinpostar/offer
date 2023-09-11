package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 用户注册
     *
     * @param user
     * @param session
     * @return {@code R<String>}
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {

        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)) {

            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            log.info("发送验证码：{}", code);

//            SMSUtils.sendMessage("SMS_205897619", user.getPhone(), code, null);

//            session.setAttribute(phone, code);

            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);

            return R.success("发送成功");
        } else {
            return R.error("手机号不能为空");
        }
    }

    /**
     * 用户登录
     *
     * @param map
     * @param session
     * @return {@code R<User>}
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, String> map, HttpSession session) {

        String UserPhone = map.get("phone");

        String UserCode = map.get("code");

//        Object codeInSession = session.getAttribute(UserPhone);

        Object codeInSession = redisTemplate.opsForValue().get(UserPhone);

        if (codeInSession == null || !codeInSession.equals(UserCode)) {
            return R.error("验证码错误");
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(User::getPhone, UserPhone);

        User one = userService.getOne(queryWrapper);

        if (one == null) {
            one = User.builder().phone(UserPhone).status(1).build();
            userService.save(one);
        }

        session.setAttribute("user", one.getId());

        redisTemplate.delete(UserPhone);

        return R.success(one);

    }
}
