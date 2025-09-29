package trainding.springcoinragsystem.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class CoinRagTelegramBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(CoinRagTelegramBot.class);

    private final ChatService ragChatService;
    private final ExecutorService executorService;
    private final Map<Long, Mode> userModes = new HashMap<>();
    @Value("${telegram.bot.token}")
    private String botToken;
    @Value("${telegram.bot.username}")
    private String botUsername;

    public CoinRagTelegramBot(ChatService ragChatService, DefaultBotOptions botOptions) {
        super(botOptions);
        this.ragChatService = ragChatService;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String messageText = update.getMessage().getText().trim();

            if (messageText.equals("/start")) {
                sendModeButtons(chatId, "Добро пожаловать в Coin RAG Bot!\n\n⚙️ Выберите режим работы:");
                return;
            }

            if (messageText.equals("/update_admin")) {
                try {
                    ragChatService.updateArticlesInQdrant();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                sendMessage(chatId, "✅ Данные обновлены из CoinTelegraph.");
                return;
            }

            // только для реальных запросов отправляем "обрабатывается..."
            Long loadingMsgId = sendLoadingMessage(chatId);
            executorService.submit(() -> processMessage(chatId, messageText, loadingMsgId));

        } else if (update.hasCallbackQuery()) {
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            Long messageId = (long) update.getCallbackQuery().getMessage().getMessageId();
            String callbackData = update.getCallbackQuery().getData();

            if (callbackData.equals("MODE_COMMON")) {
                userModes.put(Long.valueOf(chatId), Mode.COMMON);
                editMessage(chatId, messageId, "✅ Режим переключён: Common");
            } else if (callbackData.equals("MODE_ADVANCED")) {
                userModes.put(Long.valueOf(chatId), Mode.ADVANCED);
                editMessage(chatId, messageId, "✅ Режим переключён: Advanced");
            }
        }
    }

    private Long sendLoadingMessage(String chatId) {
        SendMessage loadingMessage = SendMessage.builder()
                .chatId(chatId)
                .text("⌛ Ответ обрабатывается...")
                .build();
        try {
            return Long.valueOf(execute(loadingMessage).getMessageId());
        } catch (TelegramApiException e) {
            log.error("Failed to send loading message to {}: {}", chatId, e.getMessage());
            return null;
        }
    }

    private void processMessage(String chatId, String messageText, Long messageId) {
        try {
            Mode mode = userModes.getOrDefault(Long.valueOf(chatId), Mode.COMMON);
            String response = (mode == Mode.COMMON)
                    ? ragChatService.chatWithRagCommon(messageText)
                    : ragChatService.chatWithRagAdvanced(messageText);

            if (messageId != null) {
                editMessage(chatId, messageId, response);
                sendModeButtons(chatId, "⚙️ Переключить режим:");
            } else {
                sendMessage(chatId, response);
            }

        } catch (Exception e) {
            log.error("Error processing message for chat {}: {}", chatId, messageText, e);
            String errorMessage = "❌ Ошибка. Попробуйте позже.";
            if (messageId != null) {
                editMessage(chatId, messageId, errorMessage);
            } else {
                sendMessage(chatId, errorMessage);
            }
        }
    }

    private void sendModeButtons(String chatId, String text) {
        InlineKeyboardButton commonButton = InlineKeyboardButton.builder()
                .text("⚡ Common")
                .callbackData("MODE_COMMON")
                .build();
        InlineKeyboardButton advancedButton = InlineKeyboardButton.builder()
                .text("🚀 Advanced")
                .callbackData("MODE_ADVANCED")
                .build();

        List<List<InlineKeyboardButton>> keyboard = List.of(List.of(commonButton, advancedButton));

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().keyboard(keyboard).build();

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(markup)
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send mode buttons to {}: {}", chatId, e.getMessage());
        }
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to {}: {}", chatId, text, e);
        }
    }

    private void editMessage(String chatId, Long messageId, String text) {
        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(Math.toIntExact(messageId))
                .text(text)
                .build();
        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to edit message {} in chat {}: {}", messageId, chatId, text, e);
            sendMessage(chatId, text);
        }
    }

    @Override
    public void onClosing() {
        executorService.shutdown();
    }

    // режимы
    private enum Mode {COMMON, ADVANCED}
}
