package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    RedisTemplate<String, String> redisTemplate;

    @Override
    public Result queryShopTypeString() {

        Set<String> shopTypeJSonSet = redisTemplate.opsForZSet().range(RedisConstants.CACHE_SHOP_LIST_KEY, 0, -1);

        if (shopTypeJSonSet != null && shopTypeJSonSet.size() != 0) {

            List<ShopType> shopTypes = new ArrayList<>();

            shopTypeJSonSet.forEach(x -> shopTypes.add(JSONUtil.toBean(x, ShopType.class)));

            return Result.ok(shopTypes);

        }

        List<ShopType> shopTypes = lambdaQuery().orderByAsc(ShopType::getSort).list();

        if (shopTypes == null || shopTypes.size() == 0) {
            return Result.fail("分类不存在");
        }

        shopTypes.forEach(x -> redisTemplate.opsForZSet().add(RedisConstants.CACHE_SHOP_LIST_KEY, JSONUtil.toJsonStr(x), x.getSort()));

        redisTemplate.expire(RedisConstants.CACHE_SHOP_LIST_KEY, RedisConstants.CACHE_SHOP_LIST_TTL, TimeUnit.MINUTES);

        return Result.ok(shopTypes);
    }
}
