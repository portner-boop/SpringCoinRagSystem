package trainding.springcoinragsystem.chat;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import trainding.springcoinragsystem.chunking.ChunkingProcess;
import trainding.springcoinragsystem.chunking.StringPreparing;
import trainding.springcoinragsystem.entity.Article;
import trainding.springcoinragsystem.parser.CoinTelegraphParserService;
import trainding.springcoinragsystem.qdrant.Qdrantservice;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatModel chatModel;
    private final CoinTelegraphParserService coinTelegraphParser;
    private final ChunkingProcess chunkingProcess;
    private final Qdrantservice qdrantservice;

    public String chatWithRag(String query) {
        List<Document> docs = qdrantservice.search(
                SearchRequest
                        .builder()
                        .query(query)
                        .topK(20)
                        .build());
        Set<String> uniqueContents = docs
                .stream()
                .map(Document::getText)
                .collect(Collectors.toSet());
        String context = String.join("\n", uniqueContents);
        PromptTemplate promptTemplate = new PromptTemplate("""
                Задача: Тщательно проанализируй предоставленный контекст и дай развернутый ответ на вопрос.
                Требования к ответу:
                - Основан ТОЛЬКО на информации из контекста.
                - Будь точным и фактологичным.
                - Если вопрос подразумевает перечисление данных (например, ценовые диапазоны), используй четкую структуру:
                ключевые значения, уровни поддержки/сопротивления, причины изменений.
                - Для сложных ответов используй нумерацию или маркеры для ясности.
                - Если информации в контексте недостаточно для ответа, сообщи об этом.
                
                Контекст: {context}
                
                Вопрос: {question}
                
                Ответ:
                """);
        log.info("context: " + context);
        Prompt prompt = promptTemplate.create(
                Map.of("context", context, "question", StringPreparing.cleanText(query)));
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    @Async
    public void updateArticlesInQdrant() throws InterruptedException {
        log.info("=== Starting on-demand parsing and vectorization ===");
        List<Article> articles = coinTelegraphParser.parse();
        List<Article> processedArticles = chunkingProcess.getArticlesWithChunks(articles);
        qdrantservice.saveArticles(processedArticles);
        log.info("=== Updated {} articles ===", processedArticles.size());
    }

}
