package com.niu.autoconfig;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
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
public class DataFlowAutoConfig extends BaseJobAutoConfig {

    private final ApplicationContext applicationContext;

    private final ZookeeperRegistryCenter zookeeperRegistryCenter;

    @Autowired(required = false)
    private DataSource dataSource;

    /**
     * @Description: 注册 DataFlow 任务
     * 构造方法之后执行
     * @Author: nza
     * @Date: 2020/12/20
     */
    @PostConstruct
    public void initDataFlowJob() {

        // 获取标注了指定注解的类
        Map<String, Object> jobBeans = applicationContext.getBeansWithAnnotation(ElasticDataFlowJob.class);

        jobBeans.forEach((name, bean) -> {
            Class<?>[] interfaces = bean.getClass().getInterfaces();
            for (Class<?> superInterface : interfaces) {

                // 实现了 DataFlowJob 才注册
                if (superInterface == DataflowJob.class) {

                    // 获取 Job 的Class
                    Class<?> clazz = getJobClass(bean);

                    ElasticDataFlowJob annotation = clazz.getAnnotation(ElasticDataFlowJob.class);
                    String jobName = annotation.jobName();
                    String corn = annotation.corn();
                    int totalCount = annotation.shardingTotalCount();
                    boolean streamingProcess = annotation.streamingProcess();

                    JobCoreConfiguration jcc = JobCoreConfiguration
                            .newBuilder(jobName, corn, totalCount)
                            .build();
                    DataflowJobConfiguration jtc = new DataflowJobConfiguration(
                            jcc,
                            clazz.getCanonicalName(),
                            streamingProcess);

                    initJobScheduler((ElasticJob) bean, annotation, jtc, dataSource, zookeeperRegistryCenter);

                    break;
                }
            }
        });
    }
}
