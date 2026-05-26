package com.campushub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({
        "com.campushub.user.mapper",
        "com.campushub.task.mapper",
        "com.campushub.order.mapper",
        "com.campushub.review.mapper",
        "com.campushub.message.mapper",
        "com.campushub.report.mapper"
})
public class CampusHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusHubApplication.class, args);
    }
}
