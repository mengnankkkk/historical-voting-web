package com.historical.voting.user.Factory;

import com.historical.voting.user.strategy.TranscodingStrategy;

import org.springframework.stereotype.Component;

import java.util.Map;
@Component
public class TranscodingStrategyFactory {
    private final Map<String , TranscodingStrategy> strategyMap;

    public TranscodingStrategyFactory(Map<String, TranscodingStrategy> strategyMap) {
        this.strategyMap = strategyMap;
    }
    public TranscodingStrategy get(String name){
        TranscodingStrategy s =strategyMap.get(name.toLowerCase());
        if (s==null){
            throw new IllegalArgumentException("未知转码"+name);
        }
        return s;
    }
}
