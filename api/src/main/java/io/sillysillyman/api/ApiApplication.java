package io.sillysillyman.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "io.sillysillyman.core.domain")
@ComponentScan(basePackages = {
    "io.sillysillyman.api",
    "io.sillysillyman.core"
})
@EnableJpaRepositories(basePackages = "io.sillysillyman.core.domain")
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

}
