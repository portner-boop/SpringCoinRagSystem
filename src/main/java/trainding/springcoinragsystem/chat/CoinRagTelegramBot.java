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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class CoinRagTelegramBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(CoinRagTelegramBot.class);

    private final ChatService ragChatService;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    public CoinRagTelegramBot(ChatService ragChatService, DefaultBotOptions botOptions) {
        super(botOptions);
        this.ragChatService = ragChatService;
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
            Long messageId = null;

            try {
                SendMessage loadingMessage = SendMessage.builder()
                        .chatId(chatId)
                        .text("Ответ обрабатывается...")
                        .build();
                messageId = Long.valueOf(execute(loadingMessage).getMessageId());

                String response;
                if (messageText.equals("/start")) {
                    response = "Welcome to Coin RAG Bot! Ask about crypto articles.";
                } else if (messageText.equals("/update-admin")) {
                    ragChatService.updateArticlesInQdrant();
                    response = "Data updated successfully from CoinTelegraph.";
                } else{
                    response = ragChatService.chatWithRag(messageText);
                }

                editMessage(chatId, messageId, response);
            } catch (Exception e) {
                log.error("Error processing message for chat {}: {}", chatId, messageText, e);
                if (messageId != null) {
                    editMessage(chatId, messageId, "Sorry, an error occurred. Try again later.");
                } else {
                    sendMessage(chatId, "Sorry, an error occurred. Try again later.");
                }
            }
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
}