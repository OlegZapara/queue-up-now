package dev.ua.ikeepcalm.lumios.telegram.modules.impl.reverence.commands;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@Component
public class StatsCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        List<ReverenceUser> users = userService.findAll(reverenceChat);
        String statsMessage = buildStatsMessage(users);
        sendMessage(statsMessage, ParseMode.MARKDOWN);
    }

    private String buildStatsMessage(List<ReverenceUser> users) {
        List<ReverenceUser> sortedUsers = users.stream()
                .sorted((user1, user2) -> Integer.compare(user2.getReverence(), user1.getReverence()))
                .toList();

        int maxReverence = sortedUsers.getFirst().getReverence();

        StringBuilder builder = new StringBuilder("```Загальна-статистика");

        for (ReverenceUser user : sortedUsers) {
            if (user.getReverence() >= maxReverence * 0.01) {
                builder.append(" ▻ ").append(user.getUsername()).append(": ").append(user.getReverence()).append("\n");
            }
        }

        builder.append("```");

        return builder.toString();
    }


}