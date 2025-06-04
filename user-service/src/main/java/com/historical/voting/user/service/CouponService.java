package com.historical.voting.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.historical.voting.user.entity.Coupon;

public interface CouponService extends IService<Coupon> {

    int acquireCoupon(Long couponId, Long userId);

    Coupon queryById(Long couponId);
}
