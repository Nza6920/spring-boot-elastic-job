package com.niu.elasticjob.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.niu.autoconfig.ElasticSimpleJob;
import com.niu.elasticjob.sharding.MyShardingStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义分片策略 Job
 *
 * @author [nza]
 * @version 1.0 2020/12/27
 * @createTime 15:54
 */
@Slf4j
//@ElasticSimpleJob(jobName = "customShardingJob",
//        corn = "0/10 * * * * ?",
//        shardingTotalCount = 3,
//        overwrite = true,
//        jobStrategy = MyShardingStrategy.class,
//        jobEvent = true
//)
public class CustomShardingJob implements SimpleJob {
    @Override
    public void execute(ShardingContext context) {
        log.info("执行自定义分片策略任务, 总分片数: {}, 当前分片项: {}",
                context.getShardingTotalCount(),
                context.getShardingItem());
    }
}
