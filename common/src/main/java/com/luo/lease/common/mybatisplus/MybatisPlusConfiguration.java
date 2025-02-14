package com.luo.lease.common.mybatisplus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.luo.lease.web.*.mapper")
public class MybatisPlusConfiguration {

}