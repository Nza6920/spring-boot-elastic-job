package com.niu.autoconfig;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
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
public class SimpleJobAutoConfig extends BaseJobAutoConfig {

    private final ApplicationContext applicationContext;

    private final ZookeeperRegistryCenter zookeeperRegistryCenter;


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

                    JobCoreConfiguration jcc = JobCoreConfiguration
                            .newBuilder(jobName, corn, totalCount)
                            .build();
                    SimpleJobConfiguration jtc = new SimpleJobConfiguration(jcc, clazz.getCanonicalName());

                    LiteJobConfiguration ljc = LiteJobConfiguration.newBuilder(jtc)
                            .overwrite(overwrite)
                            .build();

                    // 注册任务, 注意这里需要使用 SpringJobScheduler
                    new SpringJobScheduler((ElasticJob) bean, zookeeperRegistryCenter, ljc).init();

                    break;
                }
            }

        });
    }
}
