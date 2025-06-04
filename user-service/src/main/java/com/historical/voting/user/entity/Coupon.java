package com.historical.voting.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("coupon")
public class Coupon {
    private Long id;

    /** 优惠券名称 */
    private String title;

    /** 面额（单位：分） */
    private Integer amount;

    /** 库存总数 */
    private Integer stock;

    /** 每人限领几张 */
    private Integer limitPerUser;

    /** 有效期起始时间 */
    private LocalDateTime startTime;

    /** 有效期结束时间 */
    private LocalDateTime endTime;

    /** 状态（0=未开始, 1=进行中, 2=已结束） */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
    @Data
    @TableName("coupon_user")
    public class CouponUser {
        private Long id;
        private Long couponId;
        private Long userId;
        private LocalDateTime acquireTime;
    }
}
