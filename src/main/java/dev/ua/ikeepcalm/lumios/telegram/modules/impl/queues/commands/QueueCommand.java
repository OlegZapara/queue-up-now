package dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.commands;

import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleUser;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CommandParent;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class QueueCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        SimpleQueue simpleQueue;
        if (!message.getText().equals("/queue") && !message.getText().equals("/queue@lumios_bot")) {
            String alias = message.getText()
                    .replace("/queue@lumios_bot ", "")
                    .replace("/queue ", "")
                    .toUpperCase();
            simpleQueue = new SimpleQueue(alias);
        } else {
            simpleQueue = new SimpleQueue();
        }
        SimpleUser simpleUser = new SimpleUser();
        simpleUser.setName(message.getFrom().getFirstName());
        simpleUser.setAccountId(message.getFrom().getId());
        if (message.getFrom().getUserName() == null) {
            simpleUser.setUsername("ukhilyant");
        } else {
            simpleUser.setUsername(message.getFrom().getUserName());
        }
        simpleQueue.getContents().add(simpleUser);
        TextMessage queueMessage = new TextMessage();
        queueMessage.setChatId(message.getChatId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(simpleQueue.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (SimpleUser iteSimpleUser : simpleQueue.getContents()) {
            stringBuilder.append("ID: ")
                    .append(id).append(" - ")
                    .append(iteSimpleUser.getName())
                    .append(" (@").append(iteSimpleUser.getUsername()).append(")\n");
            ++id;
        }
        queueMessage.setText(stringBuilder.toString());
        queueMessage.setReplyKeyboard(QueueMarkupUtil.createMarkup(simpleQueue));
        Message sendTextMessage = this.telegramClient.sendTextMessage(queueMessage);
        try {
            this.telegramClient.pinChatMessage(sendTextMessage.getChatId(), sendTextMessage.getMessageId());
        } catch (TelegramApiException e) {
            sendMessage("Якщо ви хочете, щоб повідомлення було закріплено автоматично, надайте мені необхідні дозволи!");
        }
        simpleQueue.setMessageId(sendTextMessage.getMessageId());
        simpleQueue.setChatId(message.getChatId());
        queueService.save(simpleQueue);
    }
}

