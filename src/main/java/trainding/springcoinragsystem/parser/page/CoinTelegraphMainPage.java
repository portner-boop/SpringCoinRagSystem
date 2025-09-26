package trainding.springcoinragsystem.parser.page;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class CoinTelegraphMainPage extends BasePageConfig {

    public static final String COIN_TELEGRAPH_MAIN_PAGE = "https://ru.cointelegraph.com/";
    private final int timeToScroll;

    public CoinTelegraphMainPage(WebDriver driver,
                                 @Value("${coin-telegraph.parsing.time:3000}") int timeToScroll) {
        super(driver);
        this.timeToScroll = timeToScroll;
    }

    public String getPageHTML() throws InterruptedException {
        driver.get(COIN_TELEGRAPH_MAIN_PAGE);
        Thread.sleep(10000);
        moderateTimedScroll();
        return driver.getPageSource();
    }

    private void moderateTimedScroll() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        int scrollInterval = 200;
        while (elapsedTime < timeToScroll) {
            js.executeScript("window.scrollBy(0, window.innerHeight * 1.2);");
            try {
                Thread.sleep(scrollInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            elapsedTime = System.currentTimeMillis() - startTime;
        }
    }
}