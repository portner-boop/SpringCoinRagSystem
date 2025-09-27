package trainding.springcoinragsystem.parser.page;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class BasePageConfig {

    protected final WebDriver driver;

    @Autowired
    public BasePageConfig( WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }
}
