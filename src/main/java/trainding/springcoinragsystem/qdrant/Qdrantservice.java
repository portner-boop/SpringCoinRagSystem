package trainding.springcoinragsystem.qdrant;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;
import trainding.springcoinragsystem.entity.Article;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Qdrantservice {

    private final QdrantRepostory QdrantRepostory;

    public void saveArticles(List<Article> articles) {
        QdrantRepostory.saveArticles(articles);
    }

    public List<Document> search(String query) {
        return QdrantRepostory.searchSimilar(query);
    }

    public List<Document> search(SearchRequest searchRequest) {
        return QdrantRepostory.searchSimilar(searchRequest);
    }

}
