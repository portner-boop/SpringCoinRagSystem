package trainding.springcoinragsystem.chunking;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import trainding.springcoinragsystem.entity.Article;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class ChunkingService {

    private final ExecutorService executorService;
    @Value("${threads:4}")
    private int threadCount;
    @Value("${chunk.sentences}")
    private int sentencePerChunk;

    public List<Article> getArticlesWithChunks(List<Article> articles) {
        List<Future<List<Article>>> fullFillArticlesWithChunks =
                getFullFillArticleFromFutureList(articles);
        return listConverterFutureToCommon(fullFillArticlesWithChunks);
    }

    private List<Article> listConverterFutureToCommon(List<Future<List<Article>>> fullFillArticlesWithChunks) {
        List<Article> articlesWithChunks = new ArrayList<>();
        for (Future<List<Article>> fullFillArticlesWithChunk : fullFillArticlesWithChunks) {
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
        int articlePerThread = (int) Math.ceil((double) articleCount / threadCount);
        for (int i = 0; i < threadCount; i++) {
            int start = i * articlePerThread;
            int end = Math.min(start + articlePerThread, articleCount);
            if (start >= end) break;
            List<Article> subList = articles.subList(start, end);
            ChunkTask task = new ChunkTask(subList, sentencePerChunk);
            fullFillArticlesWithChunks.add(executorService.submit(task));
        }
        return fullFillArticlesWithChunks;
    }
}
