package trainding.springcoinragsystem.parser.page;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class BasePageConfig {

    protected final WebDriver driver;

    public BasePageConfig(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }
}
