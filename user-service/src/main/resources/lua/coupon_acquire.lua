-- KEYS[1]: coupon:stock:{couponId}
-- KEYS[2]: coupon:users:{couponId}
-- ARGV[1]: userId

-- 重复领取判断
if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
    return 1
end

-- 库存不足
if tonumber(redis.call('GET', KEYS[1])) <= 0 then
    return 2
end

-- 扣库存
redis.call('DECR', KEYS[1])
-- 记录用户
redis.call('SADD', KEYS[2], ARGV[1])

return 0
