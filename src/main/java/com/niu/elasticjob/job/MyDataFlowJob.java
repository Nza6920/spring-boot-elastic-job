package com.niu.elasticjob.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.niu.autoconfig.ElasticDataFlowJob;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * DataFlow Job
 *
 * @author [nza]
 * @version 1.0 2020/12/20
 * @createTime 15:47
 */
@Slf4j
@ElasticDataFlowJob(jobName = "myDataFlowJob",
        corn = "0/10 * * * * ?",
        shardingTotalCount = 2,
        overwrite = true,
        streamingProcess = true
)
public class MyDataFlowJob implements DataflowJob<Integer> {

    /**
     * 数据源
     */
    private static final List<Integer> DATA;

    static {
        DATA = new ArrayList<>();
        for (int i = 100; i > 0; i--) {
            DATA.add(i);
        }
    }

    @Override
    public List<Integer> fetchData(ShardingContext context) {
        // 数字 % 分片总数 == 当前分片项
        List<Integer> res = new ArrayList<>();

        for (Integer item : DATA) {
            if (item % context.getShardingTotalCount() == context.getShardingItem()) {
                res.add(item);
                break;
            }
        }

        log.info("我是分片项: {}, 我拉取的数据是: {}", context.getShardingItem(), res);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return res;
    }

    @Override
    public void processData(ShardingContext context, List<Integer> data) {

        // 将处理过的数据从数据源中移除
        DATA.removeAll(data);

        log.info("我是分片项: {}, 我移除的数据时: {}", context.getShardingItem(), data);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
