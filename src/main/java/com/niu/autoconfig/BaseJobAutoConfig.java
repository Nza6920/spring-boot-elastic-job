package com.niu.autoconfig;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * 自动配置基类
 *
 * @author [nza]
 * @version 1.0 2020/12/26
 * @createTime 23:20
 */
@Slf4j
public class BaseJobAutoConfig {

    /**
     * 获取 Class
     *
     * @param bean 类型
     * @return 目标类
     */
    protected Class<?> getJobClass(Object bean) {
        Class<?> clazz;
        if (AopUtils.isAopProxy(bean)) {
            clazz = AopUtils.getTargetClass(bean);
        } else {
            clazz = bean.getClass();
        }
        return clazz;
    }

    /**
     * 初始化 job scheduler
     *
     * @param bean       任务
     * @param annotation 注解
     * @param jobConfig  Job配置
     * @param dataSource 数据源
     * @param zkCenter   配置中心
     * @author nza
     * @createTime 2021/1/7 10:00
     */
    protected void initJobScheduler(ElasticJob bean, Annotation annotation, JobTypeConfiguration jobConfig, DataSource dataSource, ZookeeperRegistryCenter zkCenter) {

        boolean overwrite;
        Class<?> jobStrategy;
        boolean jobEvent;
        Class<? extends ElasticJobListener>[] jobListeners;

        if (annotation instanceof ElasticSimpleJob) {
            overwrite = ((ElasticSimpleJob) annotation).overwrite();
            jobStrategy = ((ElasticSimpleJob) annotation).jobStrategy();
            jobEvent = ((ElasticSimpleJob) annotation).jobEvent();
            jobListeners = ((ElasticSimpleJob) annotation).jobListener();
        } else if (annotation instanceof ElasticDataFlowJob) {
            overwrite = ((ElasticDataFlowJob) annotation).overwrite();
            jobStrategy = ((ElasticDataFlowJob) annotation).jobStrategy();
            jobEvent = ((ElasticDataFlowJob) annotation).jobEvent();
            jobListeners = ((ElasticDataFlowJob) annotation).jobListener();
        } else {
            throw new IllegalArgumentException("非法的任务类型");
        }

        List<ElasticJobListener> jobListenerList = Lists.newArrayList();
        if (jobListeners.length > 0) {
            for (Class<? extends ElasticJobListener> item : jobListeners) {
                try {
                    jobListenerList.add(item.getDeclaredConstructor().newInstance());
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
                    log.error("自动配置 ElasticJob 发生异常, 异常信息: ", e);
                }
            }
        }

        LiteJobConfiguration ljc = LiteJobConfiguration.newBuilder(jobConfig)
                .overwrite(overwrite)
                .jobShardingStrategyClass(jobStrategy.getCanonicalName())
                .build();

        SpringJobScheduler jobScheduler;
        ElasticJobListener[] jobListenerArray = jobListenerList.toArray(new ElasticJobListener[0]);

        // 判断是否开启了事件追踪
        if (jobEvent && dataSource != null) {
            JobEventConfiguration jec = new JobEventRdbConfiguration(dataSource);
            jobScheduler = new SpringJobScheduler(bean, zkCenter, ljc, jec, jobListenerArray);
        } else {
            // 注册任务, 注意这里需要使用 SpringJobScheduler
            jobScheduler = new SpringJobScheduler(bean, zkCenter, ljc, jobListenerArray);
        }

        jobScheduler.init();
    }
}
