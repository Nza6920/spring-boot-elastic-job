package com.niu.autoconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Zookeeper 配置类
 *
 * @author [nza]
 * @version 1.0 2020/12/19
 * @createTime 23:01
 */
@Data
@ConfigurationProperties(prefix = "elastic.zookeeper")
public class ZookeeperProperties {

    /**
     * 地址列表
     */
    private String serverList;

    /**
     * 命名空间
     */
    private String namespace;
}
