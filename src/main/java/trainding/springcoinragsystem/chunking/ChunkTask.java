package trainding.springcoinragsystem.chunking;

import lombok.extern.slf4j.Slf4j;
import trainding.springcoinragsystem.entity.Article;
import trainding.springcoinragsystem.entity.Chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static trainding.springcoinragsystem.chunking.StringPreparing.cleanText;

@Slf4j
public class ChunkTask implements Callable<List<Article>> {

    private final List<Article> articles;

    private final int sentencesPerChunk;


    public ChunkTask(List<Article> articles,
                     int sentencesPerChunk) {
        this.articles = articles;
        this.sentencesPerChunk = sentencesPerChunk;
    }

    @Override
    public List<Article> call() {
        List<Article> processedArticles = new ArrayList<>();
        for (Article article : articles) {
            Article articleWithChunks = fillArticleWithChunks(article);
            processedArticles.add(articleWithChunks);
            log.info("Обработана статья: {} | Чанков: {}", article.getTitle(), articleWithChunks.getChunks().size());
        }
        return processedArticles;
    }

    private Article fillArticleWithChunks(Article article) {
        String text = cleanText(article.getText());
        List<Chunk> chunks = makeArrayOfChunks(text);
        article.setChunks(chunks);
        return article;
    }

    private List<Chunk> makeArrayOfChunks(String cleanText) {
        List<Chunk> chunks = new ArrayList<>();
        String[] sentences = cleanText.split("[.!?;]");
        StringBuilder chunkString = new StringBuilder();
        int counter = 0;
        int chunkIndex = 0;
        for (int i = 0; i < sentences.length; i++) {
            String cleanSentence = StringPreparing.removeStopWords(sentences[i]);
            chunkString.append(" ");
            chunkString.append(cleanSentence);
            counter++;
            if (counter == sentencesPerChunk || i == sentences.length - 1) {
                Chunk chunk = new Chunk();
                String chunkText = chunkString.toString().trim();
                chunk.setText(chunkText);
                chunk.setIndex(chunkIndex);
                chunks.add(chunk);
                chunkString = new StringBuilder();
                counter = 0;
                chunkIndex++;
            }
        }
        return chunks;
    }
}
