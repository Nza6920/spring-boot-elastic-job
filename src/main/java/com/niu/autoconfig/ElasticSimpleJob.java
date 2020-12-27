package com.niu.autoconfig;

import com.dangdang.ddframe.job.lite.api.strategy.impl.AverageAllocationJobShardingStrategy;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Simple Job 注解
 *
 * @author [nza]
 * @version 1.0 2020/12/20
 * @createTime 14:04
 */
@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ElasticSimpleJob {

    /**
     * 任务名称
     */
    String jobName() default "";


    /**
     * corn 表达式
     */
    String corn() default "";

    /**
     * 分片总数
     */
    int shardingTotalCount() default 1;

    /**
     * 是否覆盖
     */
    boolean overwrite() default false;

    /**
     * 分片策略
     * 默认平均分配
     */
    Class<?> jobStrategy() default AverageAllocationJobShardingStrategy.class;

    /**
     * 是否开启时间追踪
     */
    boolean jobEvent() default false;
}

