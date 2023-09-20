package com.hmdp;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.Shop;
import com.hmdp.entity.User;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.service.impl.UserServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private CacheClient cacheClient;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private UserServiceImpl userService;

    @Test
    public void testSaveShop() {
        List<Shop> list = shopService.list();
        list.forEach(x -> cacheClient.setWithLogicalExpire(RedisConstants.CACHE_SHOP_KEY + x.getId(), x, 10L, TimeUnit.SECONDS));
    }

    @Test
    public void idWordke() {
        for (int i = 0; i < 100; i++) {
            System.out.println(redisIdWorker.nextId("shop"));
        }

    }

    @Test
    public void stock() {
        SeckillVoucher voucher = seckillVoucherService.getById(12);
        Integer stock = voucher.getStock();
        boolean success = seckillVoucherService.updateById(voucher.setStock(stock - 1));
    }

    @Test
//    生产用户Token
    public void user() {

        List<User> Users = userService.list();

        Users.forEach(userDTO -> {

            String token = UUID.randomUUID().toString(true);

            Map<String, Object> beanToMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                    CopyOptions.create()
                            .setIgnoreNullValue(true)
                            .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));

            redisTemplate.opsForHash().putAll(RedisConstants.LOGIN_USER_KEY + token, beanToMap);

        });

    }
}
