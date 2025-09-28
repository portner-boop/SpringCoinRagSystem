package trainding.springcoinragsystem.chunking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import trainding.springcoinragsystem.entity.Article;
import trainding.springcoinragsystem.entity.Chunk;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChunkTaskTest {

    private ChunkTask chunkTask;
    private List<Article> testArticles;
    private final int SENTENCES_PER_CHUNK = 3;

    @BeforeEach
    public void setUp(){
        testArticles = List.of(
                new Article("Test Article 1",
                        "title",
                        "First sentence. Second sentence. Third sentence. Fourth sentence. Fifth sentence.",
                        null),
                new Article("Test Article 2",
                        "title",
                        "One. Two. Three.",
                        null)
                );
        chunkTask = new ChunkTask(testArticles, SENTENCES_PER_CHUNK);
    }

    @Test
    @DisplayName("Should split text to correct chunks")
    public void shouldSplitTextIntoChunksCorrectly(){

        List<Article> result = chunkTask.call();

        assertNotNull(result);
        assertEquals(2,result.size());

        Article firstArticle = result.getFirst();
        List<Chunk> chunks = firstArticle.getChunks();
        assertNotNull(chunks);
        assertEquals(2,chunks.size());

        Chunk firstChunk = chunks.get(0);
        assertNotNull(firstChunk.getText());
        assertTrue(firstChunk.getText().contains("first sentence"));

        Chunk secondChunk = chunks.get(1);
        assertNotNull(secondChunk.getText());
        assertTrue(secondChunk.getText().contains("fourth"));
    }

    @Test
    @DisplayName("Should split and load chunks with size shorter than splitter param")
    public void shouldHandleArticleWithFewerSentencesThanChunkSize(){
        Article shortArticle =new Article("Test Article 1",
                "title",
                "First sentence. Second sentence.",
                null);

        ChunkTask chunkTask =new ChunkTask(List.of(shortArticle),SENTENCES_PER_CHUNK);

        List<Article> result = chunkTask.call();

        Article firstArticle = result.getFirst();
        assertNotNull(firstArticle);
        assertNotNull(firstArticle.getChunks());
        assertEquals(1,firstArticle.getChunks().size());
    }
}
