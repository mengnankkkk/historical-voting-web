package com.historical.voting.user.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.historical.voting.user.entity.Coupon;
import com.historical.voting.user.mapper.CouponMapper;
import com.historical.voting.user.service.CouponService;
import jdk.internal.dynalink.linker.LinkerServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    @Autowired
    private DefaultRedisScript<Long> couponLuaScript;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 获取优惠卷
     * @param couponId
     * @param userId
     * @return
     */
    @Override
    public int acquireCoupon(Long couponId, Long userId) {
        String stockKey = "coupon:stock:" + couponId;
        String usersKey = "coupon:users:" + couponId;

        List<String> keys = Arrays.asList(stockKey,usersKey);
        Long result = redisTemplate.execute(couponLuaScript,keys,userId.toString());
        return result==null?-1:result.intValue();
    }

    /**
     * 通过id查看优惠卷
     * @param couponId
     * @return
     */
    @Override
    public Coupon queryById(Long couponId) {
        return couponMapper.selectById(couponId);
    }
}
