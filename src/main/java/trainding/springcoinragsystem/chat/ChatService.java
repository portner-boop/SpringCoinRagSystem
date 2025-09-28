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
import trainding.springcoinragsystem.chunking.ChunkingService;
import trainding.springcoinragsystem.chunking.StringPreparing;
import trainding.springcoinragsystem.entity.Article;
import trainding.springcoinragsystem.parser.CoinTelegraphParserService;
import trainding.springcoinragsystem.qdrant.Qdrantservice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatModel chatModel;
    private final CoinTelegraphParserService coinTelegraphParser;
    private final ChunkingService chunkingProcess;
    private final Qdrantservice qdrantservice;

    public String chatWithRag(String query) {
        List<Document> docs = qdrantservice.search(
                SearchRequest
                        .builder()
                        .query(StringPreparing.removeStopWords(StringPreparing.cleanText(query)))
                        .topK(20)
                        .build());
        List<String> uniqueContents = docs
                .stream()
                .map(Document::getText)
                .collect(Collectors.toList());
        String context = String.join("\n", uniqueContents);
        PromptTemplate promptTemplate = new PromptTemplate("""
                You are an assistant that MUST refuse any request for instructions to commit harm, build weapons, engage in illegal activities, or provide step-by-step instructions for wrongdoing.
                If a user asks for such instructions, respond with the refusal:
                "Извините, я не могу помогать с инструкциями по причинению вреда или изготовлению оружия. Могу помочь с безопасной информацией про криптовалюту."
                
                USER prompt (передаётся вместе с контекстом):
                Задача: Тщательно проанализируй предоставленный контекст и дай развёрнутый ответ на вопрос.
                
                Общие правила:
                - Отвечай ТОЛЬКО на основании информации, явно присутствующей в блоке "Контекст" ниже.
                - Если запрос — инструкция по вреду / незаконной деятельности, СРАЗУ ОТКАЖИСЬ согласно правилу (не продолжай).
                - Если в контексте нет данных, подтверждающих ответ — напиши ровно: "Недостаточно информации".
                - Каждый фактический пункт обязан иметь источники в формате.
                - Не добавляй внешней информации и не упоминай, что используешь контекст.
                
                Контекст:
                {context}
                
                Вопрос:
                {question}
                
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
