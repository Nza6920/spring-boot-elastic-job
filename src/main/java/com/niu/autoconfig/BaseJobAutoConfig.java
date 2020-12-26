package com.niu.autoconfig;

import org.springframework.aop.support.AopUtils;

/**
 * 自动配置基类
 *
 * @author [nza]
 * @version 1.0 2020/12/26
 * @createTime 23:20
 */
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
}
