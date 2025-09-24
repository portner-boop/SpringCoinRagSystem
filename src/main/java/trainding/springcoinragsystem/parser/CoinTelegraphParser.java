package trainding.springcoinragsystem.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.chrome.ChromeDriver;
import trainding.springcoinragsystem.entity.Article;
import trainding.springcoinragsystem.parser.page.CoinTelegraphMainPage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CoinTelegraphParser {

    private static final int threadCount = 5;
    private static CoinTelegraphMainPage coinTelegraphMainPage;
    private static ExecutorService executorService;

    public CoinTelegraphParser() {
        coinTelegraphMainPage = new CoinTelegraphMainPage(new ChromeDriver());
        executorService = Executors.newFixedThreadPool(threadCount);
    }


    public static void main(String[] args) throws InterruptedException {
        Document doc = Jsoup.parse(coinTelegraphMainPage.getPageHTML());
        Elements elements = doc.getElementsByAttributeValue("data-testid", "post-card-header");
        List<Article> articles = new ArrayList<>();
        for (Element el : elements) {
            Element linkElement = el.child(0);
            Article article = new Article();
            article.setTitle(linkElement.attr("title"));
            article.setLink(linkElement.attr("href"));
            articles.add(article);
        }
        int totalArticles = articles.size();
        int articlesPerThread = (int) Math.ceil((double) totalArticles / threadCount);
        List<Future<List<Article>>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            int startIndex = i * articlesPerThread;
            final int endIndex = Math.min(startIndex + articlesPerThread, totalArticles);
            Callable<List<Article>> task = new ArticleParserTask(articles.subList(startIndex, endIndex));
            futures.add(executorService.submit(task));
        }
        List<Article> resultArticles = new ArrayList<>();
        for (Future<List<Article>> future : futures) {
            try {
                resultArticles.addAll(future.get());
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        executorService.shutdown();
        resultArticles.forEach(article -> {
            System.out.println("Title: " + article.getTitle());
            System.out.println("Link: " + article.getLink());
            System.out.println("Text: " + article.getText());
            System.out.println("---");
        });
    }
}

