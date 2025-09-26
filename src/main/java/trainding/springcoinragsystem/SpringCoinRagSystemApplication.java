package trainding.springcoinragsystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import trainding.springcoinragsystem.embedding.ChunkingProcess;
import trainding.springcoinragsystem.entity.Article;
import trainding.springcoinragsystem.entity.Chunk;
import trainding.springcoinragsystem.parser.CoinTelegraphParser;

import java.util.List;

@SpringBootApplication
public class SpringCoinRagSystemApplication {

    private static final Logger log = LoggerFactory.getLogger(SpringCoinRagSystemApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringCoinRagSystemApplication.class, args);
    }

//    @Bean
//    public CommandLineRunner run(CoinTelegraphParser parserService, ChunkingProcess chunkingProcess) {
//        return args -> {
//            log.info("=== Начало парсинга ===");
//            List<Article> articles = parserService.parse();
//
//            List<Article> processedArticles = chunkingProcess.getArticlesWithChunks(articles);
//
//            for (Article article : processedArticles) {
//                log.info("=== Статья: {} ===", article.getTitle());
//                for (Chunk chunk : article.getChunks()) {
//                    log.info("Чанк: {}", chunk.getText());
//                    log.info("Эмбеддинг: {}", chunk.getEmbedding());
//                }
//            }
//
//            log.info("=== Парсинг и эмбеддинг завершены ===");
//        };
//    }

}
