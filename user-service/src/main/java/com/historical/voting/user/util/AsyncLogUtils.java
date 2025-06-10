package com.historical.voting.user.util;

import com.historical.voting.user.dto.LogDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class AsyncLogUtils {
    @Autowired
    private Executor asyncExecutor;
    public void record(LogDTO logDTO){
        asyncExecutor.execute(()->{
            try {
                log.info("记录日志:{}",logDTO);
            } catch (Exception e) {
                log.error("记录失败",logDTO);
            }

        });
    }

}
