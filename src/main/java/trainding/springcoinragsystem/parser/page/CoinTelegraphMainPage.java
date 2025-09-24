package trainding.springcoinragsystem.parser.page;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

@Component
public class CoinTelegraphMainPage extends BasePageConfig {

    public static final String COIN_TELEGRAPH_MAIN_PAGE = "https://ru.cointelegraph.com/";

    public CoinTelegraphMainPage(WebDriver driver) {
        super(driver);
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
        int timeToScroll = 20000;
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