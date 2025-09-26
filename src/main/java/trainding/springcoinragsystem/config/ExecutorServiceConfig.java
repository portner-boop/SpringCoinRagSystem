package trainding.springcoinragsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorServiceConfig {

    @Value("${threads:5}")
    private int corePoolSize;

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(corePoolSize);
    }

}
