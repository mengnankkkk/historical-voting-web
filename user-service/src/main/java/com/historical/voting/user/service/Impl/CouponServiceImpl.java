package com.historical.voting.user.service.Impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.historical.voting.user.Factory.GraceStrategyFactory;
import com.historical.voting.user.entity.Coupon;
import com.historical.voting.user.entity.GraceType;
import com.historical.voting.user.entity.User;
import com.historical.voting.user.mapper.CouponMapper;
import com.historical.voting.user.mapper.UserMapper;
import com.historical.voting.user.service.CouponService;
import com.historical.voting.user.strategy.GraceStrategy;
import jdk.internal.dynalink.linker.LinkerServices;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.TIMEOUT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    @Autowired
    private DefaultRedisScript<Long> couponLuaScript;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GraceStrategyFactory graceStrategyFactory;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取优惠卷
     * @param couponId
     * @param userId
     * @return
     */
    @Override
    public int acquireCoupon(Long couponId, Long userId) {
        String limitKey = "coupon:req:" + couponId + ":" + userId;
        String stockKey = "coupon:stock:" + couponId;
        String usersKey = "coupon:users:" + couponId;
        //防止重复点击

        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(limitKey,"1",2, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isNew)) return -3;


        List<String> keys = Arrays.asList(stockKey,usersKey);
        Long result = redisTemplate.execute(couponLuaScript,keys,userId.toString());
        return result==null?-1:result.intValue();
    }

    /**
     * 通过id查看优惠卷,采用一致性策略。
     * @param couponId
     * @return
     */
    @Override
    public Coupon queryById(Long couponId) {
        String redisKey = "coupon:grace:" + couponId;
        String json = redisTemplate.opsForValue().get(redisKey);
        if (StringUtils.hasText(json)){
            return JSON.parseObject(json, Coupon.class);
        }
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon==null) return null;
        redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(coupon), Duration.ofMinutes(30));

        return coupon;
    }

    @Override
    public int applyGrace(Long couponId, GraceType type,Long userId) {
        if (!userHasGracePermission(userId,type)){
            throw new RuntimeException("无权使用");
        }
        String redisKey = "coupon:grace:" + couponId + ":" + userId + ":" + type.name();
        Boolean isLimited = Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        if (isLimited){
            throw new RuntimeException("今天已经膨胀过");
        }


        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon!=null) throw new RuntimeException("优惠劵不存在");

        GraceStrategy strategy = graceStrategyFactory.getStrategy(type);
        int newAmount = strategy.inflate(coupon.getAmount());

        coupon.setAmount(newAmount);
        couponMapper.updateById(coupon);

        redisTemplate.opsForValue().set(redisKey,"1", Duration.ofDays(1));//记录标记，然后设置过期时间
        return newAmount;
    }


    private boolean userHasGracePermission(Long userId, GraceType type){
        User user = userMapper.selectById(userId);
        if (user==null) return false;

        if (type==GraceType.FESTIVAL&&user.getLevel()>=5){
            return true;
        }
        return false;

    }
}
