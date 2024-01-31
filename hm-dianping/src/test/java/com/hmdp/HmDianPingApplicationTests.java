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
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Test
    public void loadShopData() {
        List<Shop> list = shopService.list();
        Map<Long, List<Shop>> map = list.stream().collect(Collectors.groupingBy(Shop::getTypeId));
        for (Map.Entry<Long, List<Shop>> entry : map.entrySet()) {

            Long typeID = entry.getKey();
            String key = RedisConstants.SHOP_GEO_KEY + typeID;

            List<Shop> value = entry.getValue();

            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>();

            value.forEach(shop -> locations.add(new RedisGeoCommands.GeoLocation<>(
                            shop.getId().toString(),
                            new Point(shop.getX(), shop.getY())
                    )
            ));
            redisTemplate.opsForGeo().add(key, locations);
        }
    }

    @Test
    public void testHyperLogLog() {
        String[] users = new String[1000];
        int index = 0;
        for (int i = 1; i <= 1000000; i++) {
            users[index++] = "user_" + i;
            if (i % 1000 == 0) {
                index = 0;
                redisTemplate.opsForHyperLogLog().add("hll1", users);
            }
        }
        Long size = redisTemplate.opsForHyperLogLog().size("hll1");
        System.out.println("size = " + size);
    }
}
