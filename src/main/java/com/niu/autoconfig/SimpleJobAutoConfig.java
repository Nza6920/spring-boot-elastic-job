package com.niu.autoconfig;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
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
@AllArgsConstructor
public class SimpleJobAutoConfig {

    private final ApplicationContext applicationContext;

    private final ZookeeperRegistryCenter zookeeperRegistryCenter;

    /**
     * 初始化SimpleJob
     * 构造方法之后执行
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
                    ElasticSimpleJob annotation = bean.getClass().getAnnotation(ElasticSimpleJob.class);
                    String jobName = annotation.jobName();
                    String corn = annotation.corn();
                    int totalCount = annotation.shardingTotalCount();
                    boolean overwrite = annotation.overwrite();

                    JobCoreConfiguration jcc = JobCoreConfiguration
                            .newBuilder(jobName, corn, totalCount)
                            .build();
                    SimpleJobConfiguration jtc = new SimpleJobConfiguration(jcc, bean.getClass().getCanonicalName());

                    LiteJobConfiguration ljc = LiteJobConfiguration.newBuilder(jtc)
                            .overwrite(overwrite)
                            .build();

                    // 注册任务
                    new JobScheduler(zookeeperRegistryCenter, ljc).init();

                    break;
                }
            }

        });
    }
}
