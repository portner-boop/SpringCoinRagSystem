package trainding.springcoinragsystem.qdrant;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Repository;
import trainding.springcoinragsystem.entity.Article;

import java.util.List;

@Repository
public interface ArticleRepository {
    public void saveArticles(List<Article> articles);

    public List<Document> searchSimilar(String query);

    public List<Document> searchSimilar(SearchRequest query);
}
