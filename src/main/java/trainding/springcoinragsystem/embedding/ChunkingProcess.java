package trainding.springcoinragsystem.embedding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import trainding.springcoinragsystem.entity.Article;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class ChunkingProcess {

    @Value("${chunk.sentences:4}")
    private int sentenceSize;

    @Value("${threads:5}")
    private int threadCount;

    @Autowired
    private ExecutorService executorService;
    @Autowired
    private EmbeddingService embeddingService;

    public List<Article> getArticlesWithChunks(List<Article> articles) {
        List<Future<List<Article>>> fullFillArticlesWithChunks =
                getFullFillArticleFromFutureList(articles);
        return parseArticlesFromFutureList(fullFillArticlesWithChunks);
    }

    private List<Article> parseArticlesFromFutureList(List<Future<List<Article>>> fullFillArticlesWithChunks) {
        List<Article> articlesWithChunks = new ArrayList<>();
        for(Future<List<Article>> fullFillArticlesWithChunk : fullFillArticlesWithChunks) {
            try {
                articlesWithChunks.addAll(fullFillArticlesWithChunk.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return articlesWithChunks;
    }

    private List<Future<List<Article>>> getFullFillArticleFromFutureList(List<Article> articles) {
        List<Future<List<Article>>> fullFillArticlesWithChunks = new ArrayList<>();
        int articleCount = articles.size();
        int articlePerThread = (int)Math.ceil((double) articleCount /threadCount);
        for(int i =0 ; i < articlePerThread ; i++) {
            int start = i * sentenceSize;
            int end = Math.min(start + sentenceSize, articlePerThread);
            Callable<List<Article>> tasks = new ChunkTask(articles,sentenceSize,embeddingService);
            fullFillArticlesWithChunks.add(executorService.submit(tasks));
        }

        return fullFillArticlesWithChunks;
    }

}
