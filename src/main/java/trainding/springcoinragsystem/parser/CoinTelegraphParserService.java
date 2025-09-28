package trainding.springcoinragsystem.parser;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import trainding.springcoinragsystem.entity.Article;
import trainding.springcoinragsystem.parser.page.CoinTelegraphMainPage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class CoinTelegraphParserService {

    private static final Logger logger = LoggerFactory.getLogger(CoinTelegraphParserService.class);
    private final CoinTelegraphMainPage coinTelegraphMainPage;
    private final ExecutorService executorService;
    @Value("${coin-telegraph:5}")
    private int threadCount;

    public List<Article> parse() throws InterruptedException {
        logger.info("=== Старт парсинга CoinTelegraph ===");
        Document doc = Jsoup.parse(coinTelegraphMainPage.getPageHTML());
        Elements elements = doc.getElementsByAttributeValue("data-testid", "post-card-header");
        int total = elements.size();
        logger.info("Найдено {} статей на главной странице", total);
        List<Article> articles = fillTitleAndLinkForArticle(elements);
        List<Future<List<Article>>> tasks = getFullFillArticlesWithoutChunks(articles);
        return listConverterFutureToCommon(tasks);
    }

    private List<Article> fillTitleAndLinkForArticle(Elements elements) {
        List<Article> articles = new ArrayList<>();
        for (Element el : elements) {
            Element linkElement = el.child(0);
            Article article = new Article();
            article.setTitle(linkElement.attr("title"));
            article.setLink(linkElement.attr("href"));
            articles.add(article);
        }
        return articles;
    }

    private List<Future<List<Article>>> getFullFillArticlesWithoutChunks(List<Article> articles) {
        List<Future<List<Article>>> futures = new ArrayList<>();
        int totalArticles = articles.size();
        int articlesPerThread = (int) Math.ceil((double) totalArticles / threadCount);
        for (int i = 0; i < threadCount; i++) {
            int startIndex = i * articlesPerThread;
            int endIndex = Math.min(startIndex + articlesPerThread, totalArticles);
            if (startIndex >= endIndex) break;
            Callable<List<Article>> task = new ArticleParserTask(articles.subList(startIndex, endIndex));
            futures.add(executorService.submit(task));
        }
        return futures;
    }

    private List<Article> listConverterFutureToCommon(List<Future<List<Article>>> tasks) {
        List<Article> result = new ArrayList<>();
        int processed = 0;
        for (Future<List<Article>> task : tasks) {
            try {
                List<Article> parsed = task.get();
                result.addAll(parsed);
                processed += parsed.size();
                logger.info("Прогресс: {}", processed);
            } catch (ExecutionException | InterruptedException e) {
                logger.error("Ошибка при обработке задач", e);
                Thread.currentThread().interrupt();
            }
        }
        logger.info("=== Парсинг завершён. Получено {} статей ===", result.size());
        return result;
    }
}


