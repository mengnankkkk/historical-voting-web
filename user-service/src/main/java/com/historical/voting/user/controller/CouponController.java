package com.historical.voting.user.controller;


import com.historical.voting.user.entity.Coupon;
import com.historical.voting.user.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupon")
public class CouponController {
    @Autowired
    private CouponService couponService;

    @PostMapping("/acquire")
    public ResponseEntity<String> acquire(@RequestParam Long couponId,@RequestParam Long userId){
        int result = couponService.acquireCoupon(couponId,userId);
        switch(result){
            case 0:
                return ResponseEntity.ok("领取成功");
            case 1:
                return ResponseEntity.badRequest().body("你已领取过该优惠券");
            case 2:
                return ResponseEntity.badRequest().body("优惠券已领完");
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("系统异常");
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<Coupon> getCoupon(@PathVariable("id") Long id){
        return ResponseEntity.ok(couponService.queryById(id));
    }
}
