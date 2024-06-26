package dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.commands;

import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedUser;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CommandParent;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MixedCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        MixedQueue mixedQueue;
        if (!message.getText().equals("/mixed") && !message.getText().equals("/mixed@lumios_bot")) {
            String alias = message.getText()
                    .replace("/mixed@lumios_bot ", "")
                    .replace("/mixed ", "")
                    .toUpperCase();
            mixedQueue = new MixedQueue(alias);
        } else {
            mixedQueue = new MixedQueue();
        }
        MixedUser mixedUser = new MixedUser();
        mixedUser.setName(message.getFrom().getFirstName());
        mixedUser.setAccountId(message.getFrom().getId());
        if (message.getFrom().getUserName() == null) {
            mixedUser.setUsername("ukhilyant");
        } else {
            mixedUser.setUsername(message.getFrom().getUserName());
        }
        mixedQueue.getContents().add(mixedUser);
        TextMessage queueMessage = new TextMessage();
        queueMessage.setChatId(message.getChatId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(mixedQueue.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (MixedUser iteMixedUser : mixedQueue.getContents()) {
            stringBuilder.append("ID: ").append(id).append(" - ").append(iteMixedUser.getName()).append(" (@").append(iteMixedUser.getUsername()).append(")\n");
            ++id;
        }
        queueMessage.setText(stringBuilder.toString());
        queueMessage.setReplyKeyboard(QueueMarkupUtil.createMarkup(mixedQueue));
        Message sendTextMessage = this.telegramClient.sendTextMessage(queueMessage);
        try {
            this.telegramClient.pinChatMessage(sendTextMessage.getChatId(), sendTextMessage.getMessageId());
        } catch (TelegramApiException e) {
            sendMessage("Якщо ви хочете, щоб повідомлення було закріплено автоматично, надайте мені необхідні дозволи!");
        }
        mixedQueue.setMessageId(sendTextMessage.getMessageId());
        mixedQueue.setChatId(message.getChatId());
        queueService.save(mixedQueue);
    }
}

