package com.historical.voting.user.strategy;

import org.springframework.stereotype.Component;

public interface GraceStrategy {
    /**
     * 计算膨胀金额
     * @param originalAmount 原始金额（单位：分）
     * @return 膨胀后的金额
     */
    int inflate(int originalAmount);

    /**
     *
     */
    @Component("REGISTER")
    class RegisterGraceStrategy implements GraceStrategy {
        @Override
        public int inflate(int originalAmount) {
            return originalAmount * 2;
        }
    }
    @Component("CHECK_IN")
    class CheckInGraceStrategy implements GraceStrategy {
        @Override
        public int inflate(int originalAmount) {
            return originalAmount + originalAmount / 2;
        }
    }
    @Component("FESTIVAL")
    class FestivalGraceStrategy implements GraceStrategy {
        @Override
        public int inflate(int originalAmount) {
            return originalAmount * 180 / 100;
        }
    }
}
