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
                Задача: Тщательно изучи весь контекст, включая все статьи и фрагменты.
                Отвечай только на основе релевантных частей.
                Релевантность: Используй только информацию, напрямую связанную с вопросом.
                Игнорируй нерелевантные разделы. Не вводи новую информацию или обобщения.
                Полнота: Учти все релевантные источники в контексте.
                Интегрируй ключевые детали из них, если они дополняют ответ.
                Формулировка: Если контекст дает достаточно данных, сформулируй ответ четко, кратко и структурировано.
                Используй нумерацию или маркеры для ясности при сложных ответах.
                Неопределенность: Если контекст не содержит релевантной информации или вопрос не относится к нему,
                укажи ровно: «В предоставленном контексте нет релевантной информации для ответа на этот вопрос».
                Не объясняй причины.
                Структура: Ограничься только ответом на вопрос.
                Без введения, заключения или пояснений.Язык: Ответы на русском.
                Не используй markdown, жирный шрифт или выделения.
                Ни в коем случае не выделяй текст.
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
