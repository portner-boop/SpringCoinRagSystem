package trainding.springcoinragsystem.embedding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import trainding.springcoinragsystem.entity.Article;
import trainding.springcoinragsystem.entity.Chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static trainding.springcoinragsystem.embedding.StringPreparing.cleanText;

public class ChunkTask implements Callable<List<Article>> {

    private static final Logger log = LoggerFactory.getLogger(ChunkTask.class);

    private final List<Article> articles;
    private final int sentencesPerChunk;

    public ChunkTask(List<Article> articles, int sentencesPerChunk) {
        this.articles = articles;
        this.sentencesPerChunk = sentencesPerChunk;
    }

    @Override
    public List<Article> call() throws Exception {
        List<Article> processedArticles = new ArrayList<>();

        for (Article article : articles) {
            Article articleWithChunks = fillArticleWithChunk(article);
            processedArticles.add(articleWithChunks);
            log.info("Обработана статья: {} | Чанков: {}", article.getTitle(), articleWithChunks.getChunks().size());
        }

        return processedArticles;
    }

    private Article fillArticleWithChunk(Article article) {
        String cleanText = cleanText(article.getText());
        article.setChunks(makeArrayOfChunks(cleanText));
        return article;
    }

    private List<Chunk> makeArrayOfChunks(String cleanText) {
        List<Chunk> chunks = new ArrayList<>();
        String[] sentences = cleanText.split("[.!?;:]");
        StringBuilder chunkString = new StringBuilder();
        int counter = 0;
        int chunkIndex = 1;

        for (int i = 0; i < sentences.length; i++) {
            chunkString.append(sentences[i]);
            counter++;

            if (counter == sentencesPerChunk || i == sentences.length - 1) {
                Chunk chunk = new Chunk();
                String chunkText = chunkString.toString().trim();
                chunk.setText(chunkText);
                chunks.add(chunk);
                chunkString = new StringBuilder();
                counter = 0;
                chunkIndex++;
            }
        }
        return chunks;
    }
}
