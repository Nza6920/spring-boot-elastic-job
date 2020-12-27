package com.niu.elasticjob.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.niu.autoconfig.ElasticSimpleJob;
import lombok.extern.slf4j.Slf4j;

/**
 * 简单任务
 *
 * @author [nza]
 * @version 1.0 2020/12/20
 * @createTime 14:02
 */
@Slf4j
//@ElasticSimpleJob(jobName = "mySimpleJob",
//        corn = "0/10 * * * * ?",
//        shardingTotalCount = 2,
//        overwrite = true
//)
public class MySimpleJob implements SimpleJob {
    @Override
    public void execute(ShardingContext context) {
        log.info("我是分片项: {}, 总分片数: {}", context.getShardingItem(), context.getShardingTotalCount());
    }
}
