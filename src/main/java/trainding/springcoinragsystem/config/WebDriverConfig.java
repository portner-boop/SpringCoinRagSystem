package trainding.springcoinragsystem.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class WebDriverConfig {

    @Bean(destroyMethod = "quit")
    @Lazy
    public WebDriver webDriver() {
        return new ChromeDriver();
    }
}