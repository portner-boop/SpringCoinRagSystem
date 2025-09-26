package trainding.springcoinragsystem.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import trainding.springcoinragsystem.entity.Article;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Qdrantservice {

    private final QdrantRepostory repository;

    public void saveArticles(List<Article> articles) {
        repository.saveArticles(articles);
    }

    public List<Document> search(String query) {
        return repository.searchSimilar(query);
    }

}
