package trainding.springcoinragsystem.chunking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import trainding.springcoinragsystem.entity.Article;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class ChunkingServiceTest {
    @Mock
    private ChunkingService chunkingService;

    @Test
    @DisplayName("Should handle empty list of articles")
    void shouldHandleEmptyArticleList() {
        List<Article> result = chunkingService.getArticlesWithChunks(List.of());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
