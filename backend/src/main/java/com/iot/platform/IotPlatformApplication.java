package com.iot.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.iot.platform.mapper")
public class IotPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(IotPlatformApplication.class, args);
    }
}
