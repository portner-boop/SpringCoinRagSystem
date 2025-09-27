package trainding.springcoinragsystem.qdrant;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Repository;
import trainding.springcoinragsystem.entity.Article;
import trainding.springcoinragsystem.entity.Chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Repository
@RequiredArgsConstructor
public class QdrantRepostory implements ArticleRepository {

    private static final Logger log = LoggerFactory.getLogger(QdrantRepostory.class);
    private final VectorStore vectorStore;

    public void saveArticles(List<Article> articles) {
        List<Document> documents = new ArrayList<>();
        for (Article article : articles) {
            int articleSaved = 0;
            String title = article.getTitle();
            for (int i = 0; i < article.getChunks().size(); i++) {
                Chunk chunk = article.getChunks().get(i);
                Document doc = new Document(chunk.getText(), Map.of(
                        "article_title", title,
                        "article_text", article.getText(),
                        "chunk_index", i)
                );
                documents.add(doc);
                articleSaved++;
            }
            log.info("Статья '{}': сохранено {} чанков",
                    title, articleSaved);
        }
        vectorStore.add(documents);
    }

    public List<Document> searchSimilar(String query) {
        return vectorStore.similaritySearch(query);
    }

    public List<Document> searchSimilar(SearchRequest query) {
        return vectorStore.similaritySearch(query);
    }
}
