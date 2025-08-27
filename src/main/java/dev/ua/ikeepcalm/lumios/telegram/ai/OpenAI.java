package dev.ua.ikeepcalm.lumios.telegram.ai;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.RecordService;
import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import dev.ua.ikeepcalm.lumios.telegram.exceptions.AiServiceException;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class OpenAI {

    private SimpleOpenAI openAI;
    private final RecordService recordService;

    @Value("${openai.api.key}")
    private String apiKey;

    public OpenAI(RecordService recordService) {
        this.recordService = recordService;
    }

    public CompletableFuture<String> getChatSummary(long chatId, int amountOfMessages) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                setupOpenAI();
                return executeSummary(chatId, amountOfMessages);
            } catch (Exception e) {
                throw new RuntimeException(new AiServiceException(
                    "Failed to generate chat summary", "OpenAI", "summary", e));
            }
        });
    }

    public CompletableFuture<String> getChatResponse(String message, long chatId) {
        try {
            setupOpenAI();
            return regularChatResponseHandling(message, chatId);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(
                new AiServiceException("Failed to get chat response", "OpenAI", "chat", e));
        }
    }

    private void setupOpenAI() {
        if (openAI == null) {
            openAI = SimpleOpenAI.builder().apiKey(apiKey).build();
        }
    }

    private String executeSummary(long chatId, int amountOfMessages) {
        List<MessageRecord> userMessages = recordService.findLastMessagesByChatId(chatId, amountOfMessages);
        userMessages.sort(Comparator.comparing(MessageRecord::getDate));

        StringBuilder messagesToSummarize = new StringBuilder();
        for (MessageRecord message : userMessages) {
            if (message.getText().contains("MEDIA") || message.getText().contains("lumios")) {
                continue;
            }

            String fullName = message.getUser().getFullName() == null ? message.getUser().getUsername() : message.getUser().getFullName();

            messagesToSummarize.append(fullName).append(": ").append(message.getText()).append("\n");
        }

        String prompt = """
                As a professional summarizer, create a concise and comprehensive summary of the provided conversation in group chat, while adhering to these guidelines:
                    1. Craft a summary that is detailed, thorough, in-depth, and complex, while maintaining clarity and conciseness.
                    2. Incorporate main ideas and essential information, eliminating extraneous language and focusing on critical aspects.
                    3. Rely strictly on the provided text, without including external information.
                    4. Format the summary in paragraph form for easy understanding.
                    5. Summary should be divided into paragraphs, each covering a different aspect of the conversation including names or tags of the participants.
                By following this optimized prompt, you will generate an effective summary that encapsulates the essence of the given text in a clear, concise, and reader-friendly manner.
                
                """;

        var chatRequest = ChatRequest.builder().model("gpt-4o")
                .message(ChatMessage.SystemMessage.of("You preferred language is Ukrainian. If use custom text formatting, use Markdown syntax. If meet any symbols recognized as Markdown syntax, but not actually used in formatting, escape them with a backslash (\\)."))
                .message(ChatMessage.UserMessage.of(prompt + ":\n" + messagesToSummarize)).temperature(0.4).maxTokens(8000).build();

        var futureChat = openAI.chatCompletions().create(chatRequest);
        var chatResponse = futureChat.join();
        return chatResponse.firstContent();
    }

    private CompletableFuture<String> regularChatResponseHandling(String message, long chatId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var chatRequest = ChatRequest.builder()
                    .model("gpt-4o-mini")
                    .message(ChatMessage.SystemMessage.of("""
                        Act as if you are talking to intelligent interlocutors who understand your technical programming concepts perfectly, but still respond briefly and concisely. Your preferred language is Ukrainian.
                        If asked about programming concepts, you can provide detailed explanations and examples, preferably in Java. If use custom text formatting, use Markdown syntax.
                        """))
                    .message(ChatMessage.UserMessage.of(message))
                    .temperature(0.0)
                    .maxTokens(3000)
                    .build();
                
                var futureChat = openAI.chatCompletions().create(chatRequest);
                var chatResponse = futureChat.join();
                return chatResponse.firstContent();
            } catch (Exception e) {
                throw new RuntimeException(new AiServiceException(
                    "Failed to process chat request", "OpenAI", "chat", e));
            }
        });
    }
}