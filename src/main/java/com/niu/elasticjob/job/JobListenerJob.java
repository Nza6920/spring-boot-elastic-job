package com.niu.elasticjob.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.niu.autoconfig.ElasticSimpleJob;
import com.niu.elasticjob.listener.MyNormalListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务事件监听器
 *
 * @author [nza]
 * @version 1.0 2020/12/27
 * @createTime 17:37
 */
@Slf4j
@ElasticSimpleJob(jobName = "jobListenerJob",
        corn = "0/5 * * * * ?",
        shardingTotalCount = 3,
        overwrite = true,
        jobListener = MyNormalListener.class
)
public class JobListenerJob implements SimpleJob {
    @Override
    public void execute(ShardingContext context) {
        log.info("执行 JobListenerJob, 分片总数: {}, 当前分片: {}", context.getShardingTotalCount(), context.getShardingItem());
    }
}
