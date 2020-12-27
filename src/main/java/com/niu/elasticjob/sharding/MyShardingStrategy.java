package com.niu.elasticjob.sharding;

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategy;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;

/**
 * 自定义分片策略
 * 轮询策略
 *
 * @author [nza]
 * @version 1.0 2020/12/27
 * @createTime 15:31
 */
public class MyShardingStrategy implements JobShardingStrategy {

    @Override
    public Map<JobInstance, List<Integer>> sharding(List<JobInstance> jobs,
                                                    String jobName,
                                                    int totalCount) {

        Map<JobInstance, List<Integer>> res = Maps.newHashMap();

        // 轮询队列
        ArrayDeque<Integer> queue = Queues.newArrayDeque();
        for (int i = 0; i < totalCount; i++) {
            queue.add(i);
        }

        while (!queue.isEmpty()) {
            // 遍历所有 job 实例
            jobs.forEach(job -> {
                if (queue.isEmpty()) {
                    return;
                }

                // 取出一个分片项
                Integer shardingItem = queue.pop();

                // 获取对应 job 的任务列表
                List<Integer> jobList = res.get(job);
                if (jobList == null) {
                    jobList = Lists.newArrayList();
                }
                jobList.add(shardingItem);

                res.put(job, jobList);
            });
        }

        return res;
    }
}
