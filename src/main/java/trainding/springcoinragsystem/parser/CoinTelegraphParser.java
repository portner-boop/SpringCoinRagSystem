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
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class CoinTelegraphParser {
    @Value("${coin-telegraph:5}")
    private  int threadCount;
    private final CoinTelegraphMainPage coinTelegraphMainPage;
    private final ExecutorService executorService;
    private static final Logger logger = LoggerFactory.getLogger(CoinTelegraphParser.class);


    public List<Article> parse() throws InterruptedException {
        Document doc = Jsoup.parse(coinTelegraphMainPage.getPageHTML());
        Elements elements = doc.getElementsByAttributeValue("data-testid", "post-card-header");
        List<Article> articles = fillTitleAndLinkForArticle(elements);
        List<Future<List<Article>>> fullFillArticles = getFullFillArticlesWithoutChunksInFutureList(articles);
        return parseArticlesFromFutureList(fullFillArticles);
    }

    public void showAllArticles(List<Article> articles) {
        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            logger.info("Статья {}: {}", i + 1, article.getTitle());
            logger.info("Ссылка: {}", article.getLink());
            logger.info("Текст: {}", article.getText());
            logger.info("---");
        }
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

    private List<Future<List<Article>>> getFullFillArticlesWithoutChunksInFutureList(List<Article> articles) {
        List<Future<List<Article>>> fullFillArticlesWithoutChunks = new ArrayList<>();
        int totalArticles = articles.size();
        int articlesPerThread = (int) Math.ceil((double) totalArticles / threadCount);
        for (int i = 0; i < threadCount; i++) {
            int startIndex = i * articlesPerThread;
            final int endIndex = Math.min(startIndex + articlesPerThread, totalArticles);
            Callable<List<Article>> task = new ArticleParserTask(articles.subList(startIndex, endIndex));
            fullFillArticlesWithoutChunks.add(executorService.submit(task));
        }
        return fullFillArticlesWithoutChunks;
    }

    private List<Article> parseArticlesFromFutureList(List<Future<List<Article>>> futureList) {
        List<Article> articles = new ArrayList<>();
        for (Future<List<Article>> future : futureList) {
            try {
                articles.addAll(future.get());
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return articles;
    }
}

