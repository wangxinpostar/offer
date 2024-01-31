package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.SystemConstants;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    RedisTemplate<String, String> redisTemplate;

    @Resource
    CacheClient cacheClient;

    @Resource
    IShopService shopService;

    @Override
    public Result queryById(Long id) {
//        Shop shop = queryWithLogicalExpire(id);
        Shop shop = cacheClient.queryWithLogicalExpire(RedisConstants.CACHE_SHOP_KEY, id, Shop.class, this::getById, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

        if (shop == null) return Result.fail("店铺不存在");
        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result update(Shop shop) {

        Long id = shop.getId();

        if (id == null) {
            return Result.fail("店铺id不能为空");
        }

        updateById(shop);

        redisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + id);

        return Result.ok();
    }

    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        if (x == null || y == null) {
            // 根据类型分页查询
            Page<Shop> page = shopService.lambdaQuery()
                    .eq(Shop::getTypeId, typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            // 返回数据
            return Result.ok(page.getRecords());
        }

        String key = RedisConstants.SHOP_GEO_KEY + typeId;

        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;

        // 设置搜索半径（5000米）

// 使用RedisTemplate执行radius命令
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(key, new Circle(x, y, 5000), RedisGeoCommands.GeoRadiusCommandArgs
                        .newGeoRadiusArgs()
                        .includeDistance()
                        .limit(end)
                );

//        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo().search(key, GeoReference.fromCoordinate(x, y), new Distance(5000), RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end));

        if (results == null) return Result.ok(Collections.emptyList());

        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();

        List<Shop> shops = new ArrayList<>(list.size());

        list.stream().skip(from).forEach(result -> {

            String shopId = result.getContent().getName();

            Distance distance = result.getDistance();

            Shop shop = getById(shopId);

            shops.add(shop.setDistance(distance.getValue()));

        });
        return Result.ok(shops);
    }

}
