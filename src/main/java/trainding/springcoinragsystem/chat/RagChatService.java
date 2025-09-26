package trainding.springcoinragsystem.chat;

import org.springframework.scheduling.annotation.Async;
import trainding.springcoinragsystem.embedding.StringPreparing;
import trainding.springcoinragsystem.entity.Article;
import trainding.springcoinragsystem.repository.Qdrantservice;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import trainding.springcoinragsystem.embedding.ChunkingProcess;
import trainding.springcoinragsystem.parser.CoinTelegraphParser;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagChatService {
    private static final Logger log = LoggerFactory.getLogger(RagChatService.class);

    private final ChatModel chatModel;
    private final VectorStore vectorStore;
    private final CoinTelegraphParser coinTelegraphParser;
    private final ChunkingProcess chunkingProcess;
    private final Qdrantservice qdrantservice;

    public String chatWithRag(String query){
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest
                        .builder()
                        .query(query)
                        .topK(5)
                        .build());
        if(docs.isEmpty()){
            return "Нет данных для обработки вашего запроса";
        }
        Set<String> uniqueContents = docs
                .stream()
                .map(Document::getMetadata)
                .map(meta->meta.get("article_text").toString())
                .collect(Collectors.toSet());
        String context = String.join("\n", uniqueContents);
        PromptTemplate promptTemplate = new PromptTemplate("""
    Задача: Анализ контекста: Тщательно изучи весь предоставленный контекст, включая все статьи и фрагменты.
    Убедись, что ответ основан исключительно на релевантных частях контекста.
    Релевантность: Отвечай только на основе информации, напрямую связанной с вопросом.
    Если вопрос касается конкретной темы, игнорируй нерелевантные разделы контекста.
    Не вводи новую информацию или обобщения, не содержащиеся в контексте.
    Полнота: Если контекст содержит несколько статей или фрагментов, учти все релевантные из них. Не игнорируй ни один подходящий источник;
    интегрируй ключевые детали из всех, если они дополняют ответ.
    Формулировка ответа: Если контекст дает достаточно данных для точного и полного ответа, сформулируй его четко, кратко и структурированно.
    Используй нумерацию или маркеры для ясности, если ответ сложный.
    Неопределенность: Если контекст не содержит достаточной или релевантной информации для уверенного ответа, или вопрос не относится напрямую к контексту, укажи ровно: «В предоставленном контексте нет релевантной информации для ответа на этот вопрос». Не объясняй причины.
    Структура ответа: Ограничься только ответом на вопрос. Не добавляй введение, заключение или пояснения вне контекста. Форматируй текст просто, без markdown, жирного шрифта или других выделений.
    Язык: Все ответы должны быть на русском языке.
    
    ---
    Контекст: {context}
    ---
    Вопрос: {question}
    ---
    Ответ:
    """);
        log.info("context: " +  context);
        Prompt prompt =  promptTemplate.create(Map.of("context",context,"question", StringPreparing.cleanText(query)));
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    @Async
    public void updateArticles() throws InterruptedException {
        log.info("=== Starting on-demand parsing and vectorization ===");
        List<Article> articles = coinTelegraphParser.parse();
        List<Article> processedArticles = chunkingProcess.getArticlesWithChunks(articles);
        qdrantservice.saveArticles(processedArticles);
        log.info("=== Updated {} articles ===", processedArticles.size());
    }

}
