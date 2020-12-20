package com.niu.autoconfig;

import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Zookeeper 自动配置
 *
 * @author [nza]
 * @version 1.0 2020/12/19
 * @createTime 23:17
 */
@Configuration
@ConditionalOnProperty("elastic.zookeeper.server-list")
@EnableConfigurationProperties({ZookeeperProperties.class})
@AllArgsConstructor
public class ZookeeperAutoConfig {

    private final ZookeeperProperties zookeeperProperties;

    @Bean(initMethod = "init")
    public CoordinatorRegistryCenter zkCenter() {

        String serverList = zookeeperProperties.getServerList();
        String nameSpace = zookeeperProperties.getNamespace();

        ZookeeperConfiguration zc = new ZookeeperConfiguration(serverList, nameSpace);

        return new ZookeeperRegistryCenter(zc);
    }
}
