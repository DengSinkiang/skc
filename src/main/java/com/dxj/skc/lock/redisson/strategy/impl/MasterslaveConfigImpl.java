package com.dxj.skc.lock.redisson.strategy.impl;

import com.dxj.skc.lock.redisson.enumeration.RedisConstantEnum;
import com.dxj.skc.lock.redisson.entity.RedissonProperties;
import com.dxj.skc.lock.redisson.strategy.RedissonConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.config.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 主从部署 Redisson 配置
 * 连接方式: 主节点,子节点,子节点
 * 格式为: 127.0.0.1:6379,127.0.0.1:6380,127.0.0.1:6381
 * @author: sinkiang
 * @date: 2022/4/7 10:32
 */
@Slf4j
public class MasterslaveConfigImpl implements RedissonConfigService {

    @Override
    public Config createRedissonConfig(RedissonProperties redissonProperties) {
        Config config = new Config();
        try {
            String address = redissonProperties.getAddress();
            String password = redissonProperties.getPassword();
            int database = redissonProperties.getDatabase();
            String[] addrTokens = address.split(",");
            String masterNodeAddr = addrTokens[0];
            // 设置主节点ip
            config.useMasterSlaveServers().setMasterAddress(masterNodeAddr);
            if (StringUtils.isNotBlank(password)) {
                config.useMasterSlaveServers().setPassword(password);
            }
            config.useMasterSlaveServers().setDatabase(database);
            // 设置从节点，移除第一个节点，默认第一个为主节点
            List<String> slaveList = new ArrayList<>();
            for (String addrToken : addrTokens) {
                slaveList.add(RedisConstantEnum.REDIS_CONNECTION_PREFIX.getConstantValue() + addrToken);
            }
            slaveList.remove(0);
            String[] strings = new String[slaveList.size()];
            config.useMasterSlaveServers().addSlaveAddress(slaveList.toArray(strings));
            log.info("初始化[主从部署]方式Config,redisAddress:" + address);
        } catch (Exception e) {
            log.error("主从部署 Redisson init error", e);
            e.printStackTrace();
        }
        return config;
    }

}
