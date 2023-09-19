package com.hmdp;

import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.Shop;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
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
    private ISeckillVoucherService seckillVoucherService;

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
}
