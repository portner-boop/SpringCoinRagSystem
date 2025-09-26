package trainding.springcoinragsystem.repository;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Repository;
import trainding.springcoinragsystem.entity.Article;
import trainding.springcoinragsystem.entity.Chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
public class QdrantRepostory implements ArticleRepository {

    private final VectorStore vectorStore;
    private static final Logger log = LoggerFactory.getLogger(QdrantRepostory.class);

    public void saveArticles(List<Article> articles) {
        List<Document> documents = new ArrayList<>();
        for (Article article : articles) {
            String title = article.getTitle();
            List<Document> existingDocs = vectorStore.similaritySearch(title);
            Set<Integer> existingChunkIndices = existingDocs.stream()
                    .filter(d -> title.equals(d.getMetadata().get("article_title")))
                    .map(d -> ((Long) d.getMetadata().get("chunk_index")).intValue())
                    .collect(Collectors.toSet());
            for (int i = 0; i < article.getChunks().size(); i++) {
                if (!existingChunkIndices.contains(i)) {
                    Chunk chunk = article.getChunks().get(i);

                    Document doc = new Document(chunk.getText(), Map.of(
                                    "article_title", title,
                                    "article_text", article.getText(),
                                    "chunk_index", i)
                    );
                    documents.add(doc);
                }
            }
        vectorStore.add(documents);
        log.info("Сохранено документов: " + documents.size());
        }
    }


    public List<Document> searchSimilar(String query){
        return vectorStore.similaritySearch(query);
    }
}
