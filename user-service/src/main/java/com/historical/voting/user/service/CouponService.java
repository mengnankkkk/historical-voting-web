package com.historical.voting.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.historical.voting.user.entity.Coupon;
import com.historical.voting.user.entity.type.GraceType;

public interface CouponService extends IService<Coupon> {

    int acquireCoupon(Long couponId, Long userId);

    Coupon queryById(Long couponId);
    int applyGrace(Long couponId, GraceType type,Long userId);
}
