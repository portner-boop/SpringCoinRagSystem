package trainding.springcoinragsystem.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatModel chatModel;
    private final CoinTelegraphParserService coinTelegraphParser;
    private final ChunkingService chunkingProcess;
    private final Qdrantservice qdrantservice;
    private final ObjectMapper objectMapper;

    public String chatWithRagAdvanced(String query) {
        List<String> variants = generateVariants(query);
        Map<String, Float> aggregatedScores = new HashMap<>();
        Map<String, Document> textToDoc = new HashMap<>();
        for (String variant : variants) {
            List<Document> hits =
                    qdrantservice.search(SearchRequest
                            .builder()
                            .query(StringPreparing.cleanText(StringPreparing.removeStopWords(variant)))
                            .topK(10)
                            .build());
            for (Document doc : hits) {
                String text = doc.getText();
                float score = doc.getScore() != null ? doc.getScore().floatValue() : 0f;
                aggregatedScores.merge(text, score, Float::sum);
                textToDoc.putIfAbsent(text, doc);
            }
        }
        int finalTopK = 30;
        String context = aggregatedScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(finalTopK)
                .map(Map.Entry::getKey)
                .map(textToDoc::get)
                .map(Document::getText)
                .distinct()
                .collect(Collectors.joining("\n"));

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
                - Не форматируй текст жирным или иным маркером.
                - Не добавляй внешней информации и не упоминай, что используешь контекст.
                - Это запрос с уклоном на расширенный ответ, старайся дать большой и точный ответ.
                
                Контекст:
                {context}
                
                Вопрос:
                {question}
                
                Ответ:
                """);

        Prompt prompt = promptTemplate.create(Map.of(
                "context", context,
                "question", query
        ));
        log.info(query + " | " + context);

        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }
    public String chatWithRagCommon(String query) {
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

    private List<String> generateVariants(String query) {
        String prompt = """
                Дай 5 переформулировки поискового запроса.
                Используй только синонимы или перефраз, не меняй смысл.
                Верни результат в JSON-массиве строк.
                
                Запрос: "%s"
                """.formatted(query);

        ChatResponse resp = chatModel.call(new Prompt(prompt));
        String json = resp.getResult().getOutput().getText();

        try {
            List<String> variants = objectMapper.readValue(json, new TypeReference<>() {
            });
            Set<String> set = new LinkedHashSet<>();
            set.add(query);
            set.addAll(variants);
            return new ArrayList<>(set);
        } catch (Exception e) {
            return List.of(query);
        }
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
