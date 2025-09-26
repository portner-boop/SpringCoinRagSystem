package trainding.springcoinragsystem.embedding;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import trainding.springcoinragsystem.dto.EmbeddingResponse;
import trainding.springcoinragsystem.entity.Chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final RestTemplate ollamaRestTemplate;

    @Value("${embedding.ollama.url}")
    private String embeddingUrl;

    @Value("${embedding.ollama.model}")
    private String embeddingModel;

    public List<Float> embed(String text) {
        var request = Map.of(
                "model", embeddingModel,
                "prompt", text
        );
        var response = ollamaRestTemplate.postForObject(
                embeddingUrl,
                request,
                EmbeddingResponse.class
        );
        if (response == null || response.embedding() == null) {
            throw new IllegalStateException("Ollama вернула пустой ответ для текста: " + text);
        }
        return response.embedding()
                .stream()
                .map(Number::floatValue)
                .toList();
    }

    public Chunk embedChunk(Chunk chunk) {
        chunk.setEmbedding(embed(chunk.getText()));
        return chunk;
    }
}