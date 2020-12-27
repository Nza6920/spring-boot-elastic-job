package com.niu.elasticjob.listener;

import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 分布式任务监听器(不推荐)
 *
 * @author [nza]
 * @version 1.0 2020/12/27
 * @createTime 18:01
 */
@Slf4j
public class MyDistributeListener extends AbstractDistributeOnceElasticJobListener {

    public MyDistributeListener(long startedTimeoutMilliseconds, long completedTimeoutMilliseconds) {
        super(startedTimeoutMilliseconds, completedTimeoutMilliseconds);
    }

    @Override
    public void doBeforeJobExecutedAtLastStarted(ShardingContexts contexts) {
        log.info("分布式任务 before 监听器, 当前作业: {}", contexts.getJobName());
    }

    @Override
    public void doAfterJobExecutedAtLastCompleted(ShardingContexts contexts) {
        log.info("分布式任务 after 监听器, 当前作业: {}", contexts.getJobName());
    }
}
