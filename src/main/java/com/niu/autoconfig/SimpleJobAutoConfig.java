package com.niu.autoconfig;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * Simple 任务自动配置类
 *
 * @author [nza]
 * @version 1.0 2020/12/20
 * @createTime 14:10
 */
@Configuration
@ConditionalOnBean(CoordinatorRegistryCenter.class)
@AutoConfigureAfter(ZookeeperAutoConfig.class)
@RequiredArgsConstructor
@Slf4j
public class SimpleJobAutoConfig extends BaseJobAutoConfig {

    private final ApplicationContext applicationContext;

    private final ZookeeperRegistryCenter zookeeperRegistryCenter;

    @Autowired(required = false)
    private DataSource dataSource;

    /**
     * @Description: 注册 SimpleJob
     * 构造方法之后执行
     * @Author: nza
     * @Date: 2020/12/20
     */
    @PostConstruct
    public void initSimpleJob() {

        // 获取标注了指定注解的类
        Map<String, Object> jobBeans = applicationContext.getBeansWithAnnotation(ElasticSimpleJob.class);

        jobBeans.forEach((name, bean) -> {
            Class<?>[] interfaces = bean.getClass().getInterfaces();
            for (Class<?> superInterface : interfaces) {

                // 实现了 SimpleJob 才注册
                if (superInterface == SimpleJob.class) {

                    // 获取 Job 的Class
                    Class<?> clazz = getJobClass(bean);

                    ElasticSimpleJob annotation = clazz.getAnnotation(ElasticSimpleJob.class);
                    String jobName = annotation.jobName();
                    String corn = annotation.corn();
                    int totalCount = annotation.shardingTotalCount();
                    boolean overwrite = annotation.overwrite();
                    Class<?> jobStrategy = annotation.jobStrategy();
                    boolean jobEvent = annotation.jobEvent();
                    Class<? extends ElasticJobListener>[] jobListeners = annotation.jobListener();
                    List<ElasticJobListener> jobListenerList = Lists.newArrayList();
                    if (jobListeners.length > 0) {
                        for (Class<? extends ElasticJobListener> item : jobListeners) {
                            try {
                                jobListenerList.add(item.getDeclaredConstructor().newInstance());
                            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
                                log.error("自动配置 SimpleJob 发送异常, 异常信息: ", e);
                            }
                        }
                    }

                    JobCoreConfiguration jcc = JobCoreConfiguration
                            .newBuilder(jobName, corn, totalCount)
                            .build();
                    SimpleJobConfiguration jtc = new SimpleJobConfiguration(jcc, clazz.getCanonicalName());

                    LiteJobConfiguration ljc = LiteJobConfiguration.newBuilder(jtc)
                            .overwrite(overwrite)
                            .jobShardingStrategyClass(jobStrategy.getCanonicalName())
                            .build();

                    SpringJobScheduler jobScheduler;
                    ElasticJobListener[] jobListenerArray = jobListenerList.toArray(new ElasticJobListener[0]);

                    // 判断是否开启了时间追踪
                    if (jobEvent && dataSource != null) {
                        JobEventConfiguration jec = new JobEventRdbConfiguration(dataSource);
                        jobScheduler = new SpringJobScheduler((ElasticJob) bean, zookeeperRegistryCenter, ljc, jec, jobListenerArray);
                    } else {
                        // 注册任务, 注意这里需要使用 SpringJobScheduler
                        jobScheduler = new SpringJobScheduler((ElasticJob) bean, zookeeperRegistryCenter, ljc, jobListenerArray);
                    }

                    jobScheduler.init();
                    break;
                }
            }

        });
    }
}
