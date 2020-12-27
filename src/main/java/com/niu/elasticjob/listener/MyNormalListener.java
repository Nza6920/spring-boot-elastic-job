package com.niu.elasticjob.listener;

import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 普通监听器
 * 每次任务之前和之后执行
 *
 * @author [nza]
 * @version 1.0 2020/12/27
 * @createTime 17:32
 */
@Slf4j
public class MyNormalListener implements ElasticJobListener {
    @Override
    public void beforeJobExecuted(ShardingContexts contexts) {

        log.info("普通任务 before 监听器, 当前作业: {}", contexts.getJobName());
    }

    @Override
    public void afterJobExecuted(ShardingContexts contexts) {
        log.info("普通任务 after 监听器, 当前作业: {}", contexts.getJobName());
    }
}
