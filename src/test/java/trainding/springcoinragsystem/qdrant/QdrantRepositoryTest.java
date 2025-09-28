package trainding.springcoinragsystem.qdrant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import trainding.springcoinragsystem.entity.Article;
import trainding.springcoinragsystem.entity.Chunk;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QdrantRepositoryTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private ExecutorService executorService;

    @InjectMocks
    private QdrantRepository qdrantRepository;

    @Test
    @DisplayName("Should handle search")
    void shouldSearchSimilarDocuments() {
        String query = "test query";
        when(vectorStore.similaritySearch(query))
                .thenReturn(List.of(new Document("test content")));

        List<Document> result = qdrantRepository.searchSimilar(query);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(vectorStore, times(1)).similaritySearch(query);
    }

    private Chunk createChunk(String text, int index) {
        Chunk chunk = new Chunk();
        chunk.setText(text);
        chunk.setIndex(index);
        return chunk;
    }
}
