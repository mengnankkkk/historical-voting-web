package com.historical.voting.user.Factory;

import com.historical.voting.user.entity.type.GraceType;
import com.historical.voting.user.strategy.GraceStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GraceStrategyFactory {
    private final Map<String, GraceStrategy> strategyMap =new HashMap<>();

    @Autowired
    public GraceStrategyFactory(List<GraceStrategy> strategyList){
        for (GraceStrategy strategy:strategyList){
            GraceType type = GraceType.valueOf(strategy.getClass().getAnnotation(Component.class).value());
            strategyMap.put(type.name(),strategy);
        }
    }
    public GraceStrategy getStrategy(GraceType type){
        return strategyMap.get(type.name());
    }
}
