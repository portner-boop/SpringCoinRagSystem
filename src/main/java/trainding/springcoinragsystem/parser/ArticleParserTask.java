package trainding.springcoinragsystem.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import trainding.springcoinragsystem.entity.Article;
import trainding.springcoinragsystem.parser.page.CoinTelegraphMainPage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Component
class ArticleParserTask implements Callable<List<Article>> {

    private final List<Article> articles;

    ArticleParserTask(List<Article> articles) {
        this.articles = articles;
    }

    @Override
    public List<Article> call() {
        List<Article> parsedArticles = new ArrayList<>();
        for (Article article : articles) {
            try {
                Document articleDoc = Jsoup.connect(CoinTelegraphMainPage.COIN_TELEGRAPH_MAIN_PAGE + article.getLink()).get();
                Elements contentElements = articleDoc.getElementsByClass("post-content relative post-content_margin");
                if (!contentElements.isEmpty()) {
                    String text = contentElements.getFirst().text();
                    article.setText(text);
                }
                parsedArticles.add(article);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return parsedArticles;
    }
}
