package trainding.springcoinragsystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import trainding.springcoinragsystem.embedding.ChunkingProcess;
import trainding.springcoinragsystem.entity.Article;
import trainding.springcoinragsystem.parser.CoinTelegraphParser;
import trainding.springcoinragsystem.repository.ArticleRepository;
import trainding.springcoinragsystem.repository.QdrantRepostory;
import trainding.springcoinragsystem.repository.Qdrantservice;

import java.util.List;

@SpringBootApplication
public class SpringCoinRagSystemApplication {

    private static final Logger log = LoggerFactory.getLogger(SpringCoinRagSystemApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringCoinRagSystemApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(
            CoinTelegraphParser parserService,
            ChunkingProcess chunkingProcess,
            Qdrantservice articleVectorService
    ) {
        return args -> {
            log.info("=== Начало парсинга и векторизации ===");
            List<Article> articles = parserService.parse();
            List<Article> processedArticles = chunkingProcess.getArticlesWithChunks(articles);
            articleVectorService.saveArticles(processedArticles);
            log.info("=== Обработано {} статей ===", processedArticles.size());
            List<Document> results = articleVectorService.search("криптовалюта");
            results.forEach(doc ->
                    log.info("Найдено: {} - {}",
                            doc.getMetadata().get("article_title"),
                            doc.getText().substring(0, 100) + "...")
            );
        };
    }

}
