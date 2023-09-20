package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Override
    public Result seckillVoucher(Long voucherId) {

        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);

        LocalDateTime beginTime = voucher.getBeginTime();

        Integer stock = voucher.getStock();

        if (beginTime.isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀尚未开始");
        }

        LocalDateTime endTime = voucher.getEndTime();

        if (endTime.isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀已经结束！");
        }

        if (stock < 1) {
            return Result.fail("库存不足！");
        }

        Long userId = UserHolder.getUser().getId();

        RLock lock = redissonClient.getLock("lock:order:" + userId);
        boolean isLock = lock.tryLock();

//        SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, redisTemplate);
//        boolean isLock = lock.tryLock(5);

        if (!isLock) {
            return Result.fail("不允许重复下单");
        }

        try {
//            获取代理对象
            IVoucherOrderService proxy = ((IVoucherOrderService) AopContext.currentProxy());
            return proxy.createVoucherOrder(voucherId, userId);
        } finally {
            lock.unlock();
        }

    }

    @Transactional
    public Result createVoucherOrder(Long voucherId, Long userId) {

        int count = this.lambdaQuery().eq(VoucherOrder::getUserId, userId).eq(VoucherOrder::getVoucherId, voucherId).count();

        if (count > 0)
            return Result.fail("用户已经购买过一次");

        boolean success = seckillVoucherService.lambdaUpdate().setSql("stock = stock - 1").eq(SeckillVoucher::getVoucherId, voucherId).gt(SeckillVoucher::getStock, 0).update();

        if (!success) {
            return Result.fail("库存不足！");
        }

        VoucherOrder voucherOrder = new VoucherOrder();
        Long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);

        save(voucherOrder);

        return Result.ok(orderId);
    }
}
